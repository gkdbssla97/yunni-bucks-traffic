package sejong.coffee.yun.unit.domain.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.user.Coupon;
import sejong.coffee.yun.domain.user.CouponUse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    void 쿠폰_식별번호_예외_확인() {
        assertThatThrownBy(() -> Coupon.builder()
                .identityNumber("12345-31243-543534")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("쿠폰 식별번호 형식에 맞지 않습니다.");

    }

    @Test
    void 쿠폰이_만료기간을_넘었을때_쿠폰사용_상태_변경_확인() {
        // given
        Coupon coupon = Coupon.builder()
                .identityNumber("1234-1234-1234-1234")
                .couponUse(CouponUse.NO)
                .createAt(LocalDateTime.of(2023, 7, 29, 3, 56))
                .expireAt(LocalDateTime.of(2023, 7, 30, 3, 56))
                .build();

        // when
        coupon.checkExpireTime(LocalDateTime.of(2024, 7, 29,3, 56));

        // then
        assertThat(coupon.getCouponUse()).isEqualTo(CouponUse.YES);
    }

    @Test
    void 쿠폰_상태_변경() {
        // given
        Coupon coupon = Coupon.builder()
                .identityNumber("1234-1234-1234-1234")
                .couponUse(CouponUse.NO)
                .createAt(LocalDateTime.of(2023, 7, 29, 3, 56))
                .expireAt(LocalDateTime.of(2023, 7, 30, 3, 56))
                .build();

        // when
        coupon.convertStatusUsedCoupon();

        // then
        assertThat(coupon.getCouponUse()).isEqualTo(CouponUse.YES);
    }

    @Test
    void 쿠폰이_사용_가능한지_확인() {
        // given
        Coupon coupon = Coupon.builder()
                .identityNumber("1234-1234-1234-1234")
                .couponUse(CouponUse.NO)
                .createAt(LocalDateTime.of(2023, 7, 29, 3, 56))
                .expireAt(LocalDateTime.of(2023, 7, 30, 3, 56))
                .build();

        // when
        boolean availableCoupon = coupon.hasAvailableCoupon();

        // then
        assertTrue(availableCoupon);
    }

    @Test
    void 쿠폰이_사용_불가능한지_확인() {
        // given
        Coupon coupon = Coupon.builder()
                .identityNumber("1234-1234-1234-1234")
                .couponUse(CouponUse.YES)
                .createAt(LocalDateTime.of(2023, 7, 29, 3, 56))
                .expireAt(LocalDateTime.of(2023, 7, 30, 3, 56))
                .build();

        // when
        boolean availableCoupon = coupon.hasAvailableCoupon();

        // then
        assertFalse(availableCoupon);
    }
}