package sejong.coffee.yun.controller.pay.mock;

import lombok.Builder;
import sejong.coffee.yun.infra.ApiService;
import sejong.coffee.yun.infra.fake.FakeApiService;
import sejong.coffee.yun.infra.fake.FakeUuidHolder;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.mock.repository.FakeOrderRepository;
import sejong.coffee.yun.mock.repository.FakeUserRepository;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.card.fake.FakeCardRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.pay.PayRepository;
import sejong.coffee.yun.repository.pay.fake.FakePayRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CardService;
import sejong.coffee.yun.service.PayService;

public class TestPayContainer {

    public final PayRepository payRepository;
    public final OrderRepository orderRepository;
    public final CardRepository cardRepository;
    public final UuidHolder uuidHolder;
    public final PayService payService;
    public final ApiService apiService;
    public final CardService cardService;
    public final UserRepository userRepository;

    @Builder
    public TestPayContainer(String uuid, String paymentKey) {
        this.payRepository = new FakePayRepository();
        this.orderRepository = new FakeOrderRepository();
        this.cardRepository = new FakeCardRepository();
        this.userRepository = new FakeUserRepository();
        this.uuidHolder = new FakeUuidHolder(uuid);
        this.apiService = new ApiService(new FakeApiService(paymentKey));
        this.payService = new PayService(
                this.apiService, this.payRepository, this.orderRepository
                ,this.cardRepository, this.uuidHolder);
        cardService = new CardService(
                this.cardRepository,
                this.userRepository);
    }
}
