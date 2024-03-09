package sejong.coffee.yun.domain.pay;

import lombok.*;
import sejong.coffee.yun.domain.DateTimeEntity;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.util.parse.ParsingUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static sejong.coffee.yun.domain.pay.PaymentStatus.*;
import static sejong.coffee.yun.util.parse.ParsingUtil.parsingCardValidDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "order")
@Table(name = "card_payment")
public class CardPayment extends DateTimeEntity implements Pay {

    @Id
    @GeneratedValue
    @Column(name = "card_payment_id")
    private Long id;
    private String cardNumber;
    private String cardPassword;
    private String customerName;
    private String cardExpirationMonth;
    private String cardExpirationYear;
    private String paymentKey;
    private String orderUuid;
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;
    @Enumerated(value = EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelPaymentAt;
    @Enumerated(value = EnumType.STRING)
    private PaymentCancelReason cancelReason;
    private BigDecimal refundAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public CardPayment(Card card, Order order, UuidHolder uuidHolder) {
        this.cardNumber = card.getNumber();
        this.cardPassword = displayTwoDigits(card.getCardPassword());
        this.customerName = order.getMember().getName();
        this.cardExpirationMonth = parsingCardValidDate(card.getValidThru())[0];
        this.cardExpirationYear = parsingCardValidDate(card.getValidThru())[1];
        this.order = order;
        this.paymentType = PaymentType.CARD;
        this.paymentStatus = READY;
        this.orderUuid = uuidHolder.random();
        this.requestedAt = LocalDateTime.now();
    }

    @Builder
    public CardPayment(Long id, String cardNumber, String cardPassword, String customerName,
                       String cardExpirationYear, String cardExpirationMonth, PaymentType paymentType,
                       PaymentStatus paymentStatus, LocalDateTime requestedAt, LocalDateTime approvedAt,
                       String paymentKey, String orderUuid, Order order) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.cardPassword = displayTwoDigits(cardPassword);
        this.customerName = customerName;
        this.cardExpirationYear = cardExpirationYear;
        this.cardExpirationMonth = cardExpirationMonth;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.paymentKey = paymentKey;
        this.orderUuid = orderUuid;
        this.order = order;
    }

    @Override
    public void payment() {
        Pay pay = new CardPayment();
    }

    public String displayTwoDigits(String carPassword) {
        return carPassword.substring(0, 2);
    }

    public static CardPayment fromModel(CardPaymentDto.Request cardDomain) {
        CardPayment cardPayment = new CardPayment();
        cardPayment.cardNumber = cardDomain.cardNumber();
        cardPayment.cardPassword = cardDomain.cardPassword();
        cardPayment.customerName = cardDomain.customerName();
        cardPayment.cardExpirationYear = cardDomain.cardExpirationYear();
        cardPayment.cardExpirationMonth = cardDomain.cardExpirationMonth();
        cardPayment.orderUuid = cardDomain.orderId();
        cardPayment.requestedAt = cardDomain.requestedAt();
        cardPayment.order = cardDomain.order();
        return cardPayment;
    }

    public static CardPayment approvalPayment(CardPayment cardPayment, String paymentKey,
                                              String approvedAt) {
        return CardPayment.builder()
                .cardNumber(cardPayment.getCardNumber())
                .cardExpirationYear(cardPayment.getCardExpirationYear())
                .cardExpirationMonth(cardPayment.getCardExpirationMonth())
                .paymentType(cardPayment.getPaymentType())
                .cardPassword(cardPayment.getCardPassword())
                .customerName(cardPayment.getCustomerName())
                .requestedAt(cardPayment.getRequestedAt())
                .paymentStatus(DONE)
                .paymentKey(paymentKey)
                .approvedAt(ParsingUtil.parsingISO8601ToLocalDateTime(approvedAt))
                .order(cardPayment.getOrder())
                .orderUuid(cardPayment.getOrderUuid())
                .build();
    }

    @Override
    public void cancelPayment(PaymentCancelReason cancelReason, BigDecimal refundAmount) {
        this.cancelReason = cancelReason;
        this.paymentStatus = CANCEL;
        this.cancelPaymentAt = LocalDateTime.now();
        if (refundAmount.equals(BigDecimal.ZERO)) {
            this.refundAmount = order.fetchTotalOrderPrice();
        } else {
            this.refundAmount = refundAmount;
        }
    }
}
