package sejong.coffee.yun.domain.order;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.domain.user.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static sejong.coffee.yun.domain.exception.ExceptionControl.DO_NOT_PAID;
import static sejong.coffee.yun.domain.exception.ExceptionControl.EMPTY_MENUS;
import static sejong.coffee.yun.domain.order.OrderStatus.CANCEL;
import static sejong.coffee.yun.domain.order.OrderStatus.ORDER;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue
    private Long id;
    @Column(name = "order_name")
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;
    private Money orderPrice;
    @Enumerated(value = EnumType.STRING)
    private OrderPayStatus payStatus;
    @Column(name = "create_at")
    private LocalDateTime createAt;
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Builder
    public Order(Long id, String name, Cart cart, OrderStatus status, Money orderPrice, OrderPayStatus payStatus, LocalDateTime createAt, LocalDateTime updateAt) {
        this.id = id;
        this.name = name;
        this.cart = cart;
        this.status = status;
        this.orderPrice = orderPrice;
        this.payStatus = payStatus;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public static Order from(Long id, Order order) {
        return Order.builder()
                .id(id)
                .name(order.getName())
                .orderPrice(order.getOrderPrice())
                .createAt(order.getCreateAt())
                .cart(order.getCart())
                .updateAt(order.getUpdateAt())
                .status(order.getStatus())
                .payStatus(order.getPayStatus())
                .build();
    }

    @Deprecated
    public static Order createOrder(Member member, Cart cart, Money orderPrice, LocalDateTime now) {
        String orderName = makeOrderName(cart.getCartItems());

        if(member.getCoupon() != null) {
            member.getCoupon().convertStatusUsedCoupon();
        }

        cart.getMember().addOrderCount();

        return Order.builder()
                .name(orderName)
                .cart(cart)
                .status(ORDER)
                .payStatus(OrderPayStatus.NO)
                .orderPrice(orderPrice)
                .createAt(now)
                .updateAt(now)
                .build();
    }

    public static Order createOrder(Cart cart, Money orderPrice, LocalDateTime now) {
        String orderName = makeOrderName(cart.getCartItems());

        if(cart.getMember().hasCoupon()) {
            cart.getMember().getCoupon().convertStatusUsedCoupon();
        }

        cart.getMember().addOrderCount();

        return Order.builder()
                .name(orderName)
                .cart(cart)
                .status(ORDER)
                .payStatus(OrderPayStatus.NO)
                .orderPrice(orderPrice)
                .createAt(now)
                .updateAt(now)
                .build();
    }

    public void completePayment() {
        this.payStatus = OrderPayStatus.YES;
    }

    public void cancel() {
        this.status = CANCEL;
    }

    public BigDecimal fetchTotalOrderPrice() {
        return this.orderPrice.getTotalPrice();
    }

    private static String makeOrderName(List<CartItem> cartItems) {
        if(cartItems.size() == 0) {
            throw EMPTY_MENUS.throwException();
        } else {
            String title = cartItems.get(0).getMenu().getTitle();

            return title + " 외" + " " + cartItems.size() + "개";
        }
    }

    @Deprecated
    public void updatePrice(Money money) {
        this.orderPrice = money;
    }

    public void setUpdateAt(LocalDateTime now) {
        this.updateAt = now;
    }
    public void setPayStatus() {
        this.payStatus = OrderPayStatus.YES;
    }

    public String mapOrderName() {
        return this.id + "00000";
    }

    public void checkOrderPayStatus() {
        if(!Objects.equals(this.payStatus, OrderPayStatus.YES)) {
            throw new IllegalArgumentException(DO_NOT_PAID.getMessage());
        }
    }

    public Member getMember() {
        return this.cart.getMember();
    }
}
