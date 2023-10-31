package sejong.coffee.yun.domain.order.menu;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nutrients {

    private int kcal;
    private int carbohydrates;
    private int fats;
    private int proteins;

    @Builder
    public Nutrients(int kcal, int carbohydrates, int fats, int proteins) {
        assert kcal >= 0 && carbohydrates >= 0 && fats >= 0 && proteins >= 0;
        this.kcal = kcal;
        this.carbohydrates = carbohydrates;
        this.fats = fats;
        this.proteins = proteins;
    }
}
