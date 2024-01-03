package sejong.coffee.yun.repository.pay.impl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.repository.pay.PayRepository;
import sejong.coffee.yun.repository.pay.jpa.JpaPayRepository;

import java.util.List;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_PAY_DETAILS;
import static sejong.coffee.yun.domain.order.QOrder.order;
import static sejong.coffee.yun.domain.pay.QCardPayment.cardPayment;
import static sejong.coffee.yun.domain.user.QCart.cart;
import static sejong.coffee.yun.domain.user.QMember.member;

@Repository
@Primary
@RequiredArgsConstructor
public class PayRepositoryImpl implements PayRepository {

    private final JpaPayRepository jpaPayRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    @Transactional
    public CardPayment save(CardPayment cardPayment) {
        return jpaPayRepository.save(cardPayment);
    }

    @Override
    public CardPayment findById(long id) {
        return jpaPayRepository.findById(id)
                .orElseThrow((NOT_FOUND_PAY_DETAILS::paymentDetailsException));
    }

    @Override
    public List<CardPayment> findAll() {
        return jpaPayRepository.findAll();
    }

    @Override
    public void clear() {
        jpaPayRepository.deleteAll();
    }

    @Override
    public CardPayment findByOrderUuidAnAndPaymentStatus(String orderUuid, PaymentStatus paymentStatus) {
        return jpaPayRepository.findByOrderUuidAnAndPaymentStatus(orderUuid, PaymentStatus.DONE)
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public CardPayment findByOrderIdAnAndPaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        return jpaPayRepository.findByOrderIdAnAndPaymentStatus(orderId, PaymentStatus.DONE)
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public CardPayment findByOrderId(Long orderId) {
        return jpaPayRepository.findByOrderId(orderId).orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public CardPayment findByPaymentKeyAndPaymentStatus(String paymentKey, PaymentStatus paymentStatus) {
        return jpaPayRepository.findByPaymentKeyAndPaymentStatus(paymentKey, paymentStatus)
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public Page<CardPayment> findAllByUsernameAndPaymentStatus(Pageable pageable, String username) {
        List<CardPayment> cardPayments = jpaQueryFactory.select(cardPayment)
                .from(cardPayment)
                .innerJoin(cardPayment.order, order).fetchJoin()
                .innerJoin(order.cart, cart).fetchJoin()
                .innerJoin(cart.member, member).fetchJoin()
                .where(
                        member.name.eq(username)
                                .and(cardPayment.paymentStatus.eq(PaymentStatus.DONE))
                )

                .orderBy(cardPayment.approvedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> jpaQuery = jpaQueryFactory.select(cardPayment.count())
                .from(cardPayment);

        return PageableExecutionUtils.getPage(cardPayments, pageable, jpaQuery::fetchOne);
    }

    @Override
    public Page<CardPayment> findAllByUsernameAndPaymentCancelStatus(Pageable pageable, String username) {
        List<CardPayment> cardPayments = jpaQueryFactory.selectFrom(cardPayment)
                .innerJoin(cardPayment.order, order).fetchJoin()
                .innerJoin(order.cart, cart).fetchJoin()
                .innerJoin(cart.member, member).fetchJoin()
                .where(
                        member.name.eq(username)
                                .and(cardPayment.paymentStatus.eq(PaymentStatus.CANCEL))
                )
                .orderBy(cardPayment.approvedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> jpaQuery = jpaQueryFactory.select(cardPayment.count())
                .from(cardPayment);

        return PageableExecutionUtils.getPage(cardPayments, pageable, jpaQuery::fetchOne);
    }

    @Override
    public Page<CardPayment> findAllOrderByApprovedAtByDesc(Pageable pageable) {
        List<CardPayment> cardPayments = jpaQueryFactory.selectFrom(cardPayment)
                .orderBy(cardPayment.approvedAt.desc())
                .fetch();

        JPAQuery<Long> jpaQuery = jpaQueryFactory.select(cardPayment.count())
                .from(cardPayment);

        return PageableExecutionUtils.getPage(cardPayments, pageable, jpaQuery::fetchOne);
    }
}
