package sejong.coffee.yun.service.mock;

import sejong.coffee.yun.domain.pay.PaymentCancelReason;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.dto.order.OrderDto;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.infra.port.TossApiService;

import java.io.IOException;
import java.util.Random;

import static sejong.coffee.yun.dto.pay.CardPaymentDto.Request;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.Response;

public class FakeTossApiService implements TossApiService {

    public CardPaymentDto.Request payRequest;
    public CardPaymentDto.Cancel cancel;
    public CardPaymentDto.Response payResponse;
    public String paymentKey;

    public FakeTossApiService(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    @Override
    public Response callExternalApi(Request cardPaymentDto) {
        this.payRequest = cardPaymentDto;
        this.payResponse = new Response(1L, payRequest.orderId(), payRequest.orderName(), maskingCardNumber(payRequest.cardNumber()),
                payRequest.cardExpirationYear(), payRequest.cardExpirationMonth(), payRequest.amount(), this.paymentKey,
                PaymentStatus.DONE, payRequest.requestedAt().toString(), payRequest.requestedAt().plusSeconds(5).toString(), new OrderDto.Response(cardPaymentDto.order()), null);

        return this.payResponse;
    }

    @Override
    public Response partialCancellation(String paymentKey, CardPaymentDto.Cancel cardPaymentDto) throws IOException, InterruptedException {
        this.paymentKey = paymentKey;
        this.cancel = cardPaymentDto;
        this.payResponse = new Response(1L, payRequest.orderId(), payRequest.orderName(), maskingCardNumber(payRequest.cardNumber()),
                payRequest.cardExpirationYear(), payRequest.cardExpirationMonth(), payRequest.amount(), this.paymentKey,
                PaymentStatus.CANCEL, payRequest.requestedAt().toString(), payRequest.requestedAt().plusSeconds(5).toString(), new OrderDto.Response(cardPaymentDto.order()), PaymentCancelReason.MIS_ORDER);

        return this.payResponse;
    }

    private String maskingCardNumber(String number) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(number);
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(number.length() - 1);
            sb.replace(idx, idx + 1, "*");
        }
        return sb.toString();
    }
}
