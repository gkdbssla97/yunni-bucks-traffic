package sejong.coffee.yun.repository.pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.BeforeCreatedData;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentCancelReason;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.domain.user.Cart;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.mock.repository.FakeCartRepository;
import sejong.coffee.yun.mock.repository.FakeOrderRepository;
import sejong.coffee.yun.mock.repository.FakePayRepository;
import sejong.coffee.yun.mock.repository.FakeUserRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.mock.FakeUuidHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FakePayRepositoryTest extends BeforeCreatedData {

    private final PayRepository payRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private CardPayment cardPayment;
    private String uuid;

    public FakePayRepositoryTest() {
        payRepository = new FakePayRepository();
        userRepository = new FakeUserRepository();
        orderRepository = new FakeOrderRepository();
        cartRepository = new FakeCartRepository();
    }

    @BeforeEach
    void init() {
        Member saveMember = userRepository.save(this.member);

//        Card buildCard = Card.builder()
//                .member(saveMember)
//                .number(card.getNumber())
//                .cardPassword(card.getCardPassword())
//                .validThru(card.getValidThru())
//                .build();

        Cart cart = cartRepository.save(Cart.builder().member(member).cartItems(menuList).build());
        Order saveOrder = Order.createOrder(saveMember, cart, money, LocalDateTime.now());
        orderRepository.save(saveOrder);

        uuid = new FakeUuidHolder("uuid-random-init").random();

        cardPayment = CardPayment.builder()
                .cardExpirationYear(this.card.getValidThru())
                .cardExpirationMonth(this.card.getValidThru())
                .cardNumber(this.card.getNumber())
                .cardPassword(this.card.getCardPassword())
                .customerName(this.order.getMember().getName())
                .orderUuid(uuid)
                .order(saveOrder)
                .build();
    }

    @Test
    void save로_결제_내역을_저장한다() {
        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey, LocalDateTime.now().toString());

        //when
        CardPayment save = payRepository.save(approvalPayment);

        //then
        assertThat(save.getId()).isEqualTo(1L);
    }

    @Test
    void findByOrderId로_결제_내역을_조회한다() {
        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey, LocalDateTime.now().toString());

        payRepository.save(approvalPayment);

        //when
        CardPayment result = payRepository.findByOrderUuidAnAndPaymentStatus(uuid, PaymentStatus.DONE);

        //then
        assertThat(result.getOrderUuid()).isEqualTo("asdfasdfasdf");
    }

    @Test
    void findByPaymentKeyAndPaymentStatus로_결제_내역을_조회한다() {
        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey, LocalDateTime.now().toString());

        payRepository.save(approvalPayment);

        //when
        CardPayment result = payRepository.findByPaymentKeyAndPaymentStatus(paymentKey, PaymentStatus.DONE);

        //then
        assertThat(result.getPaymentKey()).isEqualTo("5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6");
    }

    @Test
    void findAll로_결제_내역을_전체_조회한다() {
        //given
        String paymentKey_1 = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";
        String paymentKey_2 = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL7";
        String paymentKey_3 = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL8";

        CardPayment approvalPayment_1 = CardPayment.approvalPayment(cardPayment, paymentKey_1, LocalDateTime.now().toString());
        CardPayment approvalPayment_2 = CardPayment.approvalPayment(cardPayment, paymentKey_2, LocalDateTime.now().toString());
        CardPayment approvalPayment_3 = CardPayment.approvalPayment(cardPayment, paymentKey_3, LocalDateTime.now().toString());

        payRepository.save(approvalPayment_1);
        payRepository.save(approvalPayment_2);
        payRepository.save(approvalPayment_3);

        //when
        List<CardPayment> paymentList = payRepository.findAll();

        //then
        assertThat(paymentList.size()).isEqualTo(3);
    }

    @Test
    void findAllByUsernameAndPaymentStatus로_필터링하여_결제내역을_조회한다() {

        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        IntStream.range(0, 10).forEach(i ->
        {
            CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey + i, LocalDateTime.now().plusMinutes(i).toString());
            payRepository.save(approvalPayment);
        });

        PageRequest pageRequest = PageRequest.of(0, 5);
        //when
        Page<CardPayment> cardPayments = payRepository.findAllByUsernameAndPaymentStatus(pageRequest, "하윤");

        //then
        assertThat(cardPayments.getTotalPages()).isEqualTo(2);
        assertThat(cardPayments.getTotalElements()).isEqualTo(10);
        assertThat(cardPayments.getSize()).isEqualTo(5);
        assertThat(cardPayments.getContent()).extracting("paymentKey")
                .contains(
                        "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL60",
                        "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL61",
                        "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL62"
                );

    }

    @Test
    void findAllByUsernameAndPaymentCancelStatus로_필터링하여_결제내역을_조회한다() {

        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        IntStream.range(0, 10).forEach(i ->
        {
            CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey + i, LocalDateTime.now().plusMinutes(i).toString());
            approvalPayment.cancelPayment(PaymentCancelReason.CHANGE_PRODUCT, BigDecimal.ZERO);
            payRepository.save(approvalPayment);
        });

        PageRequest pageRequest = PageRequest.of(0, 10);
        //when
        Page<CardPayment> cardPayments = payRepository.findAllByUsernameAndPaymentCancelStatus(pageRequest, "하윤");
        for(CardPayment c : cardPayments) {
            System.out.println(c);
        }
        //then
        assertThat(cardPayments.getTotalPages()).isEqualTo(1);
        assertThat(cardPayments.getTotalElements()).isEqualTo(10);
        assertThat(cardPayments.getSize()).isEqualTo(10);
        assertThat(cardPayments.getContent().get(1)).extracting("paymentStatus").isEqualTo(PaymentStatus.CANCEL);
    }

    @Test
    void findAllOrderByApprovedAtByDesc로_필터링하여_결제내역을_조회한다() {

        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        IntStream.range(0, 10).forEach(i ->
        {
            CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey + i, LocalDateTime.now().plusMinutes(i).toString());
            payRepository.save(approvalPayment);
        });

        PageRequest pageRequest = PageRequest.of(0, 5);
        //when
        Page<CardPayment> cardPayments = payRepository.findAllOrderByApprovedAtByDesc(pageRequest);

        //then
        assertThat(cardPayments.getTotalPages()).isEqualTo(2);
        assertThat(cardPayments.getTotalElements()).isEqualTo(10);
        assertThat(cardPayments.getSize()).isEqualTo(5);
    }
}
