package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sejong.coffee.yun.domain.user.Coupon;
import sejong.coffee.yun.repository.coupon.CouponRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    public Coupon create(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public Coupon findByCouponId(Long couponId) {
        return couponRepository.findById(couponId);
    }

    public Coupon findByMemberId(Long memberId) {
        return couponRepository.findByMemberId(memberId);
    }

    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }
}
