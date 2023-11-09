package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.CartItem;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import java.util.ArrayList;

import static sejong.coffee.yun.domain.exception.ExceptionControl.DUPLICATE;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Cart createCart(Long memberId) {

        if(cartRepository.existByMemberId(memberId)) throw DUPLICATE.duplicatedException();

        Member member = userRepository.findById(memberId);

        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        return cartRepository.save(cart);
    }

    public Cart findCartByMember(Long memberId) {
        return cartRepository.findByMember(memberId);
    }

    @Transactional
    public Cart addMenu(Long memberId, Long menuId) {
        Menu menu = menuRepository.findById(menuId);
        Cart cart = cartRepository.findByMember(memberId);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .menu(menu)
                .build();

        CartItem saveCartItem = cartItemRepository.save(cartItem);

        cart.addMenu(saveCartItem);


        cart.getCartItems().forEach(CartItem::getId);

        return cart;
    }

    public Menu getMenu(Long memberId, int idx) {
        Cart cart = cartRepository.findByMember(memberId);

        return cart.getMenu(idx);
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId);

        cart.clearCartItems();
    }

    @Transactional
    public Cart removeMenu(Long memberId, int idx) {
        Cart cart = cartRepository.findByMember(memberId);

        cart.removeMenu(idx);

        return cart;
    }

    @Transactional
    public void removeCart(Long memberId) {
        Cart cart = cartRepository.findByMember(memberId);

        cartRepository.delete(cart);
    }
}
