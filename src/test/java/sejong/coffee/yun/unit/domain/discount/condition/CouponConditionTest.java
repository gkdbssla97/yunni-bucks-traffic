package sejong.coffee.yun.unit.domain.discount.condition;

import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.discount.condition.CouponCondition;
import sejong.coffee.yun.domain.discount.condition.DiscountCondition;
import sejong.coffee.yun.domain.user.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponConditionTest {

    private final DiscountCondition condition;

    public CouponConditionTest() {
        this.condition = new CouponCondition();
    }

    @Test
    void 사용가능한_쿠폰을_가지고_있다() {
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
        boolean availableCoupon = condition.isSatisfiedBy(member);

        // then
        assertTrue(availableCoupon);
    }

    @Test
    void 사용불가능한_쿠폰을_가지고_있다() {
        // given
        Coupon coupon = Coupon.builder()
                .createAt(LocalDateTime.of(2023, 7, 29, 10, 10))
                .discountRate(0.1)
                .name("신규가입 쿠폰")
                .identityNumber("1234-1234-1234-1234")
                .couponUse(CouponUse.YES)
                .expireAt(LocalDateTime.of(2024, 7, 29, 10, 10))
                .build();

        Member member = Member.builder()
                .userRank(UserRank.SILVER)
                .money(Money.ZERO)
                .coupon(coupon)
                .build();

        // when
        boolean notAvailableCoupon = condition.isSatisfiedBy(member);

        // then
        assertFalse(notAvailableCoupon);
    }
}