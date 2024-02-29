package sejong.coffee.yun.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.exception.NotFoundException;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU;

class CartTest {

    private Member member;
    private CartItem menu1;

    @BeforeEach
    void init() {

        String city = "서울시";
        String district = "강서구";
        String detail = "목동";
        String zipcode = "123-123";

        Address address = new Address(city, district, detail, zipcode);

        member = Member.builder()
            .address(address)
            .email("qwer1234@naver.com")
            .money(Money.ZERO)
            .userRank(UserRank.BRONZE)
            .password("qwer1234")
            .name("윤광오")
            .orderCount(0)
            .build();

        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        Menu menu = Beverage.builder()
                .id(1L)
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        menu1 = CartItem.builder().menu(menu).build();
    }

    @Test
    void 카트에_메뉴를_추가한다() {
        // given
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        // when
        cart.addMenu(menu1);

        // then
        assertThat(cart.getCartItems().get(0)).isEqualTo(menu1);
    }

    @Test
    void 카트에_추가된_메뉴가_몇개인지_센다() {
        // given
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        // when
        cart.addMenu(menu1);

        // then
        assertThat(cart.getCartItems().size()).isEqualTo(1);
    }

    @Test
    void 카트에_메뉴를_삭제한다() {
        // given
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        cart.addMenu(menu1);

        // when
        cart.removeMenu(0);

        // then
        assertThat(cart.getCartItems().size()).isEqualTo(0);
    }

    @Test
    void 메뉴를_삭제할때_메뉴를_찾을_수_없을때_예외() {
        // given
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        cart.addMenu(menu1);

        // when

        // then
        assertThatThrownBy(() -> cart.removeMenu(3))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(NOT_FOUND_MENU.getMessage());
    }

    @Test
    void 카트에_있는_메뉴를_주문하고_주문한_메뉴와_카트_메뉴들을_비교() {
        // given
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        cart.addMenu(menu1);

        // when
        Order order = Order.createOrder(member, cart, Money.ZERO, LocalDateTime.now());

        // then
        assertThat(order.getCart().getCartItems()).isEqualTo(cart.getCartItems());
    }

    @Test
    void 카트가_생성되지않고_메뉴를_추가_제거같은_행위를_할_경우() {
        // given
        Cart cart = Cart.builder()
                .member(member)
                .cartItems(null)
                .build();

        // when

        // then
        assertThatThrownBy(() -> cart.addMenu(menu1))
                .isInstanceOf(NullPointerException.class);
    }
}