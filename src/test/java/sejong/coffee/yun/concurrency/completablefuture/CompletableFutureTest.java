package sejong.coffee.yun.concurrency.completablefuture;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import sejong.coffee.yun.domain.order.menu.Bread;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.jdbc.JdbcRepository;
import sejong.coffee.yun.service.MenuService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class CompletableFutureTest extends MainIntegrationTest {

    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private MenuService menuService;
    @Autowired
    @Qualifier("mysqlJdbcRepositoryImpl")
    JdbcRepository jdbcMysqlRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisTemplate<String, Object> objectRedisTemplate;

    private final List<Menu> menus = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {

        for (int num = 1; num <= 100; num++) {
            Menu menu = Bread.builder()
                    .id((long) (num))
                    .title("빵" + num)
                    .description("성심당과 콜라보한 빵")
                    .nutrients(new Nutrients(num, num, num, num))
                    .now(LocalDateTime.now())
                    .menuSize(MenuSize.M)
                    .price(Money.initialPrice(new BigDecimal(4000)))
                    .stock(100)
                    .build();

            menus.add(menu);
            menuRepository.save(menu);
        }
//        jdbcMysqlRepository.saveMenusByJdbc(menus);
        cacheMenus(menus);
    }

    @AfterEach
    public void deleteAll() {
        menuRepository.clear();
    }

    @Test
    void writeBackTest() {
        long start = System.nanoTime();

        // 비동기 코드 또는 동기 코드 실행
        menuService.refreshPopularMenusInRedis(); // Execution time: 10.88125 ms

        long end = System.nanoTime();

        double elapsedTime = (end - start) / 1_000_000.0;  // 나노초를 밀리세컨드로 변환
        System.out.println("Execution time: " + elapsedTime + " ms");
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

    @Test
    void initMenus() {
        System.out.println("hi");
    }
}
