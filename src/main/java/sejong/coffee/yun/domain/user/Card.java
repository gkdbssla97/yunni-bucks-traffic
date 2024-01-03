package sejong.coffee.yun.domain.user;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import sejong.coffee.yun.domain.DateTimeEntity;
import sejong.coffee.yun.domain.exception.ExceptionControl;

import javax.persistence.*;
import java.time.LocalDateTime;

import static sejong.coffee.yun.util.parse.ParsingUtil.parsingCardValidDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends DateTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "card_id")
    private Long id;
    @Column(length = 20)
    @Length(max = 20, message = "카드번호는 20자 이하여야 합니다.")
    private String number;
    @Column(length = 4)
    @Length(max = 4, message = "비밀번호는 숫자 4자여야 합니다.")
    private String cardPassword;
    private String validThru;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Member member;

    @Builder
    public Card(Long id, String number, String validThru, String cardPassword, Member member) {
        this.id = id;
        this.number = checkCardNumberLength(number);
        this.validThru = checkExpirationDate(validThru);
        this.cardPassword = checkCardPassword(cardPassword);
        this.member = member;
    }

    public Card(String number, String validThru, String cardPassword, Member member) {
        this.number = checkCardNumberLength(number);
        this.validThru = checkExpirationDate(validThru);
        this.cardPassword = checkCardPassword(cardPassword);
        this.member = member;
    }

    public String checkExpirationDate(String dateTime) {
        String[] splitDateTime = parsingCardValidDate(dateTime);
        int month = Integer.parseInt(splitDateTime[0]);
        int year = Integer.parseInt(splitDateTime[1]);
        if ((year < LocalDateTime.now().getYear() % 100) || (month > 12 || month < 1)) {
            throw ExceptionControl.INVALID_CARD_EXPIRATION_DATE.cardException();
        }
        return dateTime;
    }

    public String checkCardNumberLength(String cardNumber) {
        if(!cardNumber.matches("^[0-9]*$")) {
            throw ExceptionControl.INVALID_CARD_NUMBER_LENGTH.cardException();
        } else if (cardNumber.length() > 20) {
            throw ExceptionControl.INVALID_CARD_NUMBER_LENGTH.cardException();
        }
        return cardNumber;
    }

    public String checkCardPassword(String cardPassword) {
        if(!cardPassword.matches("^[0-9]*$")) {
            throw ExceptionControl.INVALID_CARD_PASSWORD.cardException();
        } else if (cardPassword.length() != 4) {
            throw ExceptionControl.INVALID_CARD_PASSWORD.cardException();
        }
        return cardPassword;
    }

    public static Card createCard(String number, String validThru, String cardPassword, Member member) {
        return Card.builder()
                .number(number)
                .cardPassword(cardPassword)
                .validThru(validThru)
                .member(member)
                .build();
    }
}
