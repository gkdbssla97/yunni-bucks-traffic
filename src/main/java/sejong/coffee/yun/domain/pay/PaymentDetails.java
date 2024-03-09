package sejong.coffee.yun.domain.pay;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.DateTimeEntity;
import sejong.coffee.yun.domain.order.Order;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDetails extends DateTimeEntity {

    @Id @GeneratedValue
    private Long id;
    private int totalAmount; // 총 결제금액
    private int balanceAmount; // 취소할 수 있는 금액
    private int suppliedAmount; //공급가액
    private int vat; //부가세

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;


    /**비즈니스 로직
    * 세금 계산
     **/
}
