package sejong.coffee.yun.repository.review.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MenuReviewJdbcMysqlRepository implements MenuReviewJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final int batchSize = 1000;


    public MenuReviewJdbcMysqlRepository(@Qualifier("mysqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void saveAll(List<MenuReview> items, Long memberId, Long menuId) throws IOException {

        Resource resource = new ClassPathResource("schema-mysql.sql");
        InputStream inputStream = resource.getInputStream();
        String sql = StreamUtils.copyToString(inputStream, Charset.defaultCharset());

        jdbcTemplate.execute(sql);

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
