package sejong.coffee.yun.domain.discount.policy;

import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.discount.condition.RankCondition;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.domain.user.User;
import sejong.coffee.yun.domain.user.UserRank;

import static org.assertj.core.api.Assertions.assertThat;

class AmountPolicyTest {
    private final DiscountPolicy discountPolicy;

    public AmountPolicyTest() {
        this.discountPolicy = new AmountPolicy(new RankCondition());
    }

    @Test
    void 첫_주문_할인_일정_금액을_할인한다() {
        // given
        User user = User.builder()
                .userRank(UserRank.BRONZE)
                .money(Money.ZERO)
                .build();

        // when
        double discount = discountPolicy.calculateDiscount(user);

        // then
        assertThat(discount).isEqualTo(1000);
    }

    @Test
    void 첫_주문이_아닌경우_첫_주문할인_적용_안됨() {
        // given
        User user = User.builder()
                .userRank(UserRank.SILVER)
                .money(Money.ZERO)
                .build();

        // when
        double discount = discountPolicy.calculateDiscount(user);

        // then
        assertThat(discount).isEqualTo(0);
    }
}