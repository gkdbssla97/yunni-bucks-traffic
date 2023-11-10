package sejong.coffee.yun.repository.coupon.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import sejong.coffee.yun.domain.order.QOrder;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.repository.coupon.CouponRepository;
import sejong.coffee.yun.repository.coupon.jpa.JpaCouponRepository;

import java.util.List;

import static sejong.coffee.yun.domain.user.QMember.member;

@Repository
@Primary
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final JpaCouponRepository jpaCouponRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaCouponRepository.save(coupon);
    }

    @Override
    public Coupon findById(Long couponId) {
        return jpaCouponRepository.findById(couponId)
                .orElse(null);
    }

    @Override
    public List<Coupon> findAll() {
        return jpaCouponRepository.findAll();
    }

    @Override
    public Coupon findByMemberId(Long memberId) {
        return jpaQueryFactory.select(QCoupon.coupon)
                .from(member)
                .join(member.coupon, QCoupon.coupon).fetchJoin()
                .where(member.id.eq(memberId))
                .fetchFirst();
    }

    @Override
    public Coupon findByOrderId(Long orderId) {
        return jpaQueryFactory.select(QCoupon.coupon)
                .from(QOrder.order)
                .join(QOrder.order.cart, QCart.cart)
                .join(QCart.cart.member, QMember.member)
                .join(QMember.member.coupon, QCoupon.coupon)
                .where(QOrder.order.id.eq(orderId))
                .fetchFirst();
    }

    @Override
    public Page<Coupon> findDeliveryStatusByMemberId(Pageable pageable, Long memberId, CouponUse couponUse) {
        return null;
    }

    @Override
    public void clear() {
        jpaCouponRepository.deleteAll();
    }
}
