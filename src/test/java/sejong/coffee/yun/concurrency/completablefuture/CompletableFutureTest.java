package sejong.coffee.yun.concurrency.completablefuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.jdbc.JdbcRepository;
import sejong.coffee.yun.service.MenuService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CompletableFutureTest extends MainIntegrationTest {

    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private MenuService menuService;
    @Autowired
    @Qualifier("mysqlJdbcRepository")
    JdbcRepository menuReviewJdbcMysqlRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisTemplate<String, Object> objectRedisTemplate;

    private final List<Menu> menus = new ArrayList<>();

//    @AfterEach
//    void initDB() {
//        menuReviewRepository.clear();
//        userRepository.clear();
//    }

    @PostConstruct
    public void init() throws IOException {

        for (int i = 0; i < 100000; i++) {
            Menu menu = bread(i + 1);

            menus.add(menu);
        }
        menuReviewJdbcMysqlRepository.saveMenusByJdbc(menus);
        cacheMenus(menus);
    }

    @Test
    void writeBackTest() {
        long start = System.nanoTime();

        // 비동기 코드 또는 동기 코드 실행
        menuService.refreshPopularMenusInRedis();
        synchronizedRefreshPopularMenusInRedis();
        long end = System.nanoTime();

        double elapsedTime = (end - start) / 1000.0;
        System.out.println("Execution time: " + elapsedTime + " seconds");
    }

    public void synchronizedRefreshPopularMenusInRedis() {
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
                                menuService.updateMenu(findMenu, popularMenu.getOrderCount(), popularMenu.getViewCount(), score);
                            }
                        })
                );

        redisTemplate.delete("menu::");
    }

    private void cacheMenus(List<Menu> menus) {
        for (Menu menu : menus) {
            redisTemplate.opsForValue().set("menu::" + menu.getId(), menu);
            redisTemplate.opsForZSet().add("ranking", menu.getTitle(), 1.0);
        }
    }
}
