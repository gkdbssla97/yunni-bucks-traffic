package sejong.coffee.yun.service.pay;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.infra.ApiService;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.pay.PayRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;
import sejong.coffee.yun.service.PayService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static sejong.coffee.yun.domain.pay.PaymentCancelReason.NOT_SATISFIED_SERVICE;
import static sejong.coffee.yun.domain.pay.PaymentCancelReason.getByCode;
import static sejong.coffee.yun.domain.pay.PaymentStatus.DONE;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.Request;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.Response;

@SpringBootTest
@Slf4j
public class PayServiceIntegrateTest extends MainIntegrationTest {

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
    private OrderService orderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private UuidHolder uuidHolder;

    @Nested
    @DisplayName("Pay Service 통합 테스트")
    @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class PayServiceTest {

        private Card card;
        private Order order;

        @BeforeEach
        void init() {
            card = cardRepository.findById(1L);
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);
            cartService.addMenu(1L, 2L);
            cartService.addMenu(1L, 3L);
            order = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());
        }

        @Test
        void findById는_DONE_상태인_결제내역_단건을_조회한다() throws IOException, InterruptedException {

            //given
            Request request = Request.create(card, order, uuidHolder);
            Response response = apiService.callExternalPayApi(request);
            CardPayment approvalPayment = CardPayment.approvalPayment(CardPayment.fromModel(request), response.paymentKey(), request.requestedAt().toString());
            CardPayment saveCardPayment = payRepository.save(approvalPayment);
            Long id = saveCardPayment.getId();

            //when
            CardPayment byId = payService.findById(id);

            //then
            assertThat(byId.getPaymentStatus()).isEqualTo(DONE);
        }

        @Test
        void getByOrderId는_결제내역_단건을_조회한다() throws IOException, InterruptedException {

            //given
            CardPayment cardPayment = new CardPayment(card, order, uuidHolder);
            Request request = Request.from(cardPayment);
            Response response = apiService.callExternalPayApi(request);
            CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());

            CardPayment saveCardPayment = payRepository.save(approvalPayment);
            Long orderId = order.getId();

            //when
            CardPayment findCardPayment = payService.getByOrderId(orderId);

            //then
            assertThat(findCardPayment.getPaymentStatus()).isEqualTo(DONE);
            assertThat(findCardPayment.getCustomerName()).isEqualTo("홍길동");

        }

        @Test
        void getByPaymentKey는_결제내역_단건을_조회한다() throws IOException, InterruptedException {

            //given
            CardPayment cardPayment = new CardPayment(card, order, uuidHolder);
            Request request = Request.from(cardPayment);
            Response response = apiService.callExternalPayApi(request);
            CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());

            CardPayment saveCardPayment = payRepository.save(approvalPayment);
            String paymentKey = saveCardPayment.getPaymentKey();

            //when
            CardPayment byId = payService.getByPaymentKey(paymentKey);

            //then
            assertThat(byId.getPaymentStatus()).isEqualTo(DONE);
            assertThat(byId.getPaymentKey()).isEqualTo(paymentKey);
        }

        @Test
        void initPayment는_전달받은_OrderId로_CardPayment를_만든다() {

            //given
            Long orderId = order.getId();
            Long memberId = 1L;
            orderRepository.findById(orderId);

            //when
            Request request = payService.initPayment(orderId, memberId);

            //then
            assertThat(request.orderId()).isNotEmpty();
        }

        @Test
        void pay는_카드결제를_수행한다() throws IOException, InterruptedException {

            //given
            Long orderId = order.getId();
            Long memberId = 1L;
            orderRepository.findById(orderId);

            //when
            Request request = payService.initPayment(orderId, memberId);
            System.out.println("-> " + request);
            Response cardPayment = payService.pay(request);

            //then
            assertThat(cardPayment.paymentKey()).isNotEmpty();
            assertThat(cardPayment.orderUuid()).isNotEmpty();

            Card byMemberId = cardRepository.findByMemberId(memberId);
            assertThat(byMemberId.getNumber()).isEqualTo(cardPayment.cardNumber());
        }

        @Test
        void cancelPayment는_결제를_취소한다() throws IOException, InterruptedException {

            //given
            CardPayment cardPayment = new CardPayment(card, order, uuidHolder);
            Request request = Request.from(cardPayment);
            Response response = apiService.callExternalPayApi(request);
            CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, response.paymentKey(), request.requestedAt().toString());

            payRepository.save(approvalPayment);

            //when
            String cancelCode = "0001";
            CardPayment cancelPayment = payService.cancelPayment(approvalPayment.getPaymentKey(), cancelCode, request.order().fetchTotalOrderPrice());

            //then
            assertThat(cancelPayment.getCancelReason()).isEqualTo(getByCode(cancelCode));
            assertThat(cancelPayment.getCancelPaymentAt()).isAfter(cancelPayment.getApprovedAt());
        }

        @Test
        void findAllByUsernameAndPaymentStatus는_필터링된_결제내역을_조회한다() {

            //given
            Long orderId = order.getId();
            Long memberId = 1L;

            IntStream.range(0, 10).forEach(i -> {
                        Request request = payService.initPayment(orderId, memberId);
                        try {
                            payService.pay(request);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            List<CardPayment> all = payRepository.findAll();

            //when
            PageRequest pageRequest = PageRequest.of(0, 5);
            Page<CardPayment> cardPayments = payService.getAllByUsernameAndPaymentStatus(pageRequest, "홍길동");

            //then
            assertThat(cardPayments.getTotalPages()).isEqualTo(2);
            assertThat(cardPayments.getTotalElements()).isEqualTo(10);
            assertThat(cardPayments.getSize()).isEqualTo(5);
            assertThat(cardPayments
                    .getContent())
                    .extracting("paymentKey")
                    .isNotEmpty();
        }

        @Test
        void findAllByUsernameAndPaymentCancelStatus는_필터링된_결제내역을_조회한다() {

            //given
            Long orderId = order.getId();
            Long memberId = 1L;

            IntStream.range(0, 10).forEach(i -> {
                        Request request = payService.initPayment(orderId, memberId);
                        try {
                            Response approvalPayment = payService.pay(request);
                            CardPayment findCardPayment = payService.getByPaymentKey(approvalPayment.paymentKey());
                            payService.cancelPayment(findCardPayment.getPaymentKey(), NOT_SATISFIED_SERVICE.getCode(), order.fetchTotalOrderPrice());
                            System.out.println("cancel: " + findCardPayment);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            List<CardPayment> all = payRepository.findAll();
            for (CardPayment cardPayment : all) {
                System.out.println("-> " + cardPayment);
            }
            //when
            PageRequest pageRequest = PageRequest.of(0, 5);
            Page<CardPayment> cardPayments = payService.getAllByUsernameAndPaymentCancelStatus(pageRequest, "홍길동");
            for(CardPayment cardPayment : cardPayments) {
                System.out.println("CANCEL: " + cardPayment);
            }
            //then
            assertThat(cardPayments.getTotalPages()).isEqualTo(2);
            assertThat(cardPayments.getTotalElements()).isEqualTo(10);
            assertThat(cardPayments.getSize()).isEqualTo(5);
            assertThat(cardPayments
                    .getContent())
                    .extracting("cancelReason")
                    .contains(NOT_SATISFIED_SERVICE);
        }

        @Test
        void getAllOrderByApprovedAtByDesc는_필터링된_결제내역을_조회한다() {

            //given
            Long orderId = order.getId();
            Long memberId = 1L;
            orderRepository.findById(orderId);
            userRepository.findById(memberId);

            IntStream.range(0, 3).forEach(i -> {
                        Request request = payService.initPayment(orderId, memberId);
                        try {
                            Thread.sleep(1000);
                            payService.pay(request);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            //when
            PageRequest pageRequest = PageRequest.of(0, 2);
            Page<CardPayment> cardPayments = payService.getAllOrderByApprovedAtByDesc(pageRequest);

            //then
            assertThat(cardPayments.getTotalPages()).isEqualTo(2);
            assertThat(cardPayments.getTotalElements()).isEqualTo(3);
            assertThat(cardPayments.getSize()).isEqualTo(2);
            assertThat(cardPayments.getContent().get(0).getApprovedAt()
                    .compareTo(cardPayments.getContent().get(1).getApprovedAt())).isPositive();
//            log.info("first: " + cardPayments.getContent().get(0).getApprovedAt());
//            log.info("second: " + cardPayments.getContent().get(1).getApprovedAt());
        }

        @AfterEach
        void shutDown() {
            payRepository.clear();
            orderRepository.clear();
            cartRepository.clear();
            menuRepository.clear();
            cardRepository.clear();
            userRepository.clear();
        }
    }
}
