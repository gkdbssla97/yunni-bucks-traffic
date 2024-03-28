package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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
import sejong.coffee.yun.dto.menu.MenuPageDto;
import sejong.coffee.yun.facade.RedissonLockFacade;
import sejong.coffee.yun.repository.menu.MenuRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static sejong.coffee.yun.dto.menu.MenuRankingDto.Response;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final RedissonLockFacade redissonLockFacade;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final ThreadPoolExecutor threadPoolExecutor;

    @CacheEvict(value = {"AllMenus"}, allEntries = true)
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

    @Cacheable(value = "AllMenus", key = "#pageable.pageNumber", cacheManager = "cacheManager", condition = "#pageable.pageNumber <= 5")
    @Transactional(readOnly = true)
    public MenuPageDto.Response findAllByCaching(Pageable pageable) {
        Page<Menu> allMenusPaged = menuRepository.findAllMenusPaged(pageable);
        return MenuPageDto.Response.fromPage(allMenusPaged);
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
    public void refreshPopularMenusInRedis() {
        ScanOptions options = ScanOptions.scanOptions().match("AllMenus::*").count(500).build();
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        try {
            Cursor<byte[]> cursor = connection.scan(options);

            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                CompletableFuture.runAsync(() -> {
                            Optional.ofNullable((Menu) objectRedisTemplate.opsForValue().get(key))
                                    .ifPresent(popularMenu -> {
                                        Optional.ofNullable(menuRepository.findByTitle(popularMenu.getTitle()))
                                                .ifPresent(findMenu -> {
                                                    Double score = redisTemplate.opsForZSet().score("ranking", popularMenu.getTitle());
                                                    Optional.ofNullable(score)
                                                            .ifPresent(sc -> updateMenu(findMenu, popularMenu.getOrderCount(), popularMenu.getViewCount(), sc));
                                                });
                                    });
                        }, threadPoolExecutor)
                        .exceptionally(ex -> {
                            log.error("Failed to process key: {}", key, ex);
                            return null;
                        });
            }
            cursor.close();
        } catch (Exception e) {
            log.error("Error occurred while scanning keys", e);
        } finally {
            connection.close();
        }
    }

    @Transactional
    public void updateMenu(Menu menu, int orderCount, int viewCount, double score) {
        menu.updatePopularScoreByWritingBack(orderCount, viewCount, score);
    }
}
