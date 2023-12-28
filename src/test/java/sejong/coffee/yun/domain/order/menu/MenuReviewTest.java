package sejong.coffee.yun.domain.order.menu;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.menu.MenuReviewJdbcRepository;
import sejong.coffee.yun.repository.review.menu.MenuReviewRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.LocalDateTime.now;

public class MenuReviewTest extends MainIntegrationTest {


    @Autowired
    private MenuReviewRepository menuReviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private MenuReviewJdbcRepository menuReviewJdbcRepository;

    private final List<MenuReview> menuReviews = new ArrayList<>();
    private Member member;
    private Menu menu;

//    @AfterEach
//    void initDB() {
//        menuReviewRepository.clear();
//    }
    @PostConstruct
    public void init() {
        member = userRepository.save(member());
        menu = menuRepository.save(bread());

        Faker faker = new Faker(new Locale("ko"));

        for (int i = 0; i < 10000; i++) {
            String comments = faker.lorem().sentence();  // 랜덤한 문장 생성

            MenuReview menuReview = MenuReview.builder()
                    .id((long) (i + 1))
                    .comments(comments)
                    .member(member)
                    .menu(menu)
                    .now(now())
                    .build();

            menuReviews.add(menuReview);
        }

    }

    @Test
    void 대용량데이터생성() {
        menuReviewJdbcRepository.saveAll(menuReviews, member.getId(), menu.getId());
    }
}
