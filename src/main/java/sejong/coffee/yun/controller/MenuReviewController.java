package sejong.coffee.yun.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.custom.annotation.MemberId;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuReview;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.dto.review.menu.MenuReviewDto;
import sejong.coffee.yun.dto.review.menu.MenuReviewPageDto;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.MenuReviewRepository;
import sejong.coffee.yun.repository.review.jdbc.JdbcRepository;
import sejong.coffee.yun.service.MenuReviewService;
import sejong.coffee.yun.service.UserService;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.LocalDateTime.now;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
@Slf4j
public class MenuReviewController {

    private final MenuReviewService menuReviewService;
    private final CustomMapper customMapper;
    private final UserService userService;
    private final MenuRepository menuRepository;
    private final JdbcRepository mysqlJdbcRepository;
    private final MenuReviewRepository menuReviewRepository;

    @PostMapping("/{menuId}/reviews")
    ResponseEntity<MenuReviewDto.Response> menuReviewCreate(@RequestBody @Valid MenuReviewDto.Request request,
                                                            @MemberId Long memberId,
                                                            @PathVariable Long menuId) {

        MenuReview menuReview = menuReviewService.create(memberId, menuId, request.comment(), LocalDateTime.now());

        MenuReviewDto.Response response = customMapper.map(menuReview, MenuReviewDto.Response.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reviews/{reviewId}")
    ResponseEntity<MenuReviewDto.Response> findMenuReview(@PathVariable Long reviewId) {

        MenuReview menuReview = menuReviewService.findReview(reviewId);

        MenuReviewDto.Response response = customMapper.map(menuReview, MenuReviewDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reviews/{reviewId}")
    ResponseEntity<MenuReviewDto.Update.Response> updateComment(@PathVariable Long reviewId,
                                                                @RequestBody @Valid MenuReviewDto.Request request,
                                                                @MemberId Long memberId) {

        MenuReview menuReview = menuReviewService.updateComment(memberId, reviewId, request.comment(), LocalDateTime.now());

        MenuReviewDto.Update.Response response = customMapper.map(menuReview, MenuReviewDto.Update.Response.class);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reviews/{reviewId}")
    ResponseEntity<Void> menuReviewDelete(@MemberId Long memberId,
                                          @PathVariable Long reviewId) {

        menuReviewService.delete(memberId, reviewId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/page/{pageNum}")
    ResponseEntity<MenuReviewPageDto.Response> findAllByMemberId(@MemberId Long memberId,
                                                                 @PathVariable int pageNum) {
        PageRequest pr = PageRequest.of(pageNum, 10);

        Page<MenuReview> menuReviewPage = menuReviewService.findAllByMemberId(pr, memberId);

        MenuReviewPageDto.Response response = customMapper.map(menuReviewPage, MenuReviewPageDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews")
    ResponseEntity<List<MenuReviewDto.Response>> findAll() {
        List<MenuReview> menuReviewList = menuReviewService.findAll();

        List<MenuReviewDto.Response> responses = menuReviewList.stream().map(MenuReviewDto.Response::new).toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/dummy/{size}")
    ResponseEntity<Void> createBulkData(@PathVariable("size") int size) throws IOException {
        Member member = userService.findMember(1L);
        Menu menu = menuRepository.findByTitle("빵");
        Faker faker = new Faker(new Locale("ko"));
        List<MenuReview> menuReviewList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String comments = faker.lorem().sentence();  // 랜덤한 문장 생성
            String s = faker.food().ingredient() + " " +
                    faker.food().ingredient() + " " +
                    faker.food().ingredient() + " " +
                    faker.food().ingredient() + " " +
                    faker.food().ingredient();

            MenuReview menuReview = MenuReview.builder()
                    .id((long) (i + 1))
                    .comments(comments)
                    .member(member)
                    .menu(menu)
                    .now(now())
                    .build();

            menuReviewList.add(menuReview);
        }
//        menuReviewJdbcPostgresRepository.saveAll(menuReviews, member.getId(), menu.getId());
        mysqlJdbcRepository.saveReviewsByJdbc(menuReviewList, member.getId(), menu.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/search/slave")
    ResponseEntity<Void> searchMaster() {
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("과학");
        log.info("SLAVE SEARCH SIZE: " + menuReviewList.size());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/search/master")
    ResponseEntity<Void> searchSlave() {
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQueryMaster("과학");
        log.info("MASTER SEARCH SIZE: " + menuReviewList.size());
        return ResponseEntity.noContent().build();
    }
}
