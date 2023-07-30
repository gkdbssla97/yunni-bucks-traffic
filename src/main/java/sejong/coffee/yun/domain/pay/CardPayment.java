package sejong.coffee.yun.domain.pay;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.user.Card;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of ={"id", "cardNumber", "cardPassword"})
public class CardPayment extends PaymentDateTimeEntity implements Pay {

    @Id @GeneratedValue
    @Column(name = "card_payment_id")
    private Long id;
    private String cardNumber;
    private String cardPassword;
    private String customerName;
    private String cardExpirationYear;
    private String cardExpirationMonth;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public CardPayment(Order order, Card card) {
        this.cardNumber = card.getCardNumber();
        this.cardPassword = card.getCardPassword();
        this.customerName = order.getMember().getName();
        this.cardExpirationYear = parsingCardDate(card.getValidThru())[0];
        this.cardExpirationMonth = parsingCardDate(card.getValidThru())[1];
        this.order = order;
    }

    private String[] parsingCardDate(String validThru) {
        return validThru.split("/");
    }

    @Override
    public void payment() {
        Pay pay = new CardPayment();
    }

    @Override
    public void cancelPayment() {

    }
}
