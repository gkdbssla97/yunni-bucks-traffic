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
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Bread;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
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
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Import(TestConfig.class)
public class MainIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    protected JwtProvider jwtProvider;

    public static final String MEMBER_API_PATH = "/api/members";
    public static final String ORDER_API_PATH = "/api/orders";

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

    public Member member() {
        return Member.builder()
                .name("홍길동")
                .userRank(UserRank.BRONZE)
                .password("qwer1234@A")
                .money(Money.ZERO)
                .coupon(null)
                .email("qwer1234@naver.com")
                .address(new Address("서울시", "광진구", "능동로 110 세종대학교", "100- 100"))
                .orderCount(0)
                .build();
    }

    public Order order(Member member, Cart cart) {
        return Order.createOrder(member, cart.getMenuList(), Money.ZERO, LocalDateTime.now());
    }

    public Cart cart(Member member) {
        return Cart.builder()
                .menuList(new ArrayList<>())
                .member(member)
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

    protected static List<FieldDescriptor> getUserFailResponses() {
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
}