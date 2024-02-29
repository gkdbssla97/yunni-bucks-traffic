package sejong.coffee.yun.domain.pay;

import sejong.coffee.yun.domain.discount.condition.CouponCondition;
import sejong.coffee.yun.domain.discount.condition.RankCondition;
import sejong.coffee.yun.domain.discount.policy.PercentPolicy;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BeforeCreatedData {

    protected final Order order;
    protected final Member member;
    protected final Calculator calculator;
    protected final Card card;
    protected final List<CartItem> menuList;
    protected final Money money;
    public BeforeCreatedData() {

        this.calculator = new Calculator(new PercentPolicy(new RankCondition(), new CouponCondition()));

        String city = "서울시";
        String district = "강남구";
        String detail = "서초동";
        String zipcode = "123-123";

        Address address = new Address(city, district, detail, zipcode);

        Menu menu1;
        Menu menu2;
        Menu menu3;

        Coupon coupon;

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

        this.member = Member.builder()
                .address(address)
                .email("hy97@sju.ac.kr")
                .money(Money.ZERO)
                .userRank(UserRank.BRONZE)
                .password("19013141")
                .name("하윤")
                .orderCount(0)
                .build();

        this.card = new Card("1234123443211239", "10/24", "1234", this.member);

        CartItem c1 = CartItem.builder()
                .menu(menu1)
                .build();

        CartItem c2 = CartItem.builder()
                .menu(menu1)
                .build();

        CartItem c3 = CartItem.builder()
                .menu(menu1)
                .build();

        menuList = List.of(c1, c2, c3);

        Cart cart = Cart.builder()
                .member(member)
                .cartItems(menuList)
                .build();

        money = calculator.calculateMenus(member, cart.convertToMenus());

        this.order = Order.createOrder(member, cart, money, LocalDateTime.now());
    }
}
