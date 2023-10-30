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
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.repository.cart.CartRepository;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order order(Long memberId, LocalDateTime now) {

        Cart cart = cartRepository.findByMember(memberId);

        List<CartItem> cartItems = cart.getCartItems();
        cartItems.stream()
                .map(CartItem::getMenu)
                .forEach(menu -> decreaseStock(menu.getId(), 1));

        Money money = calculator.calculateMenus(cart.getMember(), cart.convertToMenus());

        Order order = Order.createOrder(cart, money, now);

        return orderRepository.save(order);
    }

    public void decreaseStock(Long menuId, int quantity) {
        Menu menu = menuRepository.findById(menuId);
        menu.decrease(quantity);
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId);

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
