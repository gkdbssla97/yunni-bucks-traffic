package sejong.coffee.yun.domain.order.menu.postgre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.util.List;
import java.util.Optional;

public interface JpaPostgresSQLMenuReviewRepository extends JpaRepository<MenuReview, Long> {
    void deleteByMemberIdAndId(Long memberId, Long reviewId);

    Optional<MenuReview> findByMemberIdAndId(Long memberId, Long reviewId);

    List<MenuReview> findMenuReviewByCommentsContaining(@Param("keyword") String keyword);

    @Query(nativeQuery = true, value = "SELECT * FROM menu_review WHERE comments LIKE %:keyword%")
    List<MenuReview> findMenuReviewByCommentsContainingWithQuery(@Param("keyword") String keyword);
    List<MenuReview> findByCommentsContains(@Param("keyword") String keyword);


    //    @Query("SELECT m FROM MenuReview m WHERE fts(m.comments, :keyword) = true")
    @Query(value = "SELECT * FROM menu_review WHERE to_tsvector('english', comments) @@ plainto_tsquery('english', ?)", nativeQuery = true)
    List<MenuReview> findMenuReviewByCommentsContainingOnFullTextSearchWithQuery(@Param("keyword") String keyword);
}
