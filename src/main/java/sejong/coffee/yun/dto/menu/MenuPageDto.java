package sejong.coffee.yun.dto.menu;

import org.springframework.data.domain.Page;
import sejong.coffee.yun.domain.order.menu.Menu;

import java.util.List;

public class MenuPageDto {
    public record Response(int pageNum, List<MenuDto.Response> responses) {

        public Response(Page<Menu> menuPage) {
            this(
                    menuPage.getNumber(),
                    menuPage.stream()
                            .map(MenuDto.Response::new)
                            .toList()
            );
        }
    }
}
