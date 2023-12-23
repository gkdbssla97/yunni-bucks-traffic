package sejong.coffee.yun.repository.pay.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentStatus;

import java.util.Optional;

public interface JpaPayRepository extends JpaRepository<CardPayment, Long> {

//    @Query("select c from Card c left join c.member where c.member.id =: memberId")
//    void findCardByOrderWithinMember(@Param("memberId") Long id);

    @Query("SELECT cp FROM CardPayment cp WHERE cp.orderUuid = :orderUuid AND cp.paymentStatus = :paymentStatus")
    Optional<CardPayment> findByOrderUuidAnAndPaymentStatus(
            @Param("orderUuid") String orderUuid,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("SELECT cp FROM CardPayment cp WHERE cp.paymentKey = :paymentKey AND cp.paymentStatus = :paymentStatus")
    Optional<CardPayment> findByPaymentKeyAndPaymentStatus(
            @Param("paymentKey") String paymentKey,
            @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT cp FROM CardPayment cp WHERE cp.order.id = :orderId AND cp.paymentStatus = :paymentStatus")
    Optional<CardPayment> findByOrderIdAnAndPaymentStatus(
            @Param("orderId") Long orderId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("SELECT cp FROM CardPayment cp WHERE cp.order.id = :orderId")
    Optional<CardPayment> findByOrderId(
            @Param("orderId") Long orderId
    );
}
