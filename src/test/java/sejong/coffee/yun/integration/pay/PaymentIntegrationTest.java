package sejong.coffee.yun.integration.pay;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.pay.PaymentStatus;
import sejong.coffee.yun.infra.ApiService;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.integration.SubIntegrationTest;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.repository.pay.PayRepository;
import sejong.coffee.yun.repository.user.UserRepository;
import sejong.coffee.yun.service.CardService;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;
import sejong.coffee.yun.service.PayService;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class PaymentIntegrationTest extends SubIntegrationTest {

    @Autowired
    public PayRepository payRepository;
    @Autowired
    public OrderRepository orderRepository;
    @Autowired
    public CardRepository cardRepository;
    @Autowired
    public UuidHolder uuidHolder;
    @Autowired
    public PayService payService;
    @Autowired
    public ApiService apiService;
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

    @Nested
    @DisplayName("Pay 통합 테스트")
    class PayTest {
        String token;

        @BeforeEach
        void init() throws Exception {
            token = signInModule();
        }

        @AfterEach
        void initDB() {
            payRepository.clear();
            orderRepository.clear();
            cartRepository.clear();
            cardRepository.clear();
            userRepository.clear();
        }

        @Test
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("keyIn 유저가 카드 결제를 한다")
        public void testCardPayment() throws Exception {

            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);
            Order order = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

            // when
            ResultActions resultActions = mockMvc.perform(post(PAY_API_PATH + "/{orderId}", order.getId())
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardNumber").value("9446032384143059"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardExpirationMonth").value("11"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardExpirationYear").value("23"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderDto.name").value("커피빵 외 1개"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentKey").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentStatus").value(PaymentStatus.DONE.name()))
                    .andDo(document("pay-create",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("orderId").description("주문 ID")
                            ),
                            responseFields(
                                    getCardPaymentResponses()
                            )
                    ));
        }

        @Test
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("keyIn 유저가 카드 결제 시 주문 ID 내역이 없음")
        public void testCardPaymentFailed() throws Exception {

            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);
            Order order = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

            // when
            ResultActions resultActions = mockMvc.perform(post(PAY_API_PATH + "/{orderId}", 2)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isInternalServerError())
                    .andDo(document("pay-create-failed",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("orderId").description("주문 ID")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql", "/sql/payment.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("getByOrderId 결제 내역을 조회한다")
        public void 결제내역_조회() throws Exception {

            // given
            // when
            ResultActions resultActions = mockMvc.perform(get(PAY_API_PATH + "/orderId/{orderId}", 1L)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardNumber").value("9446032384143059"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardExpirationMonth").value("11"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cardExpirationYear").value("23"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderDto.name").value("카페라떼 외 3건"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentKey").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentStatus").value(PaymentStatus.DONE.name()))
                    .andDo(document("pay-find",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("orderId").description("주문 ID")
                            ),
                            responseFields(
                                    findCardPaymentResponses()
                            )
                    ));
        }

        @ParameterizedTest(name = "TestCase{index}: 취소코드: {0}")
        @ValueSource(strings = {"0001", "0002", "0003", "0004"})
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql", "/sql/payment.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("cancelPaymentKey 결제를 취소한다")
        public void cancelPayment(String cancelCode) throws Exception {

            // given
            String paymentKey = payRepository.findById(1L).getPaymentKey();

            // when
            ResultActions resultActions = mockMvc.perform(get(PAY_API_PATH + "/cancel")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("paymentKey", paymentKey)
                    .param("cancelCode", cancelCode)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentKey").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.cancelReason").isNotEmpty())
                    .andDo(document("pay-cancel",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("paymentKey").description("결제 키"),
                                    parameterWithName("cancelCode").description("결제 취소 코드")
                            ),
                            responseFields(
                                    cancelCardPaymentResponses()
                            )
                    ));
        }

        @ParameterizedTest(name = "TestCase{index}: 오류코드: {0}")
        @ValueSource(strings = {"0005", "0006"})
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql", "/sql/payment.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("cancelPayment로_결제취소_실패")
        public void invalidCancelPayment(String cancelCode) throws Exception {

            // given
            String paymentKey = payRepository.findById(1L).getPaymentKey();

            // when
            ResultActions resultActions = mockMvc.perform(get(PAY_API_PATH + "/cancel")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("paymentKey", paymentKey)
                    .param("cancelCode", cancelCode)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isInternalServerError())
                    .andDo(document("pay-invalid-cancel",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("paymentKey").description("결제 키"),
                                    parameterWithName("cancelCode").description("결제 취소 코드")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @ParameterizedTest
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql", "/sql/payments.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @ValueSource(strings = {"홍길동"})
        @DisplayName("주문 회원명과 결제 상태 기준으로 내역을 조회한다")
        public void getAllByUsernameAndPaymentStatus(String username) throws Exception {

            // given
            // when
            ResultActions resultActions = mockMvc.perform(get(PAY_API_PATH + "/username-payment/{pageNumber}", 0)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("username", username)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andDo(document("pay-page-find-username-status",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("pageNumber").description("페이지 번호")
                            ),
                            requestParameters(
                                    parameterWithName("username").description("회원 이름")
                            ),
                            responseFields(
                                    getPaymentPageResponse()
                            )
                    ));
        }

        @ParameterizedTest
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql", "/sql/payments.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @ValueSource(strings = {"홍길동"})
        @DisplayName("주문 회원명과 결제 상태 기준으로 취소 내역을 조회한다")
        public void getAllByUsernameAndCancelPaymentStatus(String username) throws Exception {

            // given
            // when
            ResultActions resultActions = mockMvc.perform(get(PAY_API_PATH + "/username-payment-cancel/{pageNumber}", 0)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("username", username)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andDo(document("pay-page-find-username-cancel-status",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("pageNumber").description("페이지 번호")
                            ),
                            requestParameters(
                                    parameterWithName("username").description("회원 이름")
                            ),
                            responseFields(
                                    getPaymentPageResponse()
                            )
                    ));
        }

        @Test
        @Sql(value = {"/sql/user.sql", "/sql/menu.sql", "/sql/card.sql", "/sql/payments.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Sql(value = "/sql/truncate_pay.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("결제 내역을 페이징 조회한다")
        public void getAllPagingPayment() throws Exception {

            // given
            // when
            ResultActions resultActions = mockMvc.perform(get(PAY_API_PATH + "/get/{pageNumber}", 0)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andDo(document("pay-page-findAll",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("pageNumber").description("페이지 번호")
                            ),
                            responseFields(
                                    getPaymentPageResponse()
                            )
                    ));
        }
    }
}