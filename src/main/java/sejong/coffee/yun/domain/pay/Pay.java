package sejong.coffee.yun.domain.pay;

import java.math.BigDecimal;

public interface Pay {
    void payment();
    void cancelPayment(PaymentCancelReason cancelReason, BigDecimal refundAmount); // 결제취소
}
