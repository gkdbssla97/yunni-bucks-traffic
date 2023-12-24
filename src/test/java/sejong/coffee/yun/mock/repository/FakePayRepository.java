package sejong.coffee.yun.mock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.repository.pay.PayRepository;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_PAY_DETAILS;
import static sejong.coffee.yun.domain.pay.PaymentStatus.CANCEL;
import static sejong.coffee.yun.domain.pay.PaymentStatus.DONE;

public class FakePayRepository implements PayRepository {

    private final AtomicLong atomicGeneratedId = new AtomicLong(0);
    private final List<CardPayment> data = new ArrayList<>();

    @Override
    public CardPayment save(CardPayment cardPayment) {
        if (cardPayment.getId() == null || cardPayment.getId() == 0L) {
            CardPayment buildCardPayment = CardPayment.builder()
                    .id(atomicGeneratedId.incrementAndGet())
                    .cardNumber(cardPayment.getCardNumber())
                    .cardPassword(cardPayment.getCardPassword())
                    .customerName(cardPayment.getCustomerName())
                    .cardExpirationYear(cardPayment.getCardExpirationYear())
                    .cardExpirationMonth(cardPayment.getCardExpirationMonth())
                    .paymentKey(cardPayment.getPaymentKey())
                    .requestedAt(cardPayment.getRequestedAt())
                    .approvedAt(cardPayment.getApprovedAt())
                    .order(cardPayment.getOrder())
                    .orderUuid(cardPayment.getOrderUuid())
                    .paymentStatus(cardPayment.getPaymentStatus())
                    .paymentType(cardPayment.getPaymentType())
                    .build();
            data.add(buildCardPayment);
            return buildCardPayment;
        }
        data.removeIf(element -> Objects.equals(element.getId(), cardPayment.getId()));
        data.add(cardPayment);
        return cardPayment;
    }

    @Override
    public CardPayment findById(long id) {
        return data.stream()
                .filter(element -> Objects.equals(element.getId(), id))
                .findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public List<CardPayment> findAll() {
        return data;
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public CardPayment findByOrderUuidAnAndPaymentStatus(String orderUuid, PaymentStatus paymentStatus) {
        return data.stream()
                .filter(element -> Objects.equals(element.getOrderUuid(), orderUuid))
                .filter(element -> Objects.equals(element.getPaymentStatus(), DONE))
                .findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public CardPayment findByOrderIdAnAndPaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        return data.stream()
                .filter(element -> Objects.equals(element.getOrder().getId(), orderId))
                .filter(element -> Objects.equals(element.getPaymentStatus(), DONE))
                .findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public CardPayment findByOrderId(Long orderId) {
        return data.stream()
                .filter(element -> Objects.equals(element.getOrder().getId(), orderId))
                .findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public CardPayment findByPaymentKeyAndPaymentStatus(String paymentKey, PaymentStatus paymentStatus) {
        return data.stream()
                .filter(element -> Objects.equals(element.getPaymentKey(), paymentKey))
                .filter(element -> Objects.equals(element.getPaymentStatus(), DONE))
                .findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public Page<CardPayment> findAllByUsernameAndPaymentStatus(Pageable pageable, String username) {
        return getCardPayments(pageable, username, DONE);
    }

    @Override
    public Page<CardPayment> findAllByUsernameAndPaymentCancelStatus(Pageable pageable, String username) {
        return getCardPayments(pageable, username, CANCEL);
    }

    @NotNull
    private Page<CardPayment> getCardPayments(Pageable pageable, String username, PaymentStatus paymentStatus) {
        List<CardPayment> cardPayments = this.data.stream()
                .filter(element -> Objects.equals(element.getOrder().getMember().getName(), username))
                .filter(element -> Objects.equals(element.getPaymentStatus(), paymentStatus))
                .sorted(Comparator.comparing(CardPayment::getApprovedAt))
                .toList();

        return new PageImpl<>(cardPayments, pageable, cardPayments.size());
    }

    @Override
    public Page<CardPayment> findAllOrderByApprovedAtByDesc(Pageable pageable) {
        List<CardPayment> cardPayments = this.data.stream()
                .sorted(Comparator.comparing(CardPayment::getApprovedAt))
                .toList();

        return new PageImpl<>(cardPayments, pageable, data.size());
    }
}
