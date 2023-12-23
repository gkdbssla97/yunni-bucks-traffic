package sejong.coffee.yun.domain.pay;

import java.math.BigDecimal;

public interface Pay {
    public void payment(); // 결제 행위
    public void cancelPayment(PaymentCancelReason cancelReason, BigDecimal refundAmount); // 결제취소
}
