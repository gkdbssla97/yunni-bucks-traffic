package sejong.coffee.yun.repository.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sejong.coffee.yun.domain.user.Coupon;
import sejong.coffee.yun.domain.user.CouponUse;

import java.util.List;

public interface CouponRepository {

    Coupon save(Coupon coupon);
    Coupon findById(Long couponId);
    List<Coupon> findAll();
    Coupon findByMemberId(Long memberId);
    Coupon findByOrderId(Long orderId);
    Page<Coupon> findDeliveryStatusByMemberId(Pageable pageable, Long memberId, CouponUse couponUse);
    void clear();
}
