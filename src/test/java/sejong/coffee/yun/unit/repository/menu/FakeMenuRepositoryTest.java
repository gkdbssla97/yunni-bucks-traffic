package sejong.coffee.yun.unit.repository.menu;

import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.mock.repository.FakeMenuRepository;
import sejong.coffee.yun.repository.menu.MenuRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FakeMenuRepositoryTest {

    private final MenuRepository menuRepository;

    public FakeMenuRepositoryTest() {
        this.menuRepository = new FakeMenuRepository();
    }

    @Test
    void save() {
        // given
        Nutrients nutrients = new Nutrients(80, 80, 80, 80);
        Beverage beverage = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        // when
        Menu save = menuRepository.save(beverage);

        // then
        assertThat(save.getDescription()).isEqualTo(beverage.getDescription());
        assertThat(save.getCreateAt()).isEqualTo(beverage.getCreateAt());
        assertThat(save.getTitle()).isEqualTo(save.getTitle());
    }

    @Test
    void find() {
        // given
        Nutrients nutrients = new Nutrients(80, 80, 80, 80);
        Beverage beverage = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        Menu save = menuRepository.save(beverage);

        // when
        Menu findMenu = menuRepository.findById(save.getId());

        // then
        assertThat(findMenu).isEqualTo(save);
    }
}