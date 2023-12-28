package sejong.coffee.yun.repository.review.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MenuReviewJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private int batchSize = 1000;

    @Transactional
    public void saveAll(List<MenuReview> items, Long memberId, Long menuId) {

        List<MenuReview> subItems = new ArrayList<>();
        for (MenuReview item : items) {
            subItems.add(item);
            if (subItems.size() % batchSize == 0) {
                batchInsert(subItems, memberId, menuId);
                subItems.clear();
            }
        }
        if (!subItems.isEmpty()) {
            batchInsert(subItems, memberId, menuId);
        }
    }
    private void batchInsert(List<MenuReview> subItems, Long memberId, Long menuId) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO menu_review (id, comments, create_at, update_at, member_id, menu_id) VALUES (?, ?, ?, ?, ?, ?)",
                subItems,
                batchSize,
                (ps, argument) -> {
                    ps.setLong(1, argument.getId());
                    ps.setString(2, argument.getComments());
                    ps.setTimestamp(3, Timestamp.from(Instant.now()));
                    ps.setTimestamp(4, Timestamp.from(Instant.now()));
                    ps.setLong(5, memberId);
                    ps.setLong(6, menuId);
                }
        );
    }
}
