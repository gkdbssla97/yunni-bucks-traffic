package sejong.coffee.yun.facade;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonLockStockFacade {

    private final RedissonClient redissonClient;
    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final Calculator calculator;

    public Order order(Long id, LocalDateTime localDateTime) {
        Cart cart = cartRepository.findByMember(id);
        List<CartItem> cartItems = cart.getCartItems();

        cartItems.stream()
                .map(CartItem::getMenu)
                .forEach(menu -> {
                    decreaseStockWithRedissonLock(menu.getId(), 1);
                    increaseMenuOrderCountWithRedissonLock(menu.getId(), 1);
                });

        Money money = calculator.calculateMenus(cart.getMember(), cart.convertToMenus());

        Order order = Order.createOrder(cart, money, localDateTime);

        return orderRepository.save(order);
    }

    public void decreaseStockWithRedissonLock(Long menuId, int quantity) {
        RLock lock = redissonClient.getLock(menuId.toString());

        try {
            boolean available = lock.tryLock(15, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }

            orderService.decreaseStockWithRedissonLock(menuId, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    public void increaseMenuOrderCountWithRedissonLock(Long menuId, int quantity) {
        RLock lock = redissonClient.getLock(menuId.toString());

        try {
            boolean available = lock.tryLock(15, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }
            orderService.increaseMenuOrderCountWithRedissonLock(menuId, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
