package sejong.coffee.yun.repository.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.util.List;

public interface MenuReviewRepository {

    MenuReview save(MenuReview menuReview);
    MenuReview findById(Long reviewId);
    MenuReview findByMemberIdAndId(Long memberId, Long reviewId);
    List<MenuReview> findAll();
    void delete(Long reviewId);
    void delete(Long memberId, Long reviewId);
    Page<MenuReview> findAllByMemberId(Pageable pageable, Long memberId);
    void clear();
    List<MenuReview> findMenuReviewByCommentsContainingWithQuery(String keyword);
    List<MenuReview> findMenuReviewByCommentsContainingWithQueryMaster(String keyword);
    List<MenuReview> findMenuReviewByCommentsContainingWithFTS(String keyword);
    void deleteAllInBatch();

}
