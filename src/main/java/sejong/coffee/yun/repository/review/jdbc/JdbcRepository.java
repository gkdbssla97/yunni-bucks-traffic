package sejong.coffee.yun.repository.review.jdbc;

import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.io.IOException;
import java.util.List;

public interface JdbcRepository {
    void saveReviewsByJdbc(List<MenuReview> reviews, Long memberId, Long menuId) throws IOException;
    void saveMenusByJdbc(List<Menu> menus);
}

