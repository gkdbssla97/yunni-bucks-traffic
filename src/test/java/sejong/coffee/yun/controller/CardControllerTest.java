package sejong.coffee.yun.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sejong.coffee.yun.controller.mock.TestCardContainer;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.unit.domain.pay.BeforeCreatedData;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.dto.card.CardDto;
import sejong.coffee.yun.mapper.CustomMapper;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CardControllerTest extends BeforeCreatedData {

    private TestCardContainer testCardContainer;

    @BeforeEach
    void init() {
        testCardContainer = TestCardContainer.builder()
                .build();
        testCardContainer.userRepository.save(this.member);
    }

    @Test
    void createCard_로_회원카드를_등록한다() {

        //given
        ResponseEntity<CardDto.Response> result = CardController.builder()
                .cardService(testCardContainer.cardService)
                .customMapper(new CustomMapper())
                .build()
                .createCard(1L, new CardDto.Request(this.card.getNumber(), this.card.getCardPassword(), this.card.getValidThru()));
        //when
        Card findCard = testCardContainer.cardService.findById(1L);

        //then
        assertThat(findCard.getNumber()).isEqualTo("1234123443211239");
        assertThat(findCard.getCardPassword()).isEqualTo("1234");
        assertThat(findCard.getValidThru()).isEqualTo("10/23");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(result.getBody()).number()).isEqualTo("1234123443211239");
        assertThat(result.getBody().cardPassword()).isEqualTo("1234");
        assertThat(result.getBody().validThru()).isEqualTo("10/23");
    }

    @Test
    void createCard_로_회원카드를_등록실패_날짜오류() {

        //given
        //when
        //then
        assertThatThrownBy(() -> CardController.builder()
                .cardService(testCardContainer.cardService)
                .customMapper(new CustomMapper())
                .build()
                .createCard(1L, new CardDto.Request(this.card.getNumber(), this.card.getCardPassword(), "21/10")))
                .isInstanceOf(ExceptionControl.INVALID_CARD_EXPIRATION_DATE.cardException().getClass())
                .hasMessageContaining("카드 유효기간이 올바르지 않습니다.");
    }

    @Test
    void createCard_로_회원카드를_등록실패_카드번호오류() {

        //given
        //when
        //then
        assertThatThrownBy(() -> CardController.builder()
                .cardService(testCardContainer.cardService)
                .customMapper(new CustomMapper())
                .build()
                .createCard(1L, new CardDto.Request("1234-238901234", this.card.getCardPassword(), this.card.getValidThru())))
                .isInstanceOf(ExceptionControl.INVALID_CARD_NUMBER_LENGTH.cardException().getClass())
                .hasMessageContaining("카드번호가 유효하지 않습니다.(숫자로 20자 내외)");
    }

    @Test
    void createCard_로_회원카드를_등록실패_비밀번호오류() {

        //given
        //when
        //then
        assertThatThrownBy(() -> CardController.builder()
                .cardService(testCardContainer.cardService)
                .customMapper(new CustomMapper())
                .build()
                .createCard(1L, new CardDto.Request(this.card.getNumber(), "12345", "21/10")))
                .isInstanceOf(ExceptionControl.INVALID_CARD_PASSWORD.cardException().getClass())
                .hasMessageContaining("카드 유효기간이 올바르지 않습니다.");
    }

    @Test
    void getByMemberId_로_회원카드를_등록한다() {

        //given
        CardController.builder()
                .cardService(testCardContainer.cardService)
                .customMapper(new CustomMapper())
                .build()
                .createCard(1L, new CardDto.Request(this.card.getNumber(), this.card.getCardPassword(), this.card.getValidThru()));
        //when
        Card findCard = testCardContainer.cardService.getByMemberId(1L);

        //then
        assertThat(findCard).isNotNull();
        assertThat(findCard.getNumber()).isEqualTo("1234123443211239");
        assertThat(findCard.getCardPassword()).isEqualTo("1234");
        assertThat(findCard.getValidThru()).isEqualTo("10/23");
    }
}
