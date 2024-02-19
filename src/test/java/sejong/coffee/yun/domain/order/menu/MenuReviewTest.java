package sejong.coffee.yun.domain.order.menu;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import sejong.coffee.yun.domain.order.menu.postgre.PostgresMenuReviewRepository;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.MenuReviewRepository;
import sejong.coffee.yun.repository.review.jdbc.JdbcRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MenuReviewTest extends MainIntegrationTest {

    @Autowired
    private MenuReviewRepository menuReviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private PostgresMenuReviewRepository postgresMenuReviewRepository;
    @Autowired
    @Qualifier("postgresJdbcRepository")
    JdbcRepository menuReviewJdbcPostgresRepository;

    @Autowired
    @Qualifier("mysqlJdbcRepository")
    JdbcRepository menuReviewJdbcMysqlRepository;

    private final List<MenuReview> menuReviews = new ArrayList<>();
    private Member member;
    private Menu menu;

//    @AfterEach
//    void initDB() {
//        menuReviewRepository.clear();
//        userRepository.clear();
//    }

    @PostConstruct
    public void init() throws IOException {
        postgresMenuReviewRepository.clear();
        menuReviewRepository.deleteAllInBatch();
        userRepository.clear();
        member = userRepository.save(member());
        menu = menuRepository.save(bread());

        Faker faker = new Faker(new Locale("ko"));

        for (int i = 0; i < 10; i++) {
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

            menuReviews.add(menuReview);
        }
//        menuReviewJdbcPostgresRepository.saveReviewsByJdbc(menuReviews, member.getId(), menu.getId());
        menuReviewJdbcMysqlRepository.saveReviewsByJdbc(menuReviews, member.getId(), menu.getId());

    }

    @Test
    void MasterSlaveTest() {

        for (MenuReview menuReview : menuReviews) {
            System.out.println("--saveS--");
            menuReviewRepository.save(menuReview);
            System.out.println("--saveE--");
        }
        System.out.println("--findAllS1--");
        for(int i = 1; i <= 10; i++) {
            menuReviewRepository.findAll();
        }
    }

    @Test
    void 대용량데이터JPA로검색() {
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("일반적으로");
        assertThat(menuReviewList.size()).isEqualTo(0);
    }

    @Test
    void 대용량데이터FullTextSearch로검색_MySQL() {
        System.out.println("크기: " + menuReviews.size());
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("과학");
//        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithFTS("과학");
        assertThat(menuReviewList.size()).isGreaterThan(0);
        System.out.println("크기: " + menuReviewList.size());
    }

    @Test
    @Rollback(value = false)
    void 대용량데이터FullTextSearch로검색MySQL() {
        System.out.println("크기: " + menuReviews.size());


        String keyword = "%" + "Rice" + "%";
        long startTime2 = System.nanoTime();
        List<MenuReview> menuReviewList2 = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("Rice");
        long endTime2 = System.nanoTime();
        double duration2 = (endTime2 - startTime2) / 1_000_000.0; // 나노초를 밀리초로 변환 후, 밀리초를 초로 변환
        System.out.println("%LIKE% 실행 시간: " + duration2 + " ms");

        long startTime1 = System.nanoTime();
        List<MenuReview> menuReviewList1 = menuReviewRepository.findMenuReviewByCommentsContainingWithFTS("Rice");
        long endTime1 = System.nanoTime();
        double duration1 = (endTime1 - startTime1) / 1_000_000.0; // 나노초를 밀리초로 변환 후, 밀리초를 초로 변환
        System.out.println("Full Text Search 실행 시간: " + duration1 + " ms");

        assertThat(menuReviewList1.size()).isGreaterThan(0);
        assertThat(menuReviewList2.size()).isGreaterThan(0);
        System.out.println("Full Text Search 결과 크기: " + menuReviewList1.size());
        System.out.println("%LIKE% 결과 크기: " + menuReviewList2.size());
    }

    @Test
    @Rollback(value = false)
    void 대용량데이터FullTextSearch로검색() {
        System.out.println("크기: " + menuReviews.size());


        String keyword = "%" + "Rice" + "%";
        long startTime2 = System.nanoTime();
//        List<MenuReview> menuReviewList2 = postgresMenuReviewRepository.findMenuReviewByCommentsContainingWithQuery("Rice");
        long endTime2 = System.nanoTime();
        double duration2 = (endTime2 - startTime2) / 1_000_000.0; // 나노초를 밀리초로 변환 후, 밀리초를 초로 변환
        System.out.println("%LIKE% 실행 시간: " + duration2 + " ms");

        long startTime1 = System.nanoTime();
//        List<MenuReview> menuReviewList1 = postgresMenuReviewRepository.findMenuReviewByCommentsContainingOnFullTextSearchWithQuery("Rice");
        long endTime1 = System.nanoTime();
        double duration1 = (endTime1 - startTime1) / 1_000_000.0; // 나노초를 밀리초로 변환 후, 밀리초를 초로 변환
        System.out.println("Full Text Search 실행 시간: " + duration1 + " ms");

//        assertThat(menuReviewList1.size()).isGreaterThan(0);
//        assertThat(menuReviewList2.size()).isGreaterThan(0);
//        System.out.println("Full Text Search 결과 크기: " + menuReviewList1.size());
//        System.out.println("%LIKE% 결과 크기: " + menuReviewList2.size());
    }


    @Test
    void 대용량데이터JPQL로검색() {
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("하윤");
        System.out.println(menuReviewList.size());
        assertThat(menuReviewList.size()).isGreaterThan(0);

    }

    @Test
    @Rollback(value = false)
    void Postgres저장() {
        MenuReview menuReview = MenuReview.builder()
                .comments("hi")
                .member(member)
                .menu(menu)
                .now(now())
                .build();
        postgresMenuReviewRepository.save(menuReview);
        List<MenuReview> all = postgresMenuReviewRepository.findAll();
        for (MenuReview review : all) {
            System.out.println("댓글: " + review.getComments().toString());
        }
    }
}
