package sejong.coffee.yun.unit.repository.order.fake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sejong.coffee.yun.domain.discount.condition.CouponCondition;
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
import sejong.coffee.yun.mock.repository.FakeCartItemRepository;
import sejong.coffee.yun.mock.repository.FakeOrderRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.mock.repository.FakeCartRepository;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;
import sejong.coffee.yun.repository.order.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class FakeOrderRepositoryTest {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final Calculator calculator;

    public FakeOrderRepositoryTest() {
        this.orderRepository = new FakeOrderRepository();
        this.cartRepository = new FakeCartRepository();
        this.cartItemRepository = new FakeCartItemRepository();
        calculator = new Calculator(new PercentPolicy(new RankCondition(), new CouponCondition()));
    }

    private Coupon coupon;
    private Member member;
    private Menu menu1;
    private List<CartItem> menuList;
    private Cart cart;

    @BeforeEach
    void init() {
        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        menu1 = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        coupon = Coupon.builder()
            .createAt(LocalDateTime.of(2023, 7, 29, 10, 10))
            .discountRate(0.1)
            .name("신규가입 쿠폰")
            .identityNumber("1234-1234-1234-1234")
            .couponUse(CouponUse.YES)
            .expireAt(LocalDateTime.of(2024, 7, 29, 10, 10))
            .build();

        member = Member.from(1L, Member.builder()
                .name("윤광오")
                .userRank(UserRank.SILVER)
                .money(Money.ZERO)
                .password("qwer1234")
                .address(null)
                .coupon(coupon)
                .email("qwer1234@naver.com")
                .orderCount(0)
                .build());

        CartItem save = cartItemRepository.save(CartItem.builder().menu(menu1).build());

        menuList = List.of(save);

        Cart c = Cart.builder()
                .member(member)
                .cartItems(menuList)
                .build();

        cart = cartRepository.save(c);
    }

    @Test
    void 주문_저장() {
        // given
        Money money = calculator.calculateMenus(member, cart.convertToMenus());

        Order order = Order.createOrder(member, cart, money, LocalDateTime.now());

        // when
        Order save = orderRepository.save(order);

        // then
        assertThat(save.getId()).isEqualTo(1L);
    }

    @Test
    void 주문_찾기() {
        // given
        Money money = calculator.calculateMenus(member, cart.convertToMenus());

        Order order = Order.createOrder(member, cart, money, LocalDateTime.now());
        Order save = orderRepository.save(order);

        // when
        Order findOrder = orderRepository.findById(save.getId());

        // then
        assertThat(findOrder).isEqualTo(save);
    }

    @Test
    void 주문_리스트() {
        // given
        Money money = calculator.calculateMenus(member, cart.convertToMenus());

        Order order = Order.createOrder(member, cart, money, LocalDateTime.now());
        orderRepository.save(order);

        // when
        List<Order> orderList = orderRepository.findAll();

        // then
        assertThat(orderList.size()).isEqualTo(1);
    }

    @Test
    void 주문_시간() {
        // given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 11, 5, 30);

        Money money = calculator.calculateMenus(member, cart.convertToMenus());

        Order order = Order.createOrder(member, cart, money, localDateTime);

        // when
        Order save = orderRepository.save(order);

        // then
        assertThat(save.getCreateAt()).isEqualTo(localDateTime);
    }

    @Test
    void 주문_수정_시간() {
        // given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 11, 5, 30);

        Money money = calculator.calculateMenus(member, cart.convertToMenus());

        Order order = Order.createOrder(member, cart, money, localDateTime);

        // when
        LocalDateTime updateTime = LocalDateTime.of(2023, 9, 11, 5, 11);
        order.setUpdateAt(updateTime);

        // then
        assertThat(order.getUpdateAt()).isEqualTo(updateTime);
    }

    @Test
    void 유저가_주문한_내역() {
        // given
        Order order = Order.createOrder(member, cart, Money.ZERO, LocalDateTime.now());

        IntStream.range(0, 20).forEach(i -> orderRepository.save(order));

        PageRequest pr = PageRequest.of(1, 10);

        // when
        Page<Order> orders = orderRepository.findAllByMemberIdAndOrderStatus(pr, 1L, OrderStatus.ORDER);

        // then
        assertThat(orders.getTotalPages()).isEqualTo(2);
        assertThat(orders.getTotalElements()).isEqualTo(20);
    }

    @Test
    void 유저가_주문_취소한_내역() {
        // given
        Order order = Order.createOrder(member, cart, Money.ZERO, LocalDateTime.now());
        order.cancel();

        IntStream.range(0, 20).forEach(i -> orderRepository.save(order));

        PageRequest pr = PageRequest.of(1, 10);

        // when
        Page<Order> orders = orderRepository.findAllByMemberIdAndOrderStatus(pr, 1L, OrderStatus.CANCEL);

        // then
        assertThat(orders.getTotalPages()).isEqualTo(2);
        assertThat(orders.getTotalElements()).isEqualTo(20);
    }

    @Test
    void 유저가_주문하고_결제한_내역() {
        // given
        Order order = Order.createOrder(member, cart, Money.ZERO, LocalDateTime.now());
        order.completePayment();

        IntStream.range(0, 20).forEach(i -> orderRepository.save(order));

        PageRequest pr = PageRequest.of(1, 10);

        // when
        Page<Order> orders = orderRepository.findAllByMemberIdAndPayStatus(pr, 1L, OrderPayStatus.YES);

        // then
        assertThat(orders.getTotalPages()).isEqualTo(2);
        assertThat(orders.getTotalElements()).isEqualTo(20);
    }
}