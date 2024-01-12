package sejong.coffee.yun.domain.order.menu;

import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import sejong.coffee.yun.domain.order.menu.postgre.PostgresMenuReviewRepository;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.review.MenuReviewRepository;
import sejong.coffee.yun.repository.review.jdbc.MenuReviewJdbcRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

//@AutoConfigureTestDatabase(replace = Replace.NONE)
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
    @Qualifier("menuReviewJdbcPostgresRepository")
    MenuReviewJdbcRepository menuReviewJdbcPostgresRepository;

    @Autowired
    @Qualifier("menuReviewJdbcMysqlRepository")
    MenuReviewJdbcRepository menuReviewJdbcMysqlRepository;

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
        menuReviewRepository.clear();
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
                    .comments(s)
                    .member(member)
                    .menu(menu)
                    .now(now())
                    .build();

            menuReviews.add(menuReview);
        }
//        menuReviewJdbcPostgresRepository.saveAll(menuReviews, member.getId(), menu.getId());
        menuReviewJdbcMysqlRepository.saveAll(menuReviews, member.getId(), menu.getId());
    }

    @Test
    void MasterSlaveTest() {
        System.out.println("----");
        List<MenuReview> all = menuReviewRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(10);
    }
    @Test
    void 대용량데이터JPA로검색() {
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("일반적으로");
        assertThat(menuReviewList.size()).isEqualTo(0);
    }

    @Test
    @Rollback(value = false)
    void 대용량데이터FullTextSearch로검색_MySQL() {
        System.out.println("크기: " + menuReviews.size());
//        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("Ri");
        String keyword = "%" + "Rice" + "%";
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithFTS("Ri");
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
        List<MenuReview> menuReviewList2 = postgresMenuReviewRepository.findMenuReviewByCommentsContainingWithQuery("Rice");
        long endTime2 = System.nanoTime();
        double duration2 = (endTime2 - startTime2) / 1_000_000.0; // 나노초를 밀리초로 변환 후, 밀리초를 초로 변환
        System.out.println("%LIKE% 실행 시간: " + duration2 + " ms");

        long startTime1 = System.nanoTime();
        List<MenuReview> menuReviewList1 = postgresMenuReviewRepository.findMenuReviewByCommentsContainingOnFullTextSearchWithQuery("Rice");
        long endTime1 = System.nanoTime();
        double duration1 = (endTime1 - startTime1) / 1_000_000.0; // 나노초를 밀리초로 변환 후, 밀리초를 초로 변환
        System.out.println("Full Text Search 실행 시간: " + duration1 + " ms");

        assertThat(menuReviewList1.size()).isGreaterThan(0);
        assertThat(menuReviewList2.size()).isGreaterThan(0);
        System.out.println("Full Text Search 결과 크기: " + menuReviewList1.size());
        System.out.println("%LIKE% 결과 크기: " + menuReviewList2.size());
    }


    @Test
    void 대용량데이터JPQL로검색() {
        List<MenuReview> menuReviewList = menuReviewRepository.findMenuReviewByCommentsContainingWithQuery("의무");
        System.out.println(menuReviewList.size());
        assertThat(menuReviewList.size()).isGreaterThan(0);

    }

    @Test
//    @Rollback(value = false)
    void Postgres저장() {
        menuReviewRepository.clear();
        MenuReview menuReview = MenuReview.builder()
                    .comments("hi")
                    .member(member)
                    .menu(menu)
                    .now(now())
                    .build();
        menuReviewRepository.save(menuReview);
        postgresMenuReviewRepository.save(menuReview);
    }
}
