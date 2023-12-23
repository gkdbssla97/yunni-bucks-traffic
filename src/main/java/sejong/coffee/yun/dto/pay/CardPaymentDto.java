package sejong.coffee.yun.dto.pay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentCancelReason;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.dto.order.OrderDto;
import sejong.coffee.yun.infra.port.UuidHolder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static sejong.coffee.yun.util.parse.ParsingUtil.parsingCardValidDate;
import static sejong.coffee.yun.util.parse.ParsingUtil.parsingMemberIdentityNumber;


@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardPaymentDto {

    public record Confirm(
            @NotNull(message = "Payment Key가 없습니다.")
            String paymentKey,
            @NotNull(message = "OrderId가 없습니다.")
            Long orderId,
            BigDecimal amount
    ) {
        public static Confirm confirm(String paymentKey, Order order) {
            return new CardPaymentDto.Confirm(paymentKey, order.getId(), order.getOrderPrice().getTotalPrice());
        }
    }

    public record Request(
            @NotNull(message = "카드번호가 없습니다.")
            String cardNumber,
            @Pattern(regexp = "(?=.*[0-9]).{4}", message = "비밀번호는 숫자 4자여야 합니다.")
            String cardPassword,
            @NotNull(message = "만료 월이 없습니다.")
            String cardExpirationMonth,
            @NotNull(message = "만료 연도가 없습니다.")
            String cardExpirationYear,
            @NotNull(message = "주문 아이디가 없습니다.")
            String orderId,
            @NotNull(message = "주문명이 없습니다.")
            String orderName,
            @NotNull(message = "주문 금액이 없습니다.")
            String amount,
            @NotNull(message = "카드 소유자 번호가없습니다.")
            String customerIdentityNumber,
            String customerName,
            @JsonIgnore
            LocalDateTime requestedAt,
            @JsonIgnore
            Order order
            // 객체를 JSON 문자열로 변환
    ) {
        public static Request create(Card card, Order order, UuidHolder uuidHolder) {
            return new CardPaymentDto.Request(card.getNumber(), card.getCardPassword().substring(0, 2), parsingCardValidDate(card.getValidThru())[0],
                    parsingCardValidDate(card.getValidThru())[1], uuidHolder.random(), order.getName(), String.valueOf(order.getOrderPrice().getTotalPrice().intValue()),
//                    parsingMemberIdentityNumber(card.getMember().getEmail()),
                    parsingMemberIdentityNumber("231212"),
                    card.getMember().getName(), LocalDateTime.now(), order);
        }

        public static Request from(CardPayment cardPayment) {
            return new Request(cardPayment.getCardNumber(), cardPayment.getCardPassword(),
                    cardPayment.getCardExpirationMonth(), cardPayment.getCardExpirationYear(),
                    cardPayment.getOrderUuid(), cardPayment.getOrder().getName(),
                    String.valueOf(cardPayment.getOrder().getOrderPrice().getTotalPrice().intValue()),
                    parsingMemberIdentityNumber("231212"),
                    cardPayment.getOrder().getMember().getName(), LocalDateTime.now(), cardPayment.getOrder());
        }
    }

    public record Response(
            Long paymentId,
            @JsonProperty("orderId")
            String orderUuid,
            String orderName,
            String cardNumber,
            String cardExpirationYear,
            String cardExpirationMonth,
            String totalAmount,
            String paymentKey,
//            @JsonIgnore
            PaymentStatus paymentStatus,
            String requestedAt,
            String approvedAt,
            OrderDto.Response orderDto,
            PaymentCancelReason cancelReason
    ) {
        public Response(CardPayment entity) {
            this(entity.getId(), entity.getOrderUuid(), entity.getOrder().getName(),
                    entity.getCardNumber(), entity.getCardExpirationYear(), entity.getCardExpirationMonth(),
                    entity.getOrder().getOrderPrice().getTotalPrice().toString(),
                    entity.getPaymentKey(), entity.getPaymentStatus(),
                    entity.getRequestedAt().toString(), entity.getApprovedAt().toString(), new OrderDto.Response(entity.getOrder()), entity.getCancelReason());
        }

        public static Response cancel(CardPayment entity) {
            return new Response(entity.getId(), entity.getOrderUuid(), entity.getOrder().getName(), entity.getCardNumber(),
                    entity.getCardExpirationYear(), entity.getCardExpirationMonth(),
                    entity.getOrder().getOrderPrice().getTotalPrice().toString(), entity.getPaymentKey(), entity.getPaymentStatus(),
                    entity.getRequestedAt().toString(), entity.getApprovedAt().toString(), new OrderDto.Response(entity.getOrder()), entity.getCancelReason());
        }
    }
}
