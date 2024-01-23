package sejong.coffee.yun.controller.mock;

import lombok.Builder;
import sejong.coffee.yun.infra.ApiService;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.mock.repository.*;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.pay.PayRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CardService;
import sejong.coffee.yun.service.PayService;
import sejong.coffee.yun.service.mock.FakeOcrApiService;
import sejong.coffee.yun.service.mock.FakeTossApiService;
import sejong.coffee.yun.service.mock.FakeUuidHolder;

public class TestPayContainer {

    public final PayRepository payRepository;
    public final OrderRepository orderRepository;
    public final CardRepository cardRepository;
    public final UuidHolder uuidHolder;
    public final PayService payService;
    public final ApiService apiService;
    public final CardService cardService;
    public final UserRepository userRepository;
    public final CartRepository cartRepository;

    @Builder
    public TestPayContainer(String uuid, String paymentKey) {
        this.payRepository = new FakePayRepository();
        this.orderRepository = new FakeOrderRepository();
        this.cardRepository = new FakeCardRepository();
        this.userRepository = new FakeUserRepository();
        this.cartRepository = new FakeCartRepository();
        this.uuidHolder = new FakeUuidHolder(uuid);
        this.apiService = new ApiService(new FakeTossApiService(paymentKey), new FakeOcrApiService());
        this.payService = new PayService(
                this.apiService, this.payRepository, this.orderRepository
                ,this.cardRepository, this.uuidHolder);
        cardService = new CardService(
                this.cardRepository,
                this.userRepository);
    }
}
