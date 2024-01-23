package sejong.coffee.yun.controller.pay;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.custom.annotation.MemberId;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.dto.pay.CardPaymentPageDto;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.PayService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpTimeoutException;

import static sejong.coffee.yun.domain.pay.PaymentCancelReason.NETWORK_CANCEL;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.*;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.Response.from;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Builder
public class PaymentController {

    private final PayService payService;
    private final CustomMapper customMapper;

    @PostMapping("/{orderId}")
    public ResponseEntity<Response> keyIn(@PathVariable Long orderId, @MemberId Long memberId) throws IOException, InterruptedException {
        Request request = payService.initPayment(orderId, memberId);
        Response cardPayment = payService.pay(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardPayment);
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Response> confirm(@PathVariable Long orderId) throws IOException, InterruptedException {
        Confirm confirm = null;
        CardPayment cardPayment = null;
        try {
            confirm = payService.initConfirm(orderId);
            cardPayment = payService.confirm(confirm);
            return ResponseEntity.status(HttpStatus.CREATED).body(customMapper.map(cardPayment, Response.class));
        } catch (HttpTimeoutException e) {
            PaymentStatus status = payService.checkPaymentStatus(orderId);
            if (status.equals(PaymentStatus.READY)) {
                payService.cancelPayment(cardPayment.getPaymentKey(), NETWORK_CANCEL.getCode(), confirm.amount());
            }
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customMapper.map(cardPayment, Response.class));
    }

    @GetMapping("/orderId/{orderId}")
    public ResponseEntity<Response> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(payService.getByOrderId(orderId), Response.class));
    }

    @GetMapping("/orderUuid/{orderUuid}")
    public ResponseEntity<Response> getByOrderUuid(@PathVariable String orderUuid) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(payService.getByOrderUuid(orderUuid), Response.class));
    }

    @GetMapping("/paymentKey/{paymentKey}")
    public ResponseEntity<Response> getByPaymentKey(@PathVariable String paymentKey) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(payService.getByPaymentKey(paymentKey), Response.class));
    }

    @GetMapping("/cancel")
    public ResponseEntity<Response> cancelPayment(@RequestParam("paymentKey") String paymentKey,
                                                  @RequestParam("cancelCode") String cancelCode,
                                                  @RequestParam("refundAmount") BigDecimal money) {
        CardPayment cancelCardPayment = payService.cancelPayment(paymentKey, cancelCode, money);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(from(cancelCardPayment, cancelCardPayment.getOrder()));
    }

    @GetMapping("/username-payment/{pageNumber}")
    public ResponseEntity<CardPaymentPageDto.Response> getAllByUsernameAndPaymentStatus(@PathVariable int pageNumber,
                                                                                        @RequestParam("username") String username) {

        PageRequest pageRequest = PageRequest.of(pageNumber, 5);
        Page<CardPayment> cardPayments = payService.getAllByUsernameAndPaymentStatus(pageRequest, username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(cardPayments, CardPaymentPageDto.Response.class));
    }

    @GetMapping("/username-payment-cancel/{pageNumber}")
    public ResponseEntity<CardPaymentPageDto.Response> getAllByUsernameAndPaymentCancelStatus(@PathVariable int pageNumber,
                                                                                              @RequestParam("username") String username) {

        PageRequest pageRequest = PageRequest.of(pageNumber, 5);
        Page<CardPayment> cardPayments = payService.getAllByUsernameAndPaymentCancelStatus(pageRequest, username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(cardPayments, CardPaymentPageDto.Response.class));
    }

    @GetMapping("/get/{pageNumber}")
    public ResponseEntity<CardPaymentPageDto.Response> getAllOrderByApprovedAtByDesc(@PathVariable int pageNumber) {

        PageRequest pageRequest = PageRequest.of(pageNumber, 5);
        Page<CardPayment> cardPayments = payService.getAllOrderByApprovedAtByDesc(pageRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(cardPayments, CardPaymentPageDto.Response.class));
    }
}
