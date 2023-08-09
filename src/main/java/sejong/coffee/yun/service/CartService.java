package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public Cart createCart(Long memberId) {

        Member member = userRepository.findById(memberId);

        Cart cart = new Cart(member, new ArrayList<>());

        return cartRepository.save(cart);
    }

    public Cart findCartByMember(Long memberId) {
        return cartRepository.findByMember(memberId);
    }

    @Transactional
    public Cart addMenu(Long memberId, Long menuId) {
        Menu menu = menuRepository.findById(menuId);
        Cart cart = cartRepository.findByMember(memberId);

        cart.addMenu(menu);

        return cart;
    }

    public Menu getMenu(Long memberId, int idx) {
        Cart cart = cartRepository.findByMember(memberId);

        return cart.getMenu(idx);
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId);

        cart.clearMenuList();
    }

    @Transactional
    public Cart removeMenu(Long memberId, int idx) {
        Cart cart = cartRepository.findByMember(memberId);

        cart.removeMenu(idx);

        return cart;
    }

    @Transactional
    public void removeCart(Long cartId) {
        cartRepository.delete(cartId);
    }
}