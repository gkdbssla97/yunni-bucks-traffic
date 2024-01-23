package sejong.coffee.yun.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentCancelReason;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.infra.ApiService;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.pay.PayRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static sejong.coffee.yun.domain.pay.PaymentStatus.DONE;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.*;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.Response.*;

@Service
@RequiredArgsConstructor
@Builder
public class PayService {

    private final ApiService apiService;
    private final PayRepository payRepository;
    private final OrderRepository orderRepository;
    private final CardRepository cardRepository;
    private final UuidHolder uuidHolder;

    public CardPayment findById(long id) {
        return payRepository.findById(id);
    }

    public CardPayment getByOrderId(Long orderId) {
        return payRepository.findByOrderIdAnAndPaymentStatus(orderId, DONE);
    }

    public CardPayment getByOrderUuid(String orderUuid) {
        return payRepository.findByOrderUuidAnAndPaymentStatus(orderUuid, DONE);
    }

    public CardPayment getByPaymentKey(String paymentKey) {
        return payRepository.findByPaymentKeyAndPaymentStatus(paymentKey, DONE);
    }

    public List<CardPayment> findAll() {
        return payRepository.findAll();
    }

    @Transactional
    public Request initPayment(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId);
        Card card = cardRepository.findByMemberId(memberId);

        if (order == null) {
            throw ExceptionControl.NOT_FOUND_ORDER_ID_FOR_PAYMENT.paymentException();
        }

        return Request.create(card, order, uuidHolder);
    }

    @Transactional
    public Confirm initConfirm(Long orderId) {
        Order order = orderRepository.findById(orderId);
        CardPayment payment = payRepository.findByOrderIdAnAndPaymentStatus(orderId, DONE);

        if (order == null) {
            throw ExceptionControl.NOT_FOUND_ORDER_ID_FOR_PAYMENT.paymentException();
        }

        return Confirm.confirm(payment.getPaymentKey(), order);
    }

    @Transactional
    public Response pay(Request request) throws IOException, InterruptedException {

        Response response = apiService.callExternalPayApi(request);
        CardPayment approvalPayment = CardPayment.approvalPayment(CardPayment.fromModel(request), response.paymentKey(), response.approvedAt());
        Order changeOrder = changeOrderPayStatus(request);
        approvalPayment = payRepository.save(approvalPayment);

        return from(approvalPayment, changeOrder);
    }

    @Transactional
    public CardPayment confirm(Confirm confirm) throws IOException, InterruptedException {
        apiService.confirm(confirm);
        return payRepository.findByPaymentKeyAndPaymentStatus(confirm.paymentKey(), DONE);
    }

    public Order changeOrderPayStatus(Request request) {
        Long orderId = request.order().getId();
        Order order = orderRepository.findById(orderId);
        order.setPayStatus();
        return order;
    }

    @Transactional
    public CardPayment cancelPayment(String paymentKey, String cancelCode, BigDecimal money) {
        CardPayment findCardPayment = payRepository.findByPaymentKeyAndPaymentStatus(paymentKey, DONE);
        PaymentCancelReason byCode = PaymentCancelReason.getByCode(cancelCode);
        findCardPayment.cancelPayment(byCode, money);
        return findCardPayment;
    }

    public PaymentStatus checkPaymentStatus(Long orderId) {
        return payRepository.findByOrderId(orderId).getPaymentStatus();
    }

    public Page<CardPayment> getAllByUsernameAndPaymentStatus(Pageable pageable, String username) {
        return payRepository.findAllByUsernameAndPaymentStatus(pageable, username);
    }

    public Page<CardPayment> getAllByUsernameAndPaymentCancelStatus(Pageable pageable, String username) {
        return payRepository.findAllByUsernameAndPaymentCancelStatus(pageable, username);
    }

    public Page<CardPayment> getAllOrderByApprovedAtByDesc(Pageable pageable) {
        return payRepository.findAllOrderByApprovedAtByDesc(pageable);
    }
}
