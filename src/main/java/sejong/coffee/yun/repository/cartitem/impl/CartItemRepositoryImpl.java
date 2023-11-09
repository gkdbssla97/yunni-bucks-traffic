package sejong.coffee.yun.repository.cartitem.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;
import sejong.coffee.yun.repository.cartitem.jpa.JpaCartItemRepository;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepository {

    private final JpaCartItemRepository jpaCartItemRepository;

    @Override
    @Transactional
    public CartItem save(CartItem cartItem) {
        return jpaCartItemRepository.save(cartItem);
    }

    @Override
    public List<CartItem> findAll() {
        return jpaCartItemRepository.findAll();
    }

    @Override
    public void clear() {
        jpaCartItemRepository.deleteAll();
    }
}
