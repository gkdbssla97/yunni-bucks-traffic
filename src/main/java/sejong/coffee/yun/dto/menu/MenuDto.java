package sejong.coffee.yun.dto.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.MenuType;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.Money;

public class MenuDto {

    public record Request(
            String title,
            String description,
            Money price,
            Nutrients nutrients,
            MenuSize menuSize,
            int stock,
            MenuType menuType
    ){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Response(Long menuId, String title, String description, Money price, Nutrients nutrients,
                           MenuSize menuSize, int stock, int viewCount, int orderCount) {
        public Response(Menu menu) {
            this(menu.getId(), menu.getTitle(), menu.getDescription(),
                    menu.getPrice().mapBigDecimalToInt(), menu.getNutrients(), menu.getMenuSize(),
                    menu.getStock(), menu.getViewCount(), menu.getOrderCount());
        }

        public static Response fromMenu(Menu menu) {
            return new Response(menu.getId(), menu.getTitle(), menu.getDescription(),
                    menu.getPrice().mapBigDecimalToInt(), menu.getNutrients(), menu.getMenuSize(),
                    menu.getStock(), menu.getViewCount(), menu.getOrderCount());
        }
    }
}
