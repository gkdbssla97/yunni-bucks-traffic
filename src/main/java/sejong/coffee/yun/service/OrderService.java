package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.OrderPayStatus;
import sejong.coffee.yun.domain.order.OrderStatus;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.domain.user.Coupon;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.coupon.CouponRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.order.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final Calculator calculator;
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final CouponRepository couponRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order orderWithPessimisticLock(Long memberId, LocalDateTime now) {

        Cart cart = cartRepository.findByMember(memberId);

        List<CartItem> cartItems = cart.getCartItems();

        cartItems.stream()
                .map(CartItem::getMenu)
                .forEach(menu -> {
                    decreaseStockWithPessimisticLock(menu.getId(), 1);
                    increaseMenuOrderCountWithPessimisticLock(menu.getId(), 1);
                });


        Money money = calculator.calculateMenus(cart.getMember(), cart.convertToMenus());

        Order order = Order.createOrder(cart, money, now);

        return orderRepository.save(order);
    }

    public void decreaseStockWithPessimisticLock(Long menuId, int quantity) {
        Menu menu = menuRepository.findByIdForPessimisticLock(menuId);
        menu.decrease(quantity);
    }

    public void increaseMenuOrderCountWithPessimisticLock(Long menuId, int quantity) {
        Menu menu = menuRepository.findByIdForPessimisticLock(menuId);
        menu.increaseOrderCount(quantity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order orderWithOptimisticLock(Long memberId, LocalDateTime now) {

        Cart cart = cartRepository.findByMember(memberId);

        List<CartItem> cartItems = cart.getCartItems();

        cartItems.stream()
                .map(CartItem::getMenu)
                .forEach(menu -> {
                    decreaseStockWithOptimisticLock(menu.getId(), 1);
                    increaseMenuOrderCountWithOptimisticLock(menu.getId(), 1);
                });

        Money money = calculator.calculateMenus(cart.getMember(), cart.convertToMenus());

        Order order = Order.createOrder(cart, money, now);

        return orderRepository.save(order);
    }

    public void decreaseStockWithOptimisticLock(Long menuId, int quantity) {
        Menu menu = menuRepository.findByIdForOptimisticLock(menuId);
        menu.decrease(quantity);
    }

    public void increaseMenuOrderCountWithOptimisticLock(Long menuId, int quantity) {
        Menu menu = menuRepository.findByIdForOptimisticLock(menuId);
        menu.increaseOrderCount(quantity);
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId);
        Coupon coupon = couponRepository.findByOrderId(orderId);
        coupon.isAvailableCoupon();
        order.cancel();
    }

    @Deprecated
    public Order findOrderByMemberId(Long memberId) {
        return orderRepository.findByMemberId(memberId);
    }

    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Page<Order> findAllByMemberId(Pageable pageable, Long memberId) {
        return orderRepository.findAllByMemberId(pageable, memberId);
    }

    public Page<Order> findAllByMemberIdAndOrderStatus(Pageable pageable, Long memberId, OrderStatus status) {
        return orderRepository.findAllByMemberIdAndOrderStatus(pageable, memberId, status);
    }

    public Page<Order> findAllByMemberIdAndPayStatus(Pageable pageable, Long memberId, OrderPayStatus status) {
        return orderRepository.findAllByMemberIdAndPayStatus(pageable, memberId, status);
    }
}
