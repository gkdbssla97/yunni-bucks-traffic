package sejong.coffee.yun.repository.cartitem;

import sejong.coffee.yun.domain.user.CartItem;

import java.util.List;

public interface CartItemRepository {

    CartItem save(CartItem cartItem);
    List<CartItem> findAll();
    void clear();
}
