package sejong.coffee.yun.unit.domain.card;

import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.exception.CardException;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.unit.domain.pay.BeforeCreatedData;
import sejong.coffee.yun.domain.user.Card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CardTest extends BeforeCreatedData {

    @Test
    void 카드의_유효기간_검증_성공() {
        //given
        String validThru = this.card.getValidThru();

        //when
        String validateDateTime = this.card.checkExpirationDate(validThru);

        //then
        assertThat(validateDateTime).isEqualTo(validThru);
    }

    @Test
    void 카드의_유효기간_연도_Year_검증_실패() {

        //given

        //when

        //then
        assertThatThrownBy(() -> new Card("1234123443211239", "22/10", "1234", this.member))
                .isInstanceOf(CardException.class)
                .hasMessageContaining(ExceptionControl.INVALID_CARD_EXPIRATION_DATE.getMessage());
    }

    @Test
    void 카드의_유효기간_월_Month_검증_실패() {
        //given
        //when
        //then
        assertThatThrownBy(() -> new Card("1234123443211239", "13/24", "1234", this.member))
                .isInstanceOf(CardException.class)
                .hasMessageContaining(ExceptionControl.INVALID_CARD_EXPIRATION_DATE.getMessage());
    }

    @Test
    void 카드번호_길이_검증_실패() {
        //given
        //when
        //then
        assertThatThrownBy(() -> new Card("123412344321123984921", "23/10", "1234", this.member))
                .isInstanceOf(CardException.class)
                .hasMessageContaining(ExceptionControl.INVALID_CARD_NUMBER_LENGTH.getMessage());
    }
    @Test
    void 카드비밀번호_길이_검증_실패() {
        //given
        //when
        //then
        assertThatThrownBy(() -> new Card("1234123443211239", "11/24", "123554", this.member))
                .isInstanceOf(CardException.class)
                .hasMessageContaining(ExceptionControl.INVALID_CARD_PASSWORD.getMessage());
    }
}
