package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuReview;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.MenuReviewRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuReviewService {

    private final UserRepository userRepository;
    private final MenuReviewRepository menuReviewRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public MenuReview create(Long memberId, Long menuId, String comment, LocalDateTime now) {
        Member member = userRepository.findById(memberId);

        Menu menu = menuRepository.findById(menuId);

        MenuReview menuReview = MenuReview.create(comment, member, menu, now);

        return menuReviewRepository.save(menuReview);
    }

    public MenuReview findReview(Long reviewId) {
        return menuReviewRepository.findById(reviewId);
    }

    public List<MenuReview> findAll() {
        return menuReviewRepository.findAll();
    }

    @Transactional
    public void delete(Long reviewId) {
        menuReviewRepository.delete(reviewId);
    }

    @Transactional
    public void delete(Long memberId, Long reviewId) {
        menuReviewRepository.delete(memberId, reviewId);
    }

    public Page<MenuReview> findAllByMemberId(Pageable pageable, Long memberId) {
        return menuReviewRepository.findAllByMemberId(pageable, memberId);
    }

    @Transactional
    public MenuReview updateComment(Long memberId, Long reviewId, String comments, LocalDateTime now) {
        MenuReview menuReview = menuReviewRepository.findByMemberIdAndId(memberId, reviewId);

        menuReview.updateComment(comments);

        menuReview.setUpdateAt(now);

        return menuReview;
    }
}
