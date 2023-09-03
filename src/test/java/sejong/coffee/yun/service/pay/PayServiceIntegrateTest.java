package sejong.coffee.yun.service.pay;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sejong.coffee.yun.controller.pay.CreatePaymentData;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentCancelReason;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.infra.ApiService;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.pay.PayRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.PayService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static sejong.coffee.yun.domain.pay.PaymentStatus.DONE;

@SpringBootTest
public class PayServiceIntegrateTest extends CreatePaymentData {

    @Autowired
    private PayService payService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private PayRepository payRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UuidHolder uuidHolder;

    @BeforeEach
    void init() {
//        this.payService = PayService.builder()
//                .payRepository(payRepository)
//                .uuidHolder(uuidHolder)
//                .apiService(apiService)
//                .orderRepository(orderRepository)
//                .cardRepository(cardRepository)
//                .build();

        Member saveMember = userRepository.save(member);

        Card buildCard = Card.builder()
                .member(saveMember)
                .number(card.getNumber())
                .cardPassword(card.getCardPassword())
                .validThru(card.getValidThru())
                .build();

        cardRepository.save(buildCard);
//        menuList.forEach(menuRepository::save);
        Cart cart = cartRepository.save(new Cart(1L, saveMember, menuList));
        Order saveOrder = Order.createOrder(saveMember, cart, money, LocalDateTime.now());
        orderRepository.save(saveOrder);
    }

    @Test
    void findById는_DONE_상태인_결제내역_단건을_조회한다() throws IOException, InterruptedException {

        //given
        CardPaymentDto.Request request = CardPaymentDto.Request.from(cardPayment);
        CardPaymentDto.Response response = apiService.callExternalPayApi(request);
        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());
        payRepository.save(approvalPayment);

        //when
        CardPayment byId = payService.findById(1L);

        //then
        assertThat(byId.getPaymentStatus()).isEqualTo(DONE);
    }

    @Test
    void getByOrderId는_결제내역_단건을_조회한다() throws IOException, InterruptedException {
        //given
        CardPaymentDto.Request request = CardPaymentDto.Request.from(cardPayment);
        CardPaymentDto.Response response = apiService.callExternalPayApi(request);
        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());

        payRepository.save(approvalPayment);

        //when
        CardPayment byId = payService.getByOrderId("asdfasdf");

        //then
        assertThat(byId.getPaymentStatus()).isEqualTo(DONE);
        assertThat(byId.getOrderUuid()).isEqualTo("asdfasdf");

    }

    @Test
    void getByPaymentKey는_결제내역_단건을_조회한다() throws IOException, InterruptedException {
        //given
        CardPaymentDto.Request request = CardPaymentDto.Request.from(cardPayment);
        CardPaymentDto.Response response = apiService.callExternalPayApi(request);
        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());

        payRepository.save(approvalPayment);

        //when
        CardPayment byId = payService.getByPaymentKey("paypaypaypay_1234");

        //then
        assertThat(byId.getPaymentStatus()).isEqualTo(DONE);
        assertThat(byId.getPaymentKey()).isEqualTo("paypaypaypay_1234");
        assertThat(byId.getOrderUuid()).isEqualTo("asdfasdf");
    }

    @Test
    void initPayment는_전달받은_OrderId로_CardPayment를_만든다() {

        //given
        Long orderId = 1L;
        Long memberId = 1L;
        orderRepository.findById(orderId);

        //when
        CardPaymentDto.Request request = payService.initPayment(orderId, memberId);

        //then
        assertThat(request.orderId()).isEqualTo("qwerqewrqwer");
    }

    @Test
    void pay는_카드결제를_수행한다() throws IOException, InterruptedException {

        //given
        Long orderId = 1L;
        Long memberId = 1L;
        orderRepository.findById(orderId);
        userRepository.findById(memberId);

        //when
        CardPaymentDto.Request request = payService.initPayment(orderId, memberId);
        System.out.println("-> " + request);
        CardPayment cardPayment = payService.pay(request);

        //then
        assertThat(cardPayment.getPaymentKey()).isEqualTo("paypaypaypay_1234");
        assertThat(cardPayment.getOrderUuid()).isEqualTo("qwerqewrqwer");
        assertThat(cardPayment.getOrder().getOrderPrice().getTotalPrice().toString()).isEqualTo("3000");
        assertThat(cardPayment.getCustomerName()).isEqualTo("하윤");

        Card byMemberId = cardRepository.findByMemberId(memberId);
        assertThat(byMemberId.getMember().getName()).isEqualTo(cardPayment.getCustomerName());
    }

    @Test
    void cancelPayment는_결제를_취소한다() throws IOException, InterruptedException {
        //given
        CardPaymentDto.Request request = CardPaymentDto.Request.from(cardPayment);
        CardPaymentDto.Response response = apiService.callExternalPayApi(request);
        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());

        payRepository.save(approvalPayment);

        //when
        String cancelCode = "0001";
        CardPayment cancelPayment = payService.cancelPayment(approvalPayment.getPaymentKey(), cancelCode);

        //then
        assertThat(cancelPayment.getCancelReason()).isEqualTo(PaymentCancelReason.getByCode(cancelCode));
        assertThat(cancelPayment.getCancelPaymentAt()).isAfter(cancelPayment.getApprovedAt());
    }
    @Test
    void findAllByUsernameAndPaymentStatus는_필터링된_결제내역을_조회한다() {

        //given
        Long orderId = 1L;
        Long memberId = 1L;
        orderRepository.findById(orderId);
        userRepository.findById(memberId);

        IntStream.range(0, 10).forEach(i -> {
                    CardPaymentDto.Request request = payService.initPayment(orderId, memberId);
                    try {
                        payService.pay(request);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        //when
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<CardPayment> cardPayments = payService.getAllByUsernameAndPaymentStatus(pageRequest, "하윤");

        //then
        assertThat(cardPayments.getTotalPages()).isEqualTo(2);
        assertThat(cardPayments.getTotalElements()).isEqualTo(10);
        assertThat(cardPayments.getSize()).isEqualTo(5);
        assertThat(cardPayments
                .getContent())
                .extracting("paymentKey")
                .contains("paypaypaypay_1234");
    }

    @Test
    void findAllByUsernameAndPaymentCancelStatus는_필터링된_결제내역을_조회한다() {

        //given
        Long orderId = 1L;
        Long memberId = 1L;
        orderRepository.findById(orderId);
        userRepository.findById(memberId);

        IntStream.range(0, 10).forEach(i -> {
                    CardPaymentDto.Request request = payService.initPayment(orderId, memberId);
                    try {
                        CardPayment pay = payService.pay(request);
                        pay.cancel(PaymentCancelReason.NOT_SATISFIED_SERVICE);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        //when
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<CardPayment> cardPayments = payService.getAllByUsernameAndPaymentCancelStatus(pageRequest, "하윤");

        //then
        assertThat(cardPayments.getTotalPages()).isEqualTo(2);
        assertThat(cardPayments.getTotalElements()).isEqualTo(10);
        assertThat(cardPayments.getSize()).isEqualTo(5);
        assertThat(cardPayments
                .getContent())
                .extracting("cancelReason")
                .contains(PaymentCancelReason.NOT_SATISFIED_SERVICE);
    }

    @Test
    void getAllOrderByApprovedAtByDesc는_필터링된_결제내역을_조회한다() {

        //given
        Long orderId = 1L;
        Long memberId = 1L;
        orderRepository.findById(orderId);
        userRepository.findById(memberId);

        IntStream.range(0, 10).forEach(i -> {
                    CardPaymentDto.Request request = payService.initPayment(orderId, memberId);
                    try {
                        payService.pay(request);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        //when
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<CardPayment> cardPayments = payService.getAllOrderByApprovedAtByDesc(pageRequest);

        //then
        assertThat(cardPayments.getTotalPages()).isEqualTo(2);
        assertThat(cardPayments.getTotalElements()).isEqualTo(10);
        assertThat(cardPayments.getSize()).isEqualTo(5);
        assertThat(cardPayments.getContent().get(0).getApprovedAt())
                .isBefore(cardPayments.getContent().get(1).getApprovedAt());
    }

    @AfterEach
    void shutDown() {
        cardRepository.findAll().forEach(card -> cardRepository.delete(card.getId()));
        userRepository.findAll().forEach(user -> userRepository.delete(user.getId()));
        menuRepository.findAll().forEach(menu -> menuRepository.delete(menu.getId()));
    }
}
