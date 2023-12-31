package sejong.coffee.yun.domain.delivery;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.user.Address;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("N")
public class NormalDelivery extends Delivery {

    @Builder
    public NormalDelivery(Long id, Order order, LocalDateTime now, Address address, DeliveryType type, DeliveryStatus status) {
        super(id, order, now, address, type, status);
    }

    public static NormalDelivery create(Order order, LocalDateTime now, Address address,
                                        DeliveryType type, DeliveryStatus status) {

        return NormalDelivery.builder()
                .order(order)
                .now(now)
                .address(address)
                .type(type)
                .status(status)
                .build();
    }

    public static NormalDelivery from(Long id, NormalDelivery delivery) {
        return NormalDelivery.builder()
                .id(id)
                .type(delivery.getType())
                .now(delivery.getCreateAt())
                .address(delivery.getAddress())
                .order(delivery.getOrder())
                .status(delivery.getStatus())
                .build();
    }

    @Override
    public void cancel() {
        if(getStatus() == DeliveryStatus.READY) {
            setStatus(DeliveryStatus.CANCEL);
        } else {
            throw new RuntimeException("취소가 불가능합니다.");
        }
    }

    @Override
    public void delivery() {
        if (getStatus() == DeliveryStatus.READY) {
            setStatus(DeliveryStatus.DELIVERY);
        } else {
            throw new RuntimeException("배송이 불가능합니다.");
        }
    }

    @Override
    public void complete() {
        if(getStatus() == DeliveryStatus.DELIVERY) {
            setStatus(DeliveryStatus.COMPLETE);
        } else {
            throw new RuntimeException("배송 완료가 불가능합니다.");
        }
    }
}
