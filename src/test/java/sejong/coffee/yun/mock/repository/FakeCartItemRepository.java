package sejong.coffee.yun.mock.repository;

import org.springframework.stereotype.Repository;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FakeCartItemRepository implements CartItemRepository {

    private final List<CartItem> cartItems = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong id = new AtomicLong(0);

    @Override
    public CartItem save(CartItem cartItem) {
        if(cartItem.getId() == null || cartItem.getId() == 0L) {

            CartItem newCartItem = CartItem.from(id.incrementAndGet(), cartItem);
            cartItems.add(newCartItem);
            return newCartItem;
        }
        cartItems.removeIf(c -> Objects.equals(c.getId(), cartItem.getId()));
        cartItems.add(cartItem);
        return cartItem;
    }

    @Override
    public List<CartItem> findAll() {
        return cartItems;
    }

    @Override
    public void clear() {
        cartItems.clear();
    }
}
