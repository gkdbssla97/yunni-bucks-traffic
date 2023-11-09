package sejong.coffee.yun.concurrency.optimisticlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sejong.coffee.yun.domain.exception.MenuException;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderOptimisticLockTest extends MainIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private MenuRepository menuRepository;

    private Member member;
    private Order order;
    private List<CartItem> cartItems = new ArrayList<>();
    private Menu menu;
    private Cart cart;
    private Coupon coupon;

    @BeforeEach
    void init() {
        member = Member.builder()
                .address(new Address("서울시", "광진구", "화양동", "123-432"))
                .userRank(UserRank.BRONZE)
                .name("홍길동")
                .password("qwer1234@A")
                .money(Money.ZERO)
                .email("qwer123@naver.com")
                .orderCount(0)
                .build();

        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        menu = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .stock(10)
                .build();

        member = userRepository.save(member);
        menu = menuRepository.save(menu);

        cartService.createCart(member.getId());
        cart = cartService.addMenu(member.getId(), menu.getId());
    }

    @Test
    @DisplayName("한명의_사용자가_동시적으로_여러개의_주문을_한다")
    void concurrencyOrdersByOneCustomer() throws InterruptedException {

        // given
        int numberOfThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

        // when
        for (int i = 0; i < numberOfThread; i++) {
            executorService.submit(() -> {
                try {
                    // 주문 로직 실행
                    orderService.order(member.getId(), LocalDateTime.now());
                } catch (MenuException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
            Thread.sleep(50);
        }
        countDownLatch.await();  // 모든 작업이 완료될 때까지 대기
        executorService.shutdown();  // 모든 작업이 완료되면 ExecutorService를 종료

        // then
        Member byId = userRepository.findById(member.getId());
        Integer orderCount = byId.getOrderCount();
        assertThat(orderCount).isEqualTo(10);
    }

    @Test
    @DisplayName("1개의 카트에 동시적으로 여러개 메뉴를 담는다")
    void concurrencyCartByMultipleMenu() throws InterruptedException {

        // given
        int numberOfThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

        // when
        for (int i = 0; i < numberOfThread; i++) {
            executorService.submit(() -> {
                try {
                    // 주문 로직 실행
                    cartService.addMenu(member.getId(), menu.getId());
                } catch (MenuException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
//            Thread.sleep(50);
        }
        countDownLatch.await();  // 모든 작업이 완료될 때까지 대기
        executorService.shutdown();  // 모든 작업이 완료되면 ExecutorService를 종료

        // then
        List<CartItem> cartItemList = cartItemRepository.findAll();
        assertThat(cartItemList.size()).isEqualTo(100);
    }
}
