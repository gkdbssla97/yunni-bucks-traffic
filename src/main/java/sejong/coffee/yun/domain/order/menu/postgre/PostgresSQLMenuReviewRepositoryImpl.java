package sejong.coffee.yun.domain.order.menu.postgre;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.MenuReview;

import java.util.List;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU_REVIEW;
import static sejong.coffee.yun.domain.order.menu.QMenuReview.menuReview;

@Repository
@RequiredArgsConstructor
public class PostgresSQLMenuReviewRepositoryImpl implements PostgresMenuReviewRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final JpaPostgresSQLMenuReviewRepository jpaPostgresSQLMenuReviewRepository;

    @Override
    @Transactional("twoDBTransactionManager")
    public MenuReview save(MenuReview menuReview) {
        return jpaPostgresSQLMenuReviewRepository.save(menuReview);
    }

    @Override
    public MenuReview findById(Long reviewId) {
        return jpaPostgresSQLMenuReviewRepository.findById(reviewId)
                .orElseThrow(NOT_FOUND_MENU_REVIEW::notFoundException);
    }

    @Override
    public MenuReview findByMemberIdAndId(Long memberId, Long reviewId) {
        return jpaPostgresSQLMenuReviewRepository.findByMemberIdAndId(memberId, reviewId)
                .orElseThrow(NOT_FOUND_MENU_REVIEW::notFoundException);
    }

    @Override
    public List<MenuReview> findAll() {
        return jpaPostgresSQLMenuReviewRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long reviewId) {
        try {
            jpaPostgresSQLMenuReviewRepository.deleteById(reviewId);
        } catch (Exception e) {
            throw NOT_FOUND_MENU_REVIEW.notFoundException();
        }
    }

    @Override
    @Transactional
    public void delete(Long memberId, Long reviewId) {
        try {
            jpaPostgresSQLMenuReviewRepository.deleteByMemberIdAndId(memberId, reviewId);
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
        jpaPostgresSQLMenuReviewRepository.deleteAll();
    }

    @Override
    public List<MenuReview> findMenuReviewByCommentsContaining(String keyword) {
        return jpaPostgresSQLMenuReviewRepository.findMenuReviewByCommentsContaining(keyword);
    }

    @Override
    public List<MenuReview> findMenuReviewByCommentsContainingWithQuery(String keyword) {
        return jpaPostgresSQLMenuReviewRepository.findMenuReviewByCommentsContainingWithQuery(keyword);
    }

    @Override
    public List<MenuReview> findMenuReviewByCommentsContainingOnFullTextSearchWithQuery(String keyword) {
        return jpaPostgresSQLMenuReviewRepository.findMenuReviewByCommentsContainingOnFullTextSearchWithQuery(keyword);
    }

    @Override
    public List<MenuReview> findByCommentsContains(String keyword) {
        return jpaPostgresSQLMenuReviewRepository.findByCommentsContains(keyword);
    }
}
