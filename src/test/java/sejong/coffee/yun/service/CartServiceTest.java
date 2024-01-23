package sejong.coffee.yun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sejong.coffee.yun.domain.exception.NotFoundException;
import sejong.coffee.yun.domain.order.menu.*;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.cartitem.CartItemRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_CART;
import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU;

@ExtendWith(MockitoExtension.class)
@Disabled("mock test disabled")
class CartServiceTest {

    @InjectMocks
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    private Member member;
    private Menu menu1;
    private Menu menu2;
    private Menu menu3;
    private Cart cart;

    @BeforeEach
    void init() {
        member = Member.builder()
                .name("윤광오")
                .userRank(UserRank.BRONZE)
                .money(Money.ZERO)
                .password("qwer1234")
                .address(null)
                .email("qwer1234@naver.com")
                .build();

        cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        Beverage beverage = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        menu1 = beverage;
        menu2 = beverage;
        menu3 = beverage;

        cart.addMenu(CartItem.builder().menu(menu1).build());
        cart.addMenu(CartItem.builder().menu(menu2).build());
    }

    @Test
    void 카트_생성() {
        // given
        given(userRepository.findById(anyLong())).willReturn(member);
        given(cartRepository.save(any())).willReturn(cart);

        // when
        Cart saveCart = cartService.createCart(1L);

        // then
        assertThat(saveCart).isEqualTo(cart);
    }

    @Test
    void 메뉴_추가() {
        // given
        Cart cart1 = mock(Cart.class);

        given(cartRepository.findByMember(anyLong())).willReturn(cart1);
        given(menuRepository.findById(anyLong())).willReturn(menu1);

        // when
        cartService.addMenu(1L, 1L);

        // then
        then(cart1).should().addMenu(CartItem.builder().menu(menu1).build());
    }

    @Test
    void 유저의_카트를_찾을_수_없을_때() {
        // given
        given(cartRepository.findByMember(anyLong())).willThrow(NOT_FOUND_CART.notFoundException());

        // when

        // then
        assertThatThrownBy(() -> cartService.getCartByMember(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(NOT_FOUND_CART.getMessage());
    }

    @Test
    void 카트에서_메뉴_찾기() {
        // given
        given(cartRepository.findByMember(anyLong())).willReturn(cart);

        // when
        Menu menu = cartService.getMenu(1L, 0);

        // then
        assertThat(menu).isEqualTo(menu1);
    }

    @Test
    void 카트에서_메뉴_지우기() {
        // given
        Cart c = mock(Cart.class);

        given(cartRepository.findByMember(anyLong())).willReturn(c);

        // when
        cartService.removeMenu(1L, 0);

        // then
        then(c).should().removeMenu(0);
    }

    @Test
    void 카트에_있는_메뉴를_찾을_수_없을때() {
        // given
        given(menuRepository.findById(anyLong())).willThrow(NOT_FOUND_MENU.notFoundException());

        // when

        // then
        assertThatThrownBy(() -> cartService.addMenu(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(NOT_FOUND_MENU.getMessage());
    }
}