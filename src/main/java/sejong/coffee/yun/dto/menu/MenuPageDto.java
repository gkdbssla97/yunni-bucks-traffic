package sejong.coffee.yun.dto.menu;

import org.springframework.data.domain.PageImpl;
import sejong.coffee.yun.domain.order.menu.Menu;

import java.util.List;

public class MenuPageDto {
    public record Response(int pageNum, List<MenuDto.Response> responses) {

        public Response(PageImpl<Menu> menuPage) {
            this(
                    menuPage.getNumber(),
                    menuPage.getContent().stream()
                            .map(MenuDto.Response::new)
                            .toList()
            );
        }
    }
}
