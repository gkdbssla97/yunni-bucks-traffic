package sejong.coffee.yun.unit.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sejong.coffee.yun.domain.discount.condition.RankCondition;
import sejong.coffee.yun.domain.discount.policy.PercentPolicy;
import sejong.coffee.yun.domain.order.Calculator;
import sejong.coffee.yun.domain.order.menu.*;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.domain.user.UserRank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculatorTest {

    private final Calculator calculator;

    public CalculatorTest() {
        this.calculator = new Calculator(new PercentPolicy(new RankCondition()));
    }

    private Menu menu1;
    private Menu menu2;
    private Menu menu3;

    @BeforeEach
    void init() {
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
    }

    @ParameterizedTest
    @CsvSource({"BRONZE, 3000", "SILVER, 2700", "GOLD, 2550", "PLATINUM, 2400", "DIAMOND, 2100"})
    void 메뉴리스트를_계산_한다(UserRank userRank, BigDecimal bigDecimal) {
        // given
        Member member = Member.builder()
                .address(null)
                .email("qwer1234@naver.com")
                .money(Money.ZERO)
                .userRank(userRank)
                .password("qwer1234")
                .name("윤광오")
                .build();

        List<Menu> menuList = List.of(menu1, menu2, menu3);

        // when
        Money money = calculator.calculateMenus(member, menuList);

        Money decimalToLong = money.mapBigDecimalToLong();

        // then
        assertThat(decimalToLong.getTotalPrice()).isEqualTo(bigDecimal);
    }

    @Test
    void 메뉴_리스트가_비어있으면_0으로_반환한다() {
        // given
        Member member = Member.builder()
                .address(null)
                .email("qwer1234@naver.com")
                .money(Money.ZERO)
                .password("qwer1234")
                .name("윤광오")
                .build();

        List<Menu> menuList = new ArrayList<>();

        // when
        Money money = calculator.calculateMenus(member, menuList);

        // then
        assertThat(money.getTotalPrice()).isEqualTo(Money.ZERO.getTotalPrice());
    }
}