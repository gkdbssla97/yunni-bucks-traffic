package sejong.coffee.yun.repository.review.impl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.MenuReview;
import sejong.coffee.yun.repository.review.MenuReviewRepository;
import sejong.coffee.yun.repository.review.jpa.JpaMenuReviewRepository;

import java.util.List;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU_REVIEW;
import static sejong.coffee.yun.domain.order.menu.QMenuReview.menuReview;

@Repository
@Primary
@RequiredArgsConstructor
public class MenuReviewRepositoryImpl implements MenuReviewRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final JpaMenuReviewRepository jpaMenuReviewRepository;

    @Override
    @Transactional
    public MenuReview save(MenuReview menuReview) {
        return jpaMenuReviewRepository.save(menuReview);
    }

    @Override
    public MenuReview findById(Long reviewId) {
        return jpaMenuReviewRepository.findById(reviewId)
                .orElseThrow(NOT_FOUND_MENU_REVIEW::notFoundException);
    }

    @Override
    public MenuReview findByMemberIdAndId(Long memberId, Long reviewId) {
        return jpaMenuReviewRepository.findByMemberIdAndId(memberId, reviewId)
                .orElseThrow(NOT_FOUND_MENU_REVIEW::notFoundException);
    }

    @Override
    public List<MenuReview> findAll() {
        return jpaMenuReviewRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long reviewId) {
        try {
            jpaMenuReviewRepository.deleteById(reviewId);
        } catch (Exception e) {
            throw NOT_FOUND_MENU_REVIEW.notFoundException();
        }
    }

    @Override
    @Transactional
    public void delete(Long memberId, Long reviewId) {
        try {
            jpaMenuReviewRepository.deleteByMemberIdAndId(memberId, reviewId);
        } catch (Exception e) {
            throw NOT_FOUND_MENU_REVIEW.notFoundException();
        }
    }

    @Override
    public Page<MenuReview> findAllByMemberId(Pageable pageable, Long memberId) {
        List<MenuReview> menuReviews = jpaQueryFactory.selectFrom(menuReview)
                .where(menuReview.member.id.eq(memberId))
                .orderBy(menuReview.createAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> jpaQuery = jpaQueryFactory.select(menuReview.count())
                .from(menuReview);

        return PageableExecutionUtils.getPage(menuReviews, pageable, jpaQuery::fetchOne);
    }

    @Override
    public void clear() {
        jpaMenuReviewRepository.deleteAll();
    }

    @Override
    public List<MenuReview> findMenuReviewByCommentsContaining(String keyword) {
        return jpaMenuReviewRepository.findMenuReviewByCommentsContaining(keyword);
    }

    @Override
    public List<MenuReview> findMenuReviewByCommentsContainingWithQuery(String keyword) {
        return jpaMenuReviewRepository.findMenuReviewByCommentsContainingWithQuery(keyword);
    }
}
