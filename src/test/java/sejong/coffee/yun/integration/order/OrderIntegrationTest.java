package sejong.coffee.yun.integration.order;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.OrderPayStatus;
import sejong.coffee.yun.domain.order.OrderStatus;
import sejong.coffee.yun.integration.MainIntegrationTest;
import sejong.coffee.yun.repository.cart.CartRepository;
import sejong.coffee.yun.repository.order.OrderRepository;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderIntegrationTest extends MainIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;

    @AfterEach
    void initDB() {
        cartRepository.clear();
    }

    @Nested
    @DisplayName("유저가 장바구니에 메뉴를 담는다")
    @Sql(value = {"/sql/user.sql", "/sql/menu.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/sql/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class CartTest {
        String token;

        @BeforeEach
        void init() throws Exception {
            token = signInModule();
        }

        @Test
        void 카트_생성에_성공한다_201() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH)
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isCreated())
                    .andDo(document("cart-create",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    fieldWithPath("cartId").description("메뉴 ID"),
                                    fieldWithPath("memberId").description("유저 ID"),
                                    fieldWithPath("menuList").description("메뉴 리스트").type(JsonFieldType.ARRAY)
                            )
                    ));
        }

        @Test
        void 카트가_중복으로_생성_될_경우() throws Exception {
            // given
            cartService.createCart(1L);

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH)
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isBadRequest())
                    .andDo(document("duplicate-cart",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        void 카트_생성에_실패한다_400() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH)
                    .header(HttpHeaders.AUTHORIZATION, "bearer invalid token"));

            // then
            resultActions.andExpect(status().isBadRequest())
                    .andDo(document("cart-fail",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        void 카트에_메뉴_담는데에_성공한다_200() throws Exception {
            // given
            cartService.createCart(1L);

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuId", "1"));

            // then
            resultActions.andExpect(status().isOk())
                    .andDo(document("menu-add",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuId").description("메뉴 ID")
                            ),
                            responseFields(
                                    fieldWithPath("cartId").description("메뉴 ID"),
                                    fieldWithPath("memberId").description("유저 ID"),
                                    fieldWithPath("menuList").description("메뉴 리스트").type(JsonFieldType.ARRAY),
                                    fieldWithPath("menuList[]").description("메뉴 리스트"),
                                    fieldWithPath("menuList[].menuId").description("메뉴 ID"),
                                    fieldWithPath("menuList[].title").description("메뉴 제목"),
                                    fieldWithPath("menuList[].description").description("메뉴 설명"),
                                    fieldWithPath("menuList[].price.totalPrice").description("메뉴 가격"),
                                    fieldWithPath("menuList[].nutrients").description("영양 정보"),
                                    fieldWithPath("menuList[].menuSize").description("메뉴 크기")
                            )
                            ));
        }

        @Test
        void 카트에_메뉴를_담는데_수량이_초과한_경우_500() throws Exception {
            // given
            cartService.createCart(1L);

            IntStream.range(0, 10).forEach(i -> {
                try {
                    mockMvc.perform(post(CART_API_PATH + "/menu")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .param("menuId", "1"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuId", "1"));

            // then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("menu-add-fail",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuId").description("메뉴 ID")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                            ));
        }

        @Test
        void 잘못된_메뉴ID인_경우_500() throws Exception {
            // given
            cartService.createCart(1L);

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuId", "100"));

            // then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("invalid_menu_id",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuId").description("메뉴 ID")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        void 카트가_생성되지_않고_카트에_어떤_행위를_할_경우_500() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(post(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuId", "1"));

            // then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("not-exist-cart",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuId").description("메뉴 ID")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        void 카트에_메뉴를_지우는데_성공한다_200() throws Exception {
            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);

            // when
            ResultActions resultActions = mockMvc.perform(delete(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuIdx", "0"));

            // then
            resultActions.andExpect(status().isOk())
                    .andDo(document("menu-remove",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuIdx").description("카트에 있는 메뉴 번호")
                            ),
                            responseFields(
                                    fieldWithPath("cartId").description("메뉴 ID"),
                                    fieldWithPath("memberId").description("유저 ID"),
                                    fieldWithPath("menuList").description("메뉴 리스트").type(JsonFieldType.ARRAY)
                            )
                            ));
        }

        @Test
        void 카트에_있는_메뉴를_조회하는데_성공한다_200() throws Exception {
            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);

            // when
            ResultActions resultActions = mockMvc.perform(get(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuIdx", "0"));

            // then
            resultActions.andExpect(status().isOk())
                    .andDo(document("menu-get",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuIdx").description("카트에 있는 메뉴 번호")
                            ),
                            responseFields(
                                    fieldWithPath("menuId").type(JsonFieldType.NUMBER).description("메뉴 ID"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("메뉴 제목"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("메뉴 설명"),
                                    fieldWithPath("price.totalPrice").type(JsonFieldType.NUMBER).description("메뉴 가격"),
                                    fieldWithPath("nutrients").type(JsonFieldType.OBJECT).description("영양소"),
                                    fieldWithPath("menuSize").type(JsonFieldType.STRING).description("메뉴 사이즈")
                            )
                            ));
        }

        @Test
        void 카트에_잘못된_인덱스값을_넣었을_경우_404() throws Exception {
            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);

            // when
            ResultActions resultActions = mockMvc.perform(get(CART_API_PATH + "/menu")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("menuIdx", "100"));

            // then
            resultActions.andExpect(status().isNotFound())
                    .andDo(document("invalid-menu-idx",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("menuIdx").description("카트에 있는 메뉴 번호")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("유저가 주문을 진행")
    @Sql(value = {"/sql/user.sql", "/sql/menu.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/sql/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class OrderTest {

        String token;

        @BeforeEach
        void init() throws Exception {
            token = signInModule();
        }

        @AfterEach
        void initDB() {
            orderRepository.clear();
            cartRepository.clear();
        }

        @Test
        void 주문에_성공한다_201() throws Exception {
            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);

            // when
            ResultActions resultActions = mockMvc.perform(post(ORDER_API_PATH)
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.money.totalPrice").value("1000"))
                    .andDo(print())
                    .andDo(document("orderDto",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    getOrderResponse()
                            )
                    ));
        }

        @Test
        void 장바구니를_생성하지_않고_주문할_경우_500() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(post(ORDER_API_PATH)
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("orderDto-fail",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        void 장바구니가_비어있는데_주문_할_경우_400() throws Exception {
            // given
            cartService.createCart(1L);

            // when
            ResultActions resultActions = mockMvc.perform(post(ORDER_API_PATH)
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionControl.EMPTY_MENUS.getMessage()))
                    .andDo(document("orderDto-empty-cart-fail",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                            ));
        }

        @Test
        void 주문을_취소한다_204() throws Exception {
            // given
            cartService.createCart(1L);
            cartService.addMenu(1L, 1L);
            Order order = orderService.orderWithPessimisticLock(1L, LocalDateTime.now());

            // when
            ResultActions resultActions = mockMvc.perform(get(ORDER_API_PATH + "/cancel")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("orderId", order.getId().toString()));

            // then
            resultActions.andExpect(status().isNoContent())
                    .andDo(document("orderDto-cancel",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("orderId").description("주문 ID")
                            )
                            ));
        }

        @Test
        void 주문을_하지_않고_취소할_경우_500() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(get(ORDER_API_PATH + "/cancel")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("orderId", "100"));

            // then
            resultActions.andExpect(status().isInternalServerError())
                    .andDo(document("orderDto-cancel-fail",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            requestParameters(
                                    parameterWithName("orderId").description("주문 ID")
                            ),
                            responseFields(
                                    getFailResponses()
                            )
                    ));
        }

        @Test
        void 주문한_내역_조회에_성공한다_200() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(get(ORDER_API_PATH + "/{pageNum}", 0)
                    .header(HttpHeaders.AUTHORIZATION, token));

            // then
            resultActions.andExpect(status().isOk())
                    .andDo(document("orderDto-find",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    fieldWithPath("pageNum").description("페이지 번호"),
                                    fieldWithPath("responses").type(JsonFieldType.ARRAY).description("주문 내역")
                            )
                            ));
        }

        @Test
        void 주문_상태_기준으로_내역_조회_성공한다_200() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(get(ORDER_API_PATH + "/{pageNum}/order-status", 0)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("status", OrderStatus.ORDER.name()));

            // then
            resultActions.andExpect(status().isOk())
                    .andDo(document("orderDto-status-find",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    fieldWithPath("pageNum").description("페이지 번호"),
                                    fieldWithPath("responses").type(JsonFieldType.ARRAY).description("주문 내역")
                            )
                    ));
        }

        @Test
        void 결제_상태_기준으로_내역_조회_성공한다_200() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(get(ORDER_API_PATH + "/{pageNum}/paid-status", 0)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .param("status", OrderPayStatus.YES.name()));

            // then
            resultActions.andExpect(status().isOk())
                    .andDo(document("orderDto-paid-status-find",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                            ),
                            responseFields(
                                    fieldWithPath("pageNum").description("페이지 번호"),
                                    fieldWithPath("responses").type(JsonFieldType.ARRAY).description("주문 내역")
                            )
                    ));
        }
    }
}
