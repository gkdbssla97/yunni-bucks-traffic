package sejong.coffee.yun.concurrency.pessimisticlock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.facade.OptimisticLockStockFacade;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderPessimisticLockTest extends MainIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    private List<Member> members = new ArrayList<>();
    private List<CartItem> menuList = new ArrayList<>();
    private Menu beverage;
    private Cart cart;
    private Coupon coupon;
    private final int parameter = 1000;

    @PostConstruct
    void init() {
        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        beverage = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .stock(parameter)
                .build();

        beverage = menuRepository.save(beverage);

        members = IntStream.range(0, parameter)
                .mapToObj(i -> {
                    Member member = Member.builder()
                            .address(new Address("서울시", "광진구", "화양동", "123-432"))
                            .userRank(UserRank.BRONZE)
                            .name("홍길동" + i)
                            .password("qwer1234@A")
                            .money(Money.ZERO)
                            .email("qwer123" + i + "@naver.com")
                            .orderCount(0)
                            .build();
                    Member savedMember = userRepository.save(member);
                    cartService.createCart(savedMember.getId());
                    cartService.addMenu(savedMember.getId(), beverage.getId());
                    return savedMember;
                })
                .collect(toList());
    }

    @Nested
    @DisplayName("DB Lock 동시성 테스트")
    class ConcurrencyTest {
        @Test
        @DisplayName("여러명의 사용자가 동시적으로 음료 A를 주문한다. (비관적 락 기법)")
        void concurrencyOrderForPessimisticLock() throws InterruptedException {

            // given
            int numberOfThread = parameter;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

            // when
            for (Member member : members) {
                executorService.submit(() -> {
                    try {
                        orderService.orderWithPessimisticLock(member.getId(), LocalDateTime.now());
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            countDownLatch.await();  // 모든 주문이 완료될 때까지 대기합니다.
            executorService.shutdown();

            // then
            Menu findMenu = menuRepository.findById(beverage.getId());
            assertThat(findMenu.getStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("여러명의 사용자가 동시적으로 음료 A를 주문한다. (낙관적 락 기법)")
        void concurrencyOrderForOptimisticLock() throws InterruptedException {

            // given
            int numberOfThread = parameter;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

            // when
            for (Member member : members) {
                executorService.submit(() -> {
                    try {
                        optimisticLockStockFacade.order(member.getId(), LocalDateTime.now());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            countDownLatch.await();  // 모든 주문이 완료될 때까지 대기합니다.
            executorService.shutdown();

            // then
            Menu findMenu = menuRepository.findById(beverage.getId());
            assertThat(findMenu.getStock()).isEqualTo(0);
        }
    }
}
