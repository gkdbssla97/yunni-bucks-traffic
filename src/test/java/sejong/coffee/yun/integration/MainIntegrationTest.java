package sejong.coffee.yun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sejong.coffee.yun.config.TestConfig;
import sejong.coffee.yun.domain.delivery.DeliveryStatus;
import sejong.coffee.yun.domain.delivery.DeliveryType;
import sejong.coffee.yun.domain.delivery.NormalDelivery;
import sejong.coffee.yun.domain.delivery.ReserveDelivery;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.menu.*;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.dto.delivery.DeliveryDto;
import sejong.coffee.yun.dto.review.menu.MenuReviewDto;
import sejong.coffee.yun.dto.user.UserDto;
import sejong.coffee.yun.jwt.JwtProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 필요한 부분은 상속하여 사용하길 바람.
 * 유저, 주문에 대한 값들이 포함되어있음.
 */
@SpringBootTest(properties = "schedules.cron.test=1 * * * * *")
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Import(TestConfig.class)
public class MainIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JwtProvider jwtProvider;

    public static final String MEMBER_API_PATH = "/api/members";
    public static final String ORDER_API_PATH = "/api/orders";
    public static final String CART_API_PATH = "/api/carts";
    public static final String DELIVERY_API_PATH = "/api/deliveries";
    public static final String MENU_REVIEW_API_PATH = "/api";
    public static final String MENU_THUMBNAIL_API_PATH = "/api";

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    public UserDto.Sign.In.Request signInRequest() {
        return new UserDto.Sign.In.Request(member().getEmail(), member().getPassword());
    }

    public UserDto.Sign.Up.Request signUpRequest() {
        return new UserDto.Sign.Up.Request(member().getName(), member().getEmail(), member().getPassword(), member().getAddress());
    }

    public UserDto.Sign.Up.Request badSignUpRequest() {
        return new UserDto.Sign.Up.Request("fds", "gfsdggfd", "fgfd", member().getAddress());
    }

    public UserDto.Sign.In.Request badSignInRequest(String email, String pwd) {
        return new UserDto.Sign.In.Request(email, pwd);
    }

    public UserDto.Update.Email.Request updateEmailRequest() {
        return new UserDto.Update.Email.Request("asdf1234@naver.com");
    }

    public UserDto.Update.Name.Request updateNameRequest() {
        return new UserDto.Update.Name.Request("홍홍길동");
    }

    public UserDto.Update.Password.Request updatePasswordRequest() {
        return new UserDto.Update.Password.Request("adsf1234@A");
    }

    public UserDto.Update.Name.Request badUpdateRequest() {
        return new UserDto.Update.Name.Request("gdfg");
    }

    public DeliveryDto.ReserveRequest reserveRequest(Long orderId) {
        return new DeliveryDto.ReserveRequest(orderId, member().getAddress(), LocalDateTime.now(), LocalDateTime.now(), DeliveryType.RESERVE);
    }

    public DeliveryDto.NormalRequest normalRequest(Long orderId) {
        return new DeliveryDto.NormalRequest(orderId, member().getAddress(), LocalDateTime.now(), DeliveryType.NORMAL);
    }

    public DeliveryDto.UpdateAddressRequest updateAddressRequest(Long deliveryId, Address address) {
        return new DeliveryDto.UpdateAddressRequest(deliveryId, address, LocalDateTime.now());
    }

    public MenuReviewDto.Request menuReviewRequest() {
        return new MenuReviewDto.Request("맛있어요");
    }

    public MenuReviewDto.Request menuReviewUpdateRequest() {
        return new MenuReviewDto.Request("맛없어요");
    }

    public Member member() {
        return Member.builder()
                .name("홍길동")
                .userRank(UserRank.BRONZE)
                .password("qwer1234@A")
                .money(Money.ZERO)
                .coupon(null)
                .email("qwer1234@naver.com")
                .address(new Address("서울시", "광진구", "능동로 110 세종대학교", "100-100"))
                .orderCount(0)
                .build();
    }

    public Order order(Cart cart) {
        return Order.createOrder(cart, Money.ZERO, LocalDateTime.now());
    }

    public Cart cart(Member member) {
        return Cart.builder()
                .cartItems(new ArrayList<>())
                .member(member)
                .build();
    }

    public CartItem cartItem(Cart cart, Menu menu) {
        return CartItem.builder()
                .cart(cart)
                .menu(menu)
                .build();
    }

    public Bread bread() {
        return Bread.builder()
                .title("빵")
                .description("성심당과 콜라보한 빵")
                .nutrients(new Nutrients(80, 80, 80, 80))
                .now(LocalDateTime.now())
                .menuSize(MenuSize.M)
                .price(Money.initialPrice(new BigDecimal(4000)))
                .build();
    }

    public Bread bread(int num) {
        return Bread.builder()
                .id((long) (num))
                .title("빵" + num)
                .description("성심당과 콜라보한 빵")
                .nutrients(new Nutrients(num, num, num, num))
                .now(LocalDateTime.now())
                .menuSize(MenuSize.M)
                .price(Money.initialPrice(new BigDecimal(4000)))
                .stock(100)
                .build();
    }

    public Beverage beverage() {
        return Beverage.builder()
                .title("커피")
                .description("에티오피아 산 숙성 커피")
                .nutrients(new Nutrients(80, 80, 80, 80))
                .now(LocalDateTime.now())
                .menuSize(MenuSize.S)
                .price(Money.initialPrice(new BigDecimal(3000)))
                .build();
    }

    public ReserveDelivery reserveDelivery(Order order, LocalDateTime reserveAt) {
        return ReserveDelivery.builder()
                .type(DeliveryType.RESERVE)
                .order(order)
                .status(DeliveryStatus.READY)
                .reserveAt(reserveAt)
                .address(new Address("서울시", "광진구", "능동로 110 세종대학교", "100- 100"))
                .now(LocalDateTime.now())
                .build();
    }

    public NormalDelivery normalDelivery(Order order) {
        return NormalDelivery.builder()
                .type(DeliveryType.NORMAL)
                .status(DeliveryStatus.READY)
                .order(order)
                .address(new Address("서울시", "광진구", "능동로 110 세종대학교", "100- 100"))
                .now(LocalDateTime.now())
                .build();
    }

    /**
     * 로그인 모듈
     * @return access token
     * @throws Exception
     */

    public String signInModule() throws Exception {
        String s = toJson(signInRequest());

        return mockMvc.perform(post(MEMBER_API_PATH + "/sign-in")
                .content(s)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }

    public String toJson(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }

    public <T> T toObject(String s, Class<T> c) throws JsonProcessingException {
        return objectMapper.readValue(s, c);
    }


    protected static List<FieldDescriptor> getUserResponses() {
        return List.of(
                fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("유저 id"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일"),
                fieldWithPath("address.city").type(JsonFieldType.STRING).description("시"),
                fieldWithPath("address.district").type(JsonFieldType.STRING).description("군/구"),
                fieldWithPath("address.detail").type(JsonFieldType.STRING).description("상세 주소"),
                fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편 번호"),
                fieldWithPath("userRank").type(JsonFieldType.STRING).description("유저 등급"),
                fieldWithPath("money.totalPrice").type(JsonFieldType.NUMBER).description("유저가 소유한 잔액"),
                fieldWithPath("createAt").description("생성 시간"),
                fieldWithPath("updateAt").description("수정 시간")
        );
    }

    protected static List<FieldDescriptor> getFailResponses() {
        return List.of(
                fieldWithPath("status").type(JsonFieldType.STRING).description("상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메세지")
        );
    }

    protected static List<FieldDescriptor> getUserRequests() {
        return List.of(
                fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("유저 비밀번호"),
                fieldWithPath("address.city").type(JsonFieldType.STRING).description("시"),
                fieldWithPath("address.district").type(JsonFieldType.STRING).description("군/구"),
                fieldWithPath("address.detail").type(JsonFieldType.STRING).description("상세 주소"),
                fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편 번호")
        );
    }

    protected static List<FieldDescriptor> getOrderResponse() {
        return List.of(
                fieldWithPath("orderId").description("주문 ID"),
                fieldWithPath("name").description("주문 이름"),
                fieldWithPath("menuList").type(JsonFieldType.ARRAY).description("메뉴 리스트"),
                fieldWithPath("menuList[]").description("메뉴 리스트"),
                fieldWithPath("menuList[].menuId").description("메뉴 ID"),
                fieldWithPath("menuList[].title").description("메뉴 제목"),
                fieldWithPath("menuList[].description").description("메뉴 설명"),
                fieldWithPath("menuList[].price.totalPrice").description("메뉴 가격"),
                fieldWithPath("menuList[].nutrients").description("영양 정보"),
                fieldWithPath("menuList[].menuSize").description("메뉴 크기"),
                fieldWithPath("status").description("주문 상태"),
                fieldWithPath("money.totalPrice").description("총 주문 가격"),
                fieldWithPath("payStatus").description("결제 상태")
        );
    }

    protected static List<FieldDescriptor> getOrderPageResponse() {
        return List.of(
                fieldWithPath("orderId").description("주문 ID"),
                fieldWithPath("name").description("주문 이름"),
                fieldWithPath("menuList").type(JsonFieldType.ARRAY).description("메뉴 리스트"),
                fieldWithPath("menuList[]").description("메뉴 리스트"),
                fieldWithPath("menuList[].menuId").description("메뉴 ID"),
                fieldWithPath("menuList[].title").description("메뉴 제목"),
                fieldWithPath("menuList[].description").description("메뉴 설명"),
                fieldWithPath("menuList[].price.totalPrice").description("메뉴 가격"),
                fieldWithPath("menuList[].nutrients").description("영양 정보"),
                fieldWithPath("menuList[].menuSize").description("메뉴 크기"),
                fieldWithPath("status").description("주문 상태"),
                fieldWithPath("money.totalPrice").description("총 주문 가격"),
                fieldWithPath("payStatus").description("결제 상태")
        );
    }

    protected static List<FieldDescriptor> getDeliveryPageResponse() {
        return List.of(
                fieldWithPath("pageNum").description("페이지 번호"),
                fieldWithPath("responses").description(JsonFieldType.ARRAY).description("배달 리스트")
        );
    }

    protected static List<FieldDescriptor> getDeliveryResponse() {
        return List.of(
                fieldWithPath("deliveryId").type(JsonFieldType.NUMBER).description("배달 ID"),
                fieldWithPath("orderName").description("주문 명"),
                fieldWithPath("createAt").description("생성일"),
                fieldWithPath("updateAt").description("수정일"),
                fieldWithPath("address.city").type(JsonFieldType.STRING).description("시"),
                fieldWithPath("address.district").type(JsonFieldType.STRING).description("군/구"),
                fieldWithPath("address.detail").type(JsonFieldType.STRING).description("상세 주소"),
                fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편 번호"),
                fieldWithPath("type").description("배달 타입"),
                fieldWithPath("status").description("배달 상태")
        );
    }

    protected static List<FieldDescriptor> getDeliveryNormalRequest() {
        return List.of(
                fieldWithPath("orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                fieldWithPath("address.city").type(JsonFieldType.STRING).description("시"),
                fieldWithPath("address.district").type(JsonFieldType.STRING).description("군/구"),
                fieldWithPath("address.detail").type(JsonFieldType.STRING).description("상세 주소"),
                fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편 번호"),
                fieldWithPath("now").description("생성/수정일"),
                fieldWithPath("type").description("배달 타입")
        );
    }

    protected static List<FieldDescriptor> getDeliveryReserveRequest() {
        return List.of(
                fieldWithPath("orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                fieldWithPath("address.city").type(JsonFieldType.STRING).description("시"),
                fieldWithPath("address.district").type(JsonFieldType.STRING).description("군/구"),
                fieldWithPath("address.detail").type(JsonFieldType.STRING).description("상세 주소"),
                fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편 번호"),
                fieldWithPath("now").description("생성/수정일"),
                fieldWithPath("reserveDate").description("예약일"),
                fieldWithPath("type").description("배달 타입")
        );
    }

    protected static List<FieldDescriptor> getUpdateAddressRequest() {
        return List.of(
                fieldWithPath("deliveryId").type(JsonFieldType.NUMBER).description("배달 ID"),
                fieldWithPath("address.city").type(JsonFieldType.STRING).description("시"),
                fieldWithPath("address.district").type(JsonFieldType.STRING).description("군/구"),
                fieldWithPath("address.detail").type(JsonFieldType.STRING).description("상세 주소"),
                fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편 번호"),
                fieldWithPath("now").description("수정일")
        );
    }


    protected static List<FieldDescriptor> getMenuReviewResponse() {
        return List.of(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                fieldWithPath("userName").type(JsonFieldType.STRING).description("유저 이름"),
                fieldWithPath("userRank").description("유저 랭크"),
                fieldWithPath("comment").description("코멘트")
        );
    }

    protected static List<FieldDescriptor> getMenuReviewUpdateResponse() {
        return List.of(
                fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                fieldWithPath("comment").type(JsonFieldType.STRING).description("코멘트")
        );
    }
}
