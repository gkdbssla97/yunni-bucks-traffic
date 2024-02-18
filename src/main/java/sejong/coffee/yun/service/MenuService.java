package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Bread;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuType;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.facade.RedissonLockFacade;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.util.wrapper.RestPage;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static sejong.coffee.yun.dto.menu.MenuRankingDto.Response;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final RedissonLockFacade redissonLockFacade;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @CacheEvict(value = {"Menu", "AllMenus"}, allEntries = true)
    public Menu create(MenuDto.Request request) {
        Menu menu = createMenu(request);

        return menuRepository.save(menu);
    }

    private Menu createMenu(MenuDto.Request request) {
        MenuType menuType = request.menuType();
        if (menuType == MenuType.BEVERAGE) {
            return Beverage.builder()
                    .title(request.title())
                    .description(request.description())
                    .price(request.price())
                    .nutrients(request.nutrients())
                    .menuSize(request.menuSize())
                    .stock(request.stock())
                    .build();
        } else if (menuType == MenuType.BREAD) {
            return Bread.builder()
                    .title(request.title())
                    .description(request.description())
                    .price(request.price())
                    .nutrients(request.nutrients())
                    .menuSize(request.menuSize())
                    .stock(request.stock())
                    .build();
        }
        throw ExceptionControl.INVALID_MENU_TYPE.menuException();
    }

    @Cacheable(value = "AllMenus", key = "#pageable", cacheManager = "cacheManager")
    public RestPage<MenuDto.Response> findAllByCaching(Pageable pageable) {
        Page<Menu> allMenusPaged = menuRepository.findAllMenusPaged(pageable);
        return new RestPage<>(allMenusPaged.map(MenuDto.Response::new));
    }

    public Page<MenuDto.Response> findAll(Pageable pageable) {
        Page<Menu> allMenusPaged = menuRepository.findAllMenusPaged(pageable);
        return allMenusPaged.map(MenuDto.Response::new);
    }

    public MenuDto.Response menuSearch(String menuTitle) {
        increaseScore(menuTitle);
        return redissonLockFacade.findMenuFromCache(menuTitle);
    }

    public void increaseScore(String menuTitle) {
        redisTemplate.opsForZSet().incrementScore("ranking", menuTitle, 1);
    }

    public List<Response> searchRankList() {
        String key = "ranking";
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTupleSet = zSetOperations.reverseRangeWithScores(key, 0, 2);

        return Objects.requireNonNull(typedTupleSet).stream()
                .map(typedTuple -> {
                    String menuTitle = typedTuple.getValue();
                    Menu menu = menuRepository.findByTitle(menuTitle);
                    return Response.convertToResponseRankingDto(typedTuple, menu);
                })
                .sorted(Comparator.comparing(Response::viewCount).reversed()
                        .thenComparing(Response::orderCount, Comparator.reverseOrder())
                        .thenComparing(Response::menuTitle))
                .toList();
    }

    /**
     * 1. keys.stream().map(key -> ...)): 각 키에 대해 비동기 작업을 생성하고, 그 결과를 futures라는 리스트에 저장한다. CompletableFuture.runAsync()를 사용하여 각 작업을 별도의 스레드에서 비동기로 실행
     * 2. Optional.ofNullable((Menu) objectRedisTemplate.opsForValue().get(key)): 주어진 키에 대한 값(메뉴)를 Redis에서 반환한다. 가져온 메뉴가 null이 아닌 경우에만 이후의 코드 실행
     * 3. Optional.ofNullable(menuRepository.findByTitle(popularMenu.getTitle())): 가져온 메뉴의 제목과 일치하는 메뉴를 데이터베이스에서 조회한다. 찾은 메뉴가 null이 아닌 경우에만 이후의 코드 실행
     * 4. Double score = redisTemplate.opsForZSet().score("ranking", popularMenu.getTitle()): Redis의 "ranking" ZSet에서 가져온 메뉴의 제목에 해당하는 점수를 반환
     * 5. Optional.ofNullable(score).ifPresent(sc -> ...)): 가져온 점수가 null이 아닌 경우에만 updateMenu 메서드를 호출
     * 6. CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRunAsync(() -> redisTemplate.delete("menu::*")): 모든 비동기 작업이 완료되면 Redis의 "menu::*" 패턴에 매칭되는 모든 키를 삭제한다. 이 작업도 별도의 스레드에서 비동기로 실행
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00시 00분에 실행
    public void refreshPopularMenusInRedis() {
        Set<String> keys = Optional.ofNullable(redisTemplate.keys("menu::*")).orElse(Collections.emptySet());

        // CompletableFuture를 사용한 비동기 처리
        List<CompletableFuture<Void>> futures = keys.stream()
                .map(key -> CompletableFuture.runAsync(() -> {
                    Optional.ofNullable((Menu) objectRedisTemplate.opsForValue().get(key))
                            .ifPresent(popularMenu -> {
                                Optional.ofNullable(menuRepository.findByTitle(popularMenu.getTitle()))
                                        .ifPresent(findMenu -> {
                                            Double score = redisTemplate.opsForZSet().score("ranking", popularMenu.getTitle());
                                            Optional.ofNullable(score)
                                                    .ifPresent(sc -> updateMenu(findMenu, popularMenu.getOrderCount(), popularMenu.getViewCount(), sc));
                                        });
                            });
                })).toList();

        // 모든 비동기 작업이 완료되면 redisTemplate.delete("menu::*") 작업을 실행
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRunAsync(() -> redisTemplate.delete("menu::"));
    }

    // 각 작업이 별도의 트랜잭션에서 실행되며, 하나의 작업이 실패하더라도 다른 작업에는 영향을 주지 않는다.
    @Transactional
    public void updateMenu(Menu menu, int orderCount, int viewCount, double score) {
        menu.updatePopularScoreByWritingBack(orderCount, viewCount, score);
    }
}
