package sejong.coffee.yun.domain.pay;

import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.exception.CardException;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.service.mock.FakeUuidHolder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CardPaymentTest extends BeforeCreatedData {

    /**
     * --data '{
     * "amount":15000,
     * "orderId":"a4CWyWY5m89PNh7xJwhk1",
     * "orderName":"토스 티셔츠 외 2건",
     * "customerName":"박토스",
     * "cardNumber":"4330123412341234",
     * "cardExpirationYear":"24",
     * "cardExpirationMonth":"07",
     * "cardPassword":"12",
     * "customerIdentityNumber":"881212"
     * }'
     */

    @Test
    void 카드결제_유효기간_검증() {
        //given

        //when
        CardPayment cardPayment = new CardPayment(card, order, new FakeUuidHolder("uuid-random-init"));

        //then
        assertThat(Integer.parseInt(cardPayment.getCardExpirationYear()))
                .isGreaterThanOrEqualTo((LocalDateTime.now().getYear()) % 100)
                .isLessThanOrEqualTo(99);

        assertThat(cardPayment.getCardExpirationMonth())
                .isGreaterThanOrEqualTo("01")
                .isLessThanOrEqualTo("12");
    }

    @Test
    void 카드결제_유효기간_검증_예외발생() {
        assertThatThrownBy(() -> Card.builder()
                .number("123456789123")
                .validThru("11/22")
                .cardPassword("1234")
                .member(member)
                .build())
                .isInstanceOf(CardException.class)
                .hasMessageContaining("카드 유효기간이 올바르지 않습니다.");
    }
}