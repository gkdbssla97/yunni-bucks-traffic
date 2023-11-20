package sejong.coffee.yun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.OrderPayStatus;
import sejong.coffee.yun.domain.order.OrderStatus;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Disabled("mock test disabled")
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;
    @Mock
    private Calculator calculator;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MenuRepository menuRepository;

    private Member member;
    private Order order;
    private List<CartItem> menuList = new ArrayList<>();
    private Menu menu1;
    private Cart cart;

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

        menu1 = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        menuList.add(CartItem.builder().menu(menu1).build());

        cart = Cart.builder()
                .member(member)
                .cartItems(menuList)
                .build();

        order = Order.createOrder(member, cart, Money.initialPrice(new BigDecimal("10000")), LocalDateTime.now());
    }

    @Test
    void 주문() {
        // given
        given(orderRepository.save(any())).willReturn(order);
        given(userRepository.findById(any())).willReturn(member);

        // when
        Order saveOrder = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

        // then
        assertThat(saveOrder).isEqualTo(order);
    }

    @Test
    void 유저가_주문한_시간() {
        // given
        Order order1 = Order.createOrder(member, cart, Money.ZERO,
                LocalDateTime.of(2023, 8, 11, 5, 11));

        given(orderRepository.save(any())).willReturn(order1);
        given(userRepository.findById(any())).willReturn(member);

        // when
        Order order2 = orderService.orderWithPessimisticLock(1L, LocalDateTime.of(2023, 8, 11, 5, 11));

        // then
        assertThat(order2.getCreateAt()).isEqualTo(order1.getCreateAt());
    }

    @Test
    void 주문을_조회한다() {
        // given
        given(orderRepository.findById(any())).willReturn(order);

        // when
        Order findOrder = orderService.findOrder(order.getId());

        // then
        assertThat(findOrder).isEqualTo(order);
    }

    @Test
    void 주문_리스트_조회() {
        // given
        given(orderRepository.findAll()).willReturn(List.of(order));

        // when
        List<Order> orders = orderService.findAll();

        // then
        assertThat(orders).isEqualTo(List.of(order));
    }

    @Test
    void 주문_총_금액_확인() {
        // given
        given(orderRepository.save(any())).willReturn(order);
        given(userRepository.findById(any())).willReturn(member);
        given(calculator.calculateMenus(any(), any())).willReturn(Money.initialPrice(new BigDecimal("10000")));

        // when
        Order saveOrder = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

        // then
        assertThat(saveOrder.fetchTotalOrderPrice()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    void 주문명_확인() {
        // given
        given(orderRepository.save(any())).willReturn(order);
        given(userRepository.findById(any())).willReturn(member);

        // when
        Order saveOrder = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

        // then
        assertThat(saveOrder.getName()).isEqualTo("커피 외 1개");
    }

    @Test
    void 유저가_주문_하고_주문_개수_확인() {
        // given
        given(orderRepository.save(any())).willReturn(order);
        given(userRepository.findById(any())).willReturn(member);

        // when
        Order saveOrder = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

        // then
        assertThat(saveOrder.getMember().getOrderCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORDER", "CANCEL"})
    void 유저가_주문한_내역_주문_상태(OrderStatus status) {
        // given
        PageRequest pr = PageRequest.of(0, 10);
        Order order1 = Order.createOrder(member, cart, Money.ZERO, LocalDateTime.now());
        List<Order> orders = List.of(order1);

        PageImpl<Order> orderPage = new PageImpl<>(orders, pr, orders.size());

        given(orderRepository.findAllByMemberIdAndOrderStatus(any(), anyLong(), any())).willReturn(orderPage);

        // when
        Page<Order> all = orderService.findAllByMemberIdAndOrderStatus(pr, 1L, status);

        // then
        assertThat(all.getTotalElements()).isEqualTo(1);
        assertThat(all.getTotalPages()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"YES", "NO"})
    void 유저가_주문하고_결제한_내역_결제_상태(OrderPayStatus status) {
        // given
        PageRequest pr = PageRequest.of(0, 10);
        Order order1 = Order.createOrder(member, cart, Money.ZERO, LocalDateTime.now());
        order1.completePayment();

        List<Order> orders = List.of(order1);

        PageImpl<Order> orderPage = new PageImpl<>(orders, pr, orders.size());

        given(orderRepository.findAllByMemberIdAndPayStatus(any(), anyLong(), any())).willReturn(orderPage);

        // when
        Page<Order> all = orderService.findAllByMemberIdAndPayStatus(pr, 1L, status);

        // then
        assertThat(all.getTotalElements()).isEqualTo(1);
        assertThat(all.getTotalPages()).isEqualTo(1);
    }
}