package sejong.coffee.yun.facade;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonLockFacade {

    private final RedissonClient redissonClient;
    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final Calculator calculator;
    private final CacheManager cacheManager;

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
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("재고감소 위한 메뉴 접근 LOCK 획득 실패");
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
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("주문수 증가 위한 메뉴 접근 LOCK 획득 실패");
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

    public MenuDto.Response findMenuFromCache(String menuTitle) {
        MenuDto.Response cachedMenu = null;
        RLock lock = redissonClient.getLock("MenuLock");

        // 먼저 캐시에서 메뉴를 조회
        cachedMenu = Objects.requireNonNull(cacheManager.getCache("Menu")).get(menuTitle, MenuDto.Response.class);

        // 캐시에 메뉴가 없을 경우에만 RDB에서 메뉴를 조회하고 캐시에 저장
        if (cachedMenu == null) {
            try {
                boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
                if (!available) {
                    System.out.println("메뉴 접근 LOCK 획득 실패");
                    return null;
                }
                Menu findMenu = menuRepository.findByTitle(menuTitle);
                cachedMenu = MenuDto.Response.fromMenu(findMenu);
                Objects.requireNonNull(cacheManager.getCache("Menu")).put(menuTitle, cachedMenu);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        return cachedMenu;
    }
}
