package sejong.coffee.yun.repository.coupon.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sejong.coffee.yun.domain.user.Coupon;

public interface JpaCouponRepository extends JpaRepository<Coupon, Long> {
}
