package sejong.coffee.yun.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sejong.coffee.yun.controller.pay.CreatePaymentData;
import sejong.coffee.yun.controller.pay.PaymentController;
import sejong.coffee.yun.controller.mock.TestPayContainer;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.PaymentCancelReason;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.dto.card.CardDto;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.dto.pay.CardPaymentPageDto;
import sejong.coffee.yun.mapper.CustomMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentControllerTest extends CreatePaymentData {

    private TestPayContainer testPayContainer;
    @BeforeEach
    void init() {
        testPayContainer = TestPayContainer.builder()
                .uuid("testUuid")
                .paymentKey("testPaymentKey")
                .build();
        Member saveMember = testPayContainer.userRepository.save(this.member);
        testPayContainer.cardService.create(1L, new CardDto.Request(this.card.getNumber(),
                this.card.getCardPassword(), this.card.getValidThru()));

        Cart cart = testPayContainer.cartRepository.save(Cart.builder().member(member).cartItems(menuList).build());
        testPayContainer.orderRepository.save(Order.createOrder(saveMember, cart, money, LocalDateTime.now()));
    }

    @Test
    public void keyIn으로_카드결제를_한다() throws Exception {

        // given
        // when
        ResponseEntity<CardPaymentDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .keyIn(1L, 1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().paymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.getBody().orderUuid()).isEqualTo("testUuid");
        assertThat(result.getBody().totalAmount()).isEqualTo("3000");
//        assertThat(result.getBody().orderDto().getMember().getName()).isEqualTo("하윤");
        assertThat(result.getBody().paymentKey()).isEqualTo("testPaymentKey");
        assertThat(IntStream.range(0, result.getBody().cardNumber().length())
                .filter(i -> result.getBody().cardNumber().charAt(i) != '*')
                .allMatch(i -> result.getBody().cardNumber().charAt(i) == this.card.getNumber().charAt(i))).isTrue();
    }

    @Test
    public void getByOrderId로_결제내역을_조회한다() throws Exception {

        // given
        PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .keyIn(1L, 1L);

        // when
        ResponseEntity<CardPaymentDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .getByOrderUuid("testUuid");

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().paymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.getBody().orderUuid()).isEqualTo("testUuid");
//        assertThat(result.getBody().orderDto().getOrderPrice().getTotalPrice().toString()).isEqualTo("3000");
//        assertThat(result.getBody().orderDto().getMember().getName()).isEqualTo("하윤");
        assertThat(result.getBody().cardExpirationYear()).isEqualTo("23");
        assertThat(result.getBody().cardExpirationMonth()).isEqualTo("10");
        assertThat(result.getBody().paymentKey()).isEqualTo("testPaymentKey");
        assertThat(result.getBody().cardNumber()).isEqualTo(this.card.getNumber());
    }

    @Test
    public void getByPaymentKey로_결제내역을_조회한다() throws Exception {

        // given
        PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .keyIn(1L, 1L);

        // when
        ResponseEntity<CardPaymentDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .getByPaymentKey("testPaymentKey");

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().paymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.getBody().orderUuid()).isEqualTo("testUuid");
//        assertThat(result.getBody().orderDto().getOrderPrice().getTotalPrice().toString()).isEqualTo("3000");
//        assertThat(result.getBody().orderDto().getMember().getName()).isEqualTo("하윤");
        assertThat(result.getBody().cardExpirationYear()).isEqualTo("23");
        assertThat(result.getBody().cardExpirationMonth()).isEqualTo("10");
        assertThat(result.getBody().paymentKey()).isEqualTo("testPaymentKey");
        assertThat(result.getBody().cardNumber()).isEqualTo(this.card.getNumber());
    }

    @Test
    public void cancelPaymentKey로_결제를_취소한다() throws Exception {

        // given
        PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .keyIn(1L, 1L);

        // when
        ResponseEntity<CardPaymentDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .cancelPayment("testPaymentKey", "0001", BigDecimal.ZERO);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().paymentStatus()).isEqualTo(PaymentStatus.CANCEL);
        assertThat(result.getBody().cancelReason().getDescription()).isEqualTo("서비스 및 상품 불만족");
        assertThat(result.getBody().cancelReason()).isEqualTo(PaymentCancelReason.NOT_SATISFIED_SERVICE);
    }

    @Test
    public void cancelPaymentKey로_결제취소_실패() throws Exception {

        // given
        PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .keyIn(1L, 1L);

        // when
        // then
        assertThatThrownBy(() -> PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .cancelPayment("testPaymentKey", "0005", BigDecimal.ZERO))
                .isInstanceOf(ExceptionControl.NOT_MATCHED_CANCEL_STATUS.paymentException().getClass())
                .hasMessageContaining("결제취소 사유가 올바르지 않습니다.");
    }

    @Test
    void getAllByUsernameAndPaymentStatus로_페이징_처리를_한다() {

        //given
        IntStream.range(0, 10).forEach(i -> {
            try {
                PaymentController.builder()
                        .payService(testPayContainer.payService)
                        .customMapper(new CustomMapper())
                        .build()
                        .keyIn(1L, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //when
        ResponseEntity<CardPaymentPageDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .getAllByUsernameAndPaymentStatus(0, "하윤");

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(result.getBody()).pageNumber()).isEqualTo(0);
    }

    @Test
    void getAllByUsernameAndPaymentCancelStatus로_페이징_처리를_한다() {

        //given
        IntStream.range(0, 10).forEach(i -> {
            try {
                PaymentController.builder()
                        .payService(testPayContainer.payService)
                        .customMapper(new CustomMapper())
                        .build()
                        .keyIn(1L, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //when
        ResponseEntity<CardPaymentPageDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .getAllByUsernameAndPaymentCancelStatus(0, "하윤");

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(result.getBody()).pageNumber()).isEqualTo(0);
    }

    @Test
    void getAllOrderByApprovedAtByDesc로_페이징_처리를_한다() {

        //given
        IntStream.range(0, 10).forEach(i -> {
            try {
                PaymentController.builder()
                        .payService(testPayContainer.payService)
                        .customMapper(new CustomMapper())
                        .build()
                        .keyIn(1L, 1L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //when
        ResponseEntity<CardPaymentPageDto.Response> result = PaymentController.builder()
                .payService(testPayContainer.payService)
                .customMapper(new CustomMapper())
                .build()
                .getAllOrderByApprovedAtByDesc(0);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(result.getBody()).pageNumber()).isEqualTo(0);
    }
}