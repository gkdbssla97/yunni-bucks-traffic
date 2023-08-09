package sejong.coffee.yun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sejong.coffee.yun.domain.exception.NotFoundException;
import sejong.coffee.yun.domain.order.menu.*;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.domain.user.UserRank;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_CART;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MenuRepository menuRepository;

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

        cart = new Cart(member, new ArrayList<>());

        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        menu1 = new Beverage("커피", "에티오피아산 커피",
                Money.initialPrice(new BigDecimal(1000)), nutrients, MenuSize.M);
        menu2 = new Beverage("아이스티", "복숭아 아이스티",
                Money.initialPrice(new BigDecimal(1000)), nutrients, MenuSize.M);
        menu3 = new Bread("소라빵", "소라빵",
                Money.initialPrice(new BigDecimal(1000)), nutrients, MenuSize.M);

        cart.addMenu(menu1);
        cart.addMenu(menu2);
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
        then(cart1).should().addMenu(menu1);
    }

    @Test
    void 유저의_카트를_찾을_수_없을_때() {
        // given
        given(cartRepository.findByMember(anyLong())).willThrow(NOT_FOUND_CART.notFoundException());

        // when

        // then
        assertThatThrownBy(() -> cartService.findCartByMember(1L))
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
    void 카트를_찾을_수_없을때() {
        // given
        given(cartRepository.findByMember(anyLong())).willThrow(NOT_FOUND_CART.notFoundException());
        given(menuRepository.findById(anyLong())).willReturn(menu1);

        // when

        // then
        assertThatThrownBy(() -> cartService.addMenu(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(NOT_FOUND_CART.getMessage());
    }
}