package sejong.coffee.yun.service.fake;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import sejong.coffee.yun.domain.discount.condition.RankCondition;
import sejong.coffee.yun.domain.discount.policy.PercentPolicy;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.OrderPayStatus;
import sejong.coffee.yun.domain.order.OrderStatus;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.jwt.JwtProvider;
import sejong.coffee.yun.mock.repository.FakeCartItemRepository;
import sejong.coffee.yun.mock.repository.FakeMenuRepository;
import sejong.coffee.yun.mock.repository.FakeOrderRepository;
import sejong.coffee.yun.mock.repository.FakeUserRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.mock.repository.FakeCartRepository;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ContextConfiguration(classes = {
        OrderService.class,
        FakeUserRepository.class,
        FakeOrderRepository.class,
        JwtProvider.class,
        Calculator.class,
        PercentPolicy.class,
        RankCondition.class,
        FakeCartRepository.class,
        FakeMenuRepository.class,
        FakeCartItemRepository.class
})
@TestPropertySource(properties = {
        "jwt.key=applicationKey",
        "jwt.expireTime.access=100000",
        "jwt.expireTime.refresh=1000000",
        "jwt.blackList=blackList"
})
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FakeUserRepository fakeUserRepository;
    @Autowired
    private FakeOrderRepository fakeOrderRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private FakeMenuRepository fakeMenuRepository;
    @Autowired
    private FakeCartRepository fakeCartRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private FakeCartItemRepository fakeCartItemRepository;

    Member member;
    List<CartItem> menuList = new ArrayList<>();

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

        Menu menu1 = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        CartItem cartItem = CartItem.builder()
                .menu(menu1)
                .build();

        menuList.add(cartItemRepository.save(cartItem));
    }

    @AfterEach
    void initDB() {
        fakeOrderRepository.clear();
        fakeUserRepository.clear();
        fakeMenuRepository.clear();
        fakeCartRepository.clear();
        fakeCartItemRepository.clear();
    }

    @Test
    void 주문() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        // when
        Order order = orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

        int sum = menuList.stream().mapToInt(menu -> menu.getMenu().getPrice().getTotalPrice().intValue()).sum();

        // then
        assertThat(order.getMember()).isEqualTo(save);
        assertThat(order.getCart().getCartItems()).isEqualTo(menuList);
        assertThat(order.getOrderPrice().getTotalPrice()).isEqualTo(String.valueOf(sum));
    }

    @Test
    void 유저가_주문한_시간() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        LocalDateTime orderTime = LocalDateTime.of(2022, 11, 20, 11, 20);

        // when
        Order order = orderService.orderWithPessimisticLock(save.getId(), orderTime);

        // then
        assertThat(order.getCreateAt()).isEqualTo(orderTime);
    }

    @Test
    void 주문을_조회한다() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        Order order = orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

        // when
        Order findOrder = orderService.findOrder(order.getId());

        // then
        assertThat(findOrder).isEqualTo(order);
    }

    @Test
    void 주문_리스트_조회() {
        // given
        int size = 10;
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        IntStream.range(0, size).forEach(i -> orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now()));

        // when
        List<Order> orders = orderService.findAll();

        // then
        assertThat(orders.size()).isEqualTo(size);
    }

    @Test
    void 주문_수정_시간() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        LocalDateTime initTime = LocalDateTime.now();
        Order order = orderService.orderWithPessimisticLock(save.getId(), initTime);

        LocalDateTime updateTime = LocalDateTime.of(2022, 11, 20, 11, 20);

        // when
        order.setUpdateAt(updateTime);

        // then
        assertThat(order.getUpdateAt()).isEqualTo(updateTime);
    }

    @Test
    void 주문_총_금액_확인() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        // when
        Order order = orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

        // then
        assertThat(order.fetchTotalOrderPrice()).isEqualTo(menuList.get(0).getMenu().getPrice().getTotalPrice());
    }

    @Test
    void 주문명_확인() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        // when
        Order order = orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

        // then
        assertThat(order.getName()).isEqualTo(menuList.get(0).getMenu().getTitle() + " 외 " + menuList.size() + "개");
    }

    @Test
    void 유저가_주문_하고_주문_개수_확인() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        // when
        orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

        // then
        assertThat(save.getOrderCount()).isEqualTo(1);
    }

    @Test
    void 유저가_주문한_내역() {
        // given
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

        PageRequest pr = PageRequest.of(0,10);

        // when
        Page<Order> orderPage = orderService.findAllByMemberId(pr, save.getId());

        // then
        assertThat(orderPage.getContent()).isEqualTo(orderService.findAll());
        assertThat(orderPage.getTotalPages()).isEqualTo(1);
        assertThat(orderPage.getTotalElements()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORDER", "CANCEL"})
    void 유저가_주문한_내역_주문상태(OrderStatus status) {
        // given
        int statusCount = 10;
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        IntStream.range(0, statusCount).forEach(i -> {
            Order order = orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

            order.cancel();
        });

        IntStream.range(0, statusCount).forEach(i -> {
            orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());
        });

        PageRequest pr = PageRequest.of(0, 10);

        // when
        Page<Order> orderPage = orderService.findAllByMemberIdAndOrderStatus(pr, save.getId(), status);

        // then
        assertThat(orderPage.getTotalElements()).isEqualTo(statusCount);
    }

    @ParameterizedTest
    @ValueSource(strings = {"YES", "NO"})
    void 유저가_주문하고_결제한_내역(OrderPayStatus status) {
        // given
        int statusCount = 10;
        Member save = userRepository.save(member);
        cartRepository.save(Cart.builder().member(save).cartItems(menuList).build());

        IntStream.range(0, statusCount).forEach(i -> {
            Order order = orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());

            order.completePayment();
        });

        IntStream.range(0, statusCount).forEach(i -> {
            orderService.orderWithPessimisticLock(save.getId(), LocalDateTime.now());
        });

        PageRequest pr = PageRequest.of(0, 10);

        // when
        Page<Order> orderPage = orderService.findAllByMemberIdAndPayStatus(pr, save.getId(), status);

        // then
        assertThat(orderPage.getTotalElements()).isEqualTo(statusCount);
    }
}
