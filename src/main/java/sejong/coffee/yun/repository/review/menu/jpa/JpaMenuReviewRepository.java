package sejong.coffee.yun.repository.review.menu.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.util.List;
import java.util.Optional;

public interface JpaMenuReviewRepository extends JpaRepository<MenuReview, Long> {
    void deleteByMemberIdAndId(Long memberId, Long reviewId);

    Optional<MenuReview> findByMemberIdAndId(Long memberId, Long reviewId);

    List<MenuReview> findMenuReviewByCommentsContaining(@Param("keyword") String keyword);

    @Query("SELECT m FROM MenuReview m WHERE m.comments LIKE %:keyword%")
    List<MenuReview> findMenuReviewByCommentsContainingWithQuery(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM menu_review WHERE MATCH (comments) AGAINST (?1)", nativeQuery = true)
    List<MenuReview> findMenuReviewByCommentsContainingOnFullTextSearchWithQuery(@Param("keyword") String keyword);
}
