package sejong.coffee.yun.repository.pay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import sejong.coffee.yun.domain.pay.BeforeCreatedData;
import sejong.coffee.yun.domain.pay.CardPayment;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.infra.fake.FakeUuidHolder;
import sejong.coffee.yun.repository.pay.jpa.JpaPayRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = true)
public class PayJpaRepositoryTest extends BeforeCreatedData {

    @Autowired private JpaPayRepository jpaPayRepository;

    private CardPayment cardPayment;
    private String uuid;

    @BeforeEach
    void init() {
        uuid = new FakeUuidHolder("asdfasdfasdf").random();

        cardPayment = CardPayment.builder()
                .cardExpirationYear(this.card.getValidThru())
                .cardExpirationMonth(this.card.getValidThru())
                .cardNumber(this.card.getNumber())
                .cardPassword(this.card.getCardPassword())
                .customerName(this.order.getMember().getName())
                .orderUuid(uuid)
                .order(null)
                .build();
    }

    @Test
    void save로_결제_내역을_저장한다() {
        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey, LocalDateTime.now());

        //when
        CardPayment save = jpaPayRepository.save(approvalPayment);

        //then
        assertThat(save.getId()).isEqualTo(1L);
    }

    @Test
    void findByOrderId로_결제_내역을_조회한다() {
        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey, LocalDateTime.now());

        jpaPayRepository.save(approvalPayment);

        //when
        Optional<CardPayment> result = jpaPayRepository.findByOrderIdAnAndPaymentStatus(uuid, PaymentStatus.DONE);

        //then
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    void findByPaymentKeyAndPaymentStatus로_결제_내역을_조회한다() {
        //given
        String paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";

        CardPayment approvalPayment = CardPayment.approvalPayment(cardPayment, paymentKey, LocalDateTime.now());

        jpaPayRepository.save(approvalPayment);

        //when
        Optional<CardPayment> result = jpaPayRepository.findByPaymentKeyAndPaymentStatus(paymentKey, PaymentStatus.DONE);

        //then
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    void findAll로_결제_내역을_전체_조회한다() {
        //given
        String paymentKey_1 = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6";
        String paymentKey_2 = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL7";
        String paymentKey_3 = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL8";

        CardPayment approvalPayment_1 = CardPayment.approvalPayment(cardPayment, paymentKey_1, LocalDateTime.now());
        CardPayment approvalPayment_2 = CardPayment.approvalPayment(cardPayment, paymentKey_2, LocalDateTime.now());
        CardPayment approvalPayment_3 = CardPayment.approvalPayment(cardPayment, paymentKey_3, LocalDateTime.now());

        jpaPayRepository.save(approvalPayment_1);
        jpaPayRepository.save(approvalPayment_2);
        jpaPayRepository.save(approvalPayment_3);

        //when
        List<CardPayment> paymentList = jpaPayRepository.findAll();

        //then
        assertThat(paymentList.size()).isEqualTo(3);
    }
}