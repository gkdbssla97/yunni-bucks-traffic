package sejong.coffee.yun.integration.card;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sejong.coffee.yun.dto.card.CardDto;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.integration.SubIntegrationTest;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CardService;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Deprecated
public class CardIntegrationTest extends SubIntegrationTest {

    @Autowired
    public CardRepository cardRepository;
    @Autowired
    public UuidHolder uuidHolder;
    @Autowired
    public CardService cardService;
    @Autowired
    public CartService cartService;
    @Autowired
    public OrderService orderService;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public CartRepository cartRepository;
    @Autowired
    public CustomMapper customMapper;

    @Nested
    @DisplayName("Card Controller 통합 테스트")
    @Sql(value = {"/sql/user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class CardTest {
        String token;

        @BeforeEach
        void init() throws Exception {
            token = signInModule();
        }

        @AfterEach
        void initDB() {
            cardRepository.clear();
            userRepository.clear();
        }

        @Test
        @DisplayName("카드 생성 Http Status Code 201")
        void registerInCard() throws Exception {

            //given
            CardDto.Request request = registerRequest();

            // when
            ResultActions resultActions = mockMvc.perform(post(CARD_API_PATH + "/")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(request.number()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardPassword").value(request.cardPassword()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.validThru").value(request.validThru()))
                    .andDo(document("card-create",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestFields(
                                    getCardRequests()
                            ),
                            responseFields(
                                    getCardResponses()
                            )
                    ));
        }

        @Test
        @DisplayName("카드 번호 오류 생성 실패 Http Status Code 500")
        void createCard_로_회원카드를_등록실패_카드번호오류() throws Exception {

            //given
            CardDto.Request request = badCardNumberRegisterRequest();
            // when
            ResultActions resultActions = mockMvc.perform(post(CARD_API_PATH + "/")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            //then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("card-invalid-number",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestFields(
                                    getCardRequests()
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        @DisplayName("카드 비밀번호 오류 생성 실패 Http Status Code 500")
        void createCard_로_회원카드를_등록실패_비밀번호오류() throws Exception {

            //given
            CardDto.Request request = badCardPasswordRegisterRequest();
            // when
            ResultActions resultActions = mockMvc.perform(post(CARD_API_PATH + "/")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            //then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("card-invalid-password",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestFields(
                                    getCardRequests()
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        @DisplayName("카드 날짜 오류 생성 실패 Http Status Code 500")
        void createCard_로_회원카드를_등록실패_날짜오류() throws Exception {

            //given
            CardDto.Request request = badCardValidDateRegisterRequest();
            // when
            ResultActions resultActions = mockMvc.perform(post(CARD_API_PATH + "/")
                            .content(objectMapper.writeValueAsString(request))
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            //then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("card-invalid-date",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestFields(
                                    getCardRequests()
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Nested
        @DisplayName("등록된 카드 테스트")
        @Sql(value = {"/sql/user.sql", "/sql/card.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        class RegisteredCardTest {

            @Test
            @DisplayName("회원ID로 카드 조회 Http Status Code 200")
            void getByMemberId_로_회원카드를_조회한다() throws Exception {

                //given
                // when
                ResultActions resultActions = mockMvc.perform(get(CARD_API_PATH + "/")
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print());

                // then
                resultActions
                        .andExpect(status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(card().getNumber()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.cardPassword").value(card().getCardPassword()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.validThru").value(card().getValidThru()))
                        .andDo(document("card-find",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                                ),
                                responseFields(
                                        getCardResponses()
                                )
                        ));
            }

            @Test
            @DisplayName("카드 삭제")
            void removeCard_로_회원카드를_삭제한다() throws Exception {

                // when
                ResultActions resultActions = mockMvc.perform(delete(CARD_API_PATH + "/")
                        .header(HttpHeaders.AUTHORIZATION, token));

                // then
                resultActions.andExpect(status().isNoContent())
                        .andDo(document("card-remove",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                                )
                        ));
            }
        }
    }
}
