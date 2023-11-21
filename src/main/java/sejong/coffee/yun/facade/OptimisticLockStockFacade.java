package sejong.coffee.yun.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.service.OrderService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

    private final OrderService orderService;

    public Order order(Long memberId, LocalDateTime localDateTime) throws InterruptedException {
        while (true) {
            try {
                return orderService.orderWithOptimisticLock(memberId, localDateTime);
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
