package sejong.coffee.yun.repository.review.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuReview;
import sejong.coffee.yun.domain.user.Money;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MysqlJdbcRepository implements JdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final int batchSize = 1000;

    public MysqlJdbcRepository(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional("transactionManager")
    public void saveReviewsByJdbc(List<MenuReview> reviews, Long memberId, Long menuId) {

        List<MenuReview> subItems = new ArrayList<>();
        for (MenuReview review : reviews) {
            subItems.add(review);
            if (subItems.size() % batchSize == 0) {
                batchReviewsInsert(reviews, memberId, menuId);
                subItems.clear();
            }
        }
        if (!subItems.isEmpty()) {
            batchReviewsInsert(reviews, memberId, menuId);
        }
    }

    private void batchReviewsInsert(List<MenuReview> reviews, Long memberId, Long menuId) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO menu_review (id, comments, create_at, update_at, member_id, menu_id) VALUES (?, ?, ?, ?, ?, ?)",
                reviews, batchSize,
                (ps, menuReview) -> {
                    ps.setLong(1, menuReview.getId());
                    ps.setString(2, menuReview.getComments());
                    ps.setTimestamp(3, Timestamp.from(Instant.now()));
                    ps.setTimestamp(4, Timestamp.from(Instant.now()));
                    ps.setLong(5, memberId);
                    ps.setLong(6, menuId);
                }
        );
    }

    @Override
    @Transactional("transactionManager")
    public void saveMenusByJdbc(List<Menu> menus) {

        List<Menu> subItems = new ArrayList<>();
        for (Menu menu : menus) {
            subItems.add(menu);
            if (subItems.size() % batchSize == 0) {
                batchMenusInsert(subItems);
                subItems.clear();
            }
        }
        if (!subItems.isEmpty()) {
            batchMenusInsert(subItems);
        }
    }

    private void batchMenusInsert(List<Menu> menus) {
        final int DUMMY_DATA = 1;
        final Money DUMMY_MONEY = Money.initialPrice(new BigDecimal(4000));
        jdbcTemplate.batchUpdate(
                "INSERT INTO menu(dtype, id, carbohydrates, fats, kcal, proteins, order_count, score, view_count, stock, title, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                menus, batchSize,
                (ps, menu) -> {
                    ps.setString(1, menu.getClass().getSimpleName());
                    ps.setLong(2, menu.getId());
                    ps.setInt(3, menu.getNutrients().getCarbohydrates());
                    ps.setInt(4, menu.getNutrients().getFats());
                    ps.setInt(5, menu.getNutrients().getKcal());
                    ps.setInt(6, menu.getNutrients().getProteins());
                    ps.setInt(7, DUMMY_DATA);
                    ps.setDouble(8, DUMMY_DATA);
                    ps.setInt(9, DUMMY_DATA);
                    ps.setInt(10, menu.getStock());
                    ps.setString(11, menu.getTitle());
                    ps.setBigDecimal(12, DUMMY_MONEY.getTotalPrice());
                }
        );
    }
}
