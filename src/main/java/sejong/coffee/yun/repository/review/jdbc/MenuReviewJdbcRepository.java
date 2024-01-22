package sejong.coffee.yun.repository.review.jdbc;

import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.io.IOException;
import java.util.List;

public interface MenuReviewJdbcRepository {
    void saveAll(List<MenuReview> items, Long memberId, Long menuId) throws IOException;
}
