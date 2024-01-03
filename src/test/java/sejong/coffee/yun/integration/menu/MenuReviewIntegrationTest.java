package sejong.coffee.yun.integration.menu;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import sejong.coffee.yun.domain.order.menu.MenuReview;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.review.MenuReviewRepository;
import sejong.coffee.yun.service.MenuReviewService;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MenuReviewIntegrationTest extends MainIntegrationTest {

    @Autowired
    private MenuReviewService menuReviewService;
    @Autowired
    private MenuReviewRepository menuReviewRepository;

    @AfterEach
    void initDB() {
        menuReviewRepository.clear();
    }

    @Nested
    @DisplayName("유저가 로그인하고 메뉴 리뷰를 작성한다")
    @Sql(value = {"/sql/user.sql", "/sql/menu.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/sql/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ReviewTest {

        String token;

        @BeforeEach
        void init() throws Exception{
            token = signInModule();
        }

        @Test
        void 유저가_메뉴리뷰를_작성한다_201() throws Exception {
            // given
            String s = toJson(menuReviewRequest());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post(MENU_REVIEW_API_PATH + "/{menuId}/reviews", 1L)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .content(s)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));

            // then
            resultActions.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.comment").value(menuReviewRequest().comment()))
                    .andDo(document("menu-review",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("menuId").description("메뉴 ID")
                            ),
                            requestFields(
                                    fieldWithPath("comment").type(JsonFieldType.STRING).description("코멘트")
                            ),
                            responseFields(
                                    getMenuReviewResponse()
                            )
                    ));
        }

        @Test
        void 유저가_자신이_쓴_리뷰를_조회한다_200() throws Exception {
            // given
            MenuReview menuReview = menuReviewService.create(1L, 1L, "맛있어요", LocalDateTime.now());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get(MENU_REVIEW_API_PATH + "/reviews/{reviewId}", menuReview.getId())
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.comment").value("맛있어요"))
                    .andDo(document("menu-review-find",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("reviewId").description("리뷰 ID")
                            ),
                            responseFields(
                                    getMenuReviewResponse()
                            )
                    ));
        }

        @Test
        void 유저가_메뉴리뷰를_삭제한다_204() throws Exception {
            // given
            MenuReview menuReview = menuReviewService.create(1L, 1L, "맛있어요", LocalDateTime.now());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.delete(MENU_REVIEW_API_PATH + "/reviews/{reviewId}", menuReview.getId())
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isNoContent())
                    .andDo(document("menu-review-delete",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("reviewId").description("리뷰 ID")
                            )
                    ));
        }

        @Test
        void 유저가_자신이_쓴_리뷰를_수정한다_200() throws Exception {
            // given
            MenuReview menuReview = menuReviewService.create(1L, 1L, "맛있어요", LocalDateTime.now());

            String s = toJson(menuReviewUpdateRequest());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.patch(MENU_REVIEW_API_PATH + "/reviews/{reviewId}", menuReview.getId())
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .content(s)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.comment").value(menuReviewUpdateRequest().comment()))
                    .andDo(document("menu-review-update",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("reviewId").description("리뷰 ID")
                            ),
                            requestFields(
                                    fieldWithPath("comment").description("코멘트")
                            ),
                            responseFields(
                                    getMenuReviewUpdateResponse()
                            )
                    ));
        }

        @Test
        void 잘못된_리뷰ID인_경우_500() throws Exception {
            // given
            menuReviewService.create(1L, 1L, "맛있어요", LocalDateTime.now());

            String s = toJson(menuReviewUpdateRequest());

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.patch(MENU_REVIEW_API_PATH + "/reviews/{reviewId}", 100L)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .content(s)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("invalid-review-id",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }
    }
}
