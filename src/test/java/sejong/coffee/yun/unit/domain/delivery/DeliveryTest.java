package sejong.coffee.yun.unit.domain.delivery;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.delivery.Delivery;
import sejong.coffee.yun.domain.delivery.DeliveryStatus;
import sejong.coffee.yun.domain.delivery.DeliveryType;
import sejong.coffee.yun.domain.delivery.ReserveDelivery;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class DeliveryTest {

    @Test
    void 예약_배달() {
        // given
        LocalDateTime reserveTime = LocalDateTime.of(2022, 10, 2, 10, 2);

        Delivery delivery = ReserveDelivery.create(null, LocalDateTime.now(),
                null, DeliveryType.RESERVE, DeliveryStatus.READY, reserveTime);

        // when
        delivery.delivery();

        // then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERY);
    }

    @Test
    void 예약_배달_실패_아직_예약_시간에_도달하지_못함() {
        // given
        LocalDateTime reserveTime = LocalDateTime.of(2024, 10, 2, 10, 2);

        Delivery delivery = ReserveDelivery.create(null, LocalDateTime.now(),
                null, DeliveryType.RESERVE, DeliveryStatus.READY, reserveTime);

        // when
        delivery.delivery();

        // then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.READY);
    }
}
