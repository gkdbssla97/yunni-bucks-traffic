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

import static sejong.coffee.yun.dto.menu.MenuRankingDto.Response;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final RedissonLockFacade redissonLockFacade;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @CacheEvict(value = "Menu", key = "#request.title()")
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

    @Scheduled(cron = "0 0 0 * * *") // 매일 00시 00분에 실행
    @Transactional
    public void refreshPopularMenusInRedis() {
        Optional.ofNullable(redisTemplate.keys("menu::"))
                .ifPresent(keys -> keys.stream()
                        .map(key -> (Menu) objectRedisTemplate.opsForValue().get(key))
                        .filter(Objects::nonNull)
                        .forEach(popularMenu -> {
                            Menu findMenu = menuRepository.findByTitle(popularMenu.getTitle());
                            Double score = redisTemplate.opsForZSet().score("ranking", popularMenu.getTitle());
                            if (score != null && findMenu != null) {
                                findMenu.updatePopularScoreByWritingBack(popularMenu.getOrderCount(), popularMenu.getViewCount(), score);
                            } else {
                                menuRepository.save(findMenu);
                            }
                        })
                );

        redisTemplate.delete("menu::*");
    }
}
