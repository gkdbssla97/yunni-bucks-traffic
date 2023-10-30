package sejong.coffee.yun.domain.order.menu;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sejong.coffee.yun.domain.user.Money;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DiscriminatorValue("Bread")
public class Bread extends Menu {

    @Builder
    public Bread(Long id, String title, String description, Money price, Nutrients nutrients, MenuSize menuSize, LocalDateTime now, int stock) {
        super(id, title, description, price, nutrients, menuSize, now, stock);
    }

    public static Bread from(Long id, Bread bread) {
        return Bread.builder()
                .id(id)
                .title(bread.getTitle())
                .description(bread.getDescription())
                .price(bread.getPrice())
                .nutrients(bread.getNutrients())
                .menuSize(bread.getMenuSize())
                .now(bread.getCreateAt())
                .build();
    }
}
