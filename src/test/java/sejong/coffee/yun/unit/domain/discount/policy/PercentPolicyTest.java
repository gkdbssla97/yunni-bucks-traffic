package sejong.coffee.yun.unit.domain.discount.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sejong.coffee.yun.domain.discount.condition.CouponCondition;
import sejong.coffee.yun.domain.discount.condition.RankCondition;
import sejong.coffee.yun.domain.discount.policy.DiscountPolicy;
import sejong.coffee.yun.domain.discount.policy.PercentPolicy;
import sejong.coffee.yun.domain.user.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PercentPolicyTest {
    private final DiscountPolicy discountPolicy;

    public PercentPolicyTest() {
        this.discountPolicy = new PercentPolicy(new RankCondition(), new CouponCondition());
    }

    @ParameterizedTest
    @CsvSource({"BRONZE, 0", "SILVER, 0.1", "GOLD, 0.15", "PLATINUM, 0.2", "DIAMOND, 0.3"})
    void 할인_정책_등급_할인(UserRank userRank, double rankDiscountPercent) {
        // given
        Member member = Member.builder()
                .userRank(userRank)
                .money(Money.ZERO)
                .build();

        // when
        double percent = discountPolicy.calculateDiscount(member);

        // then
        assertThat(percent).isEqualTo(rankDiscountPercent);
    }

    @Test
    void 다양한_할인조건의_총_할인률() {
        // given
        Coupon coupon = Coupon.builder()
                .createAt(LocalDateTime.of(2023, 7, 29, 10, 10))
                .discountRate(0.1)
                .name("신규가입 쿠폰")
                .identityNumber("1234-1234-1234-1234")
                .couponUse(CouponUse.NO)
                .expireAt(LocalDateTime.of(2024, 7, 29, 10, 10))
                .build();

        Member member = Member.builder()
                .userRank(UserRank.SILVER)
                .money(Money.ZERO)
                .coupon(coupon)
                .build();

        // when
        double discount = discountPolicy.calculateDiscount(member);

        // then
        assertThat(discount).isEqualTo(0.2);
    }
}