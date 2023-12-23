package sejong.coffee.yun.repository.pay;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentStatus;

import java.util.List;

public interface PayRepository {

    CardPayment save(CardPayment cardPayment);

    CardPayment findById(long id);

    List<CardPayment> findAll();

    void clear();

    CardPayment findByOrderUuidAnAndPaymentStatus(String orderUuid, PaymentStatus paymentStatus);
    CardPayment findByOrderIdAnAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
    CardPayment findByOrderId(Long orderId);

    CardPayment findByPaymentKeyAndPaymentStatus(String paymentKey, PaymentStatus paymentStatus);

    Page<CardPayment> findAllByUsernameAndPaymentStatus(Pageable pageable, String username);

    Page<CardPayment> findAllByUsernameAndPaymentCancelStatus(Pageable pageable, String username);

    Page<CardPayment> findAllOrderByApprovedAtByDesc(Pageable pageable);
}
