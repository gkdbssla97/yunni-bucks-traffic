package sejong.coffee.yun.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import sejong.coffee.yun.domain.order.Order;
import sejong.coffee.yun.domain.order.OrderPayStatus;
import sejong.coffee.yun.domain.order.OrderStatus;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.dto.order.OrderDto;
import sejong.coffee.yun.dto.order.OrderPageDto;
import sejong.coffee.yun.jwt.JwtProvider;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.CartService;
import sejong.coffee.yun.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    OrderService orderService;
    @MockBean
    CartService cartService;
    @MockBean
    CustomMapper customMapper;
    @MockBean
    JwtProvider jwtProvider;

    static Menu menu;
    static Member member;
    static String token;
    static OrderDto.Response response;
    static Order order;
    static Cart cart;
    static PageImpl<Order> orderPage;
    static OrderPageDto.Response pageResponse;

    @BeforeAll
    static void init() {
        Address address = Address.builder()
                .city("서울시")
                .district("광진구")
                .detail("화양동")
                .zipCode("123-123")
                .build();

        member = Member.from(1L, Member.builder()
                .email("qwer1234@naver.com")
                .password("qwer1234@A")
                .name("홍길동")
                .userRank(UserRank.BRONZE)
                .money(Money.ZERO)
                .address(address)
                .orderCount(0)
                .build()
        );

        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        menu = Beverage.builder()
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        token = "bearer accessToken";

        CartItem cartItem = CartItem.builder().menu(menu).build();

        cart = Cart.builder()
                .member(member)
                .cartItems(List.of(cartItem))
                .build();

        order = Order.from(1L,
                Order.createOrder(member, cart, menu.getPrice(), LocalDateTime.now()));

        MenuDto.Response menuResponse = new MenuDto.Response(1L, menu.getTitle(), menu.getDescription(), menu.getPrice(), menu.getNutrients(),
                menu.getMenuSize(), menu.getStock(), menu.getViewCount(), menu.getOrderCount());

        response = new OrderDto.Response(1L, order.getName(), List.of(menuResponse),
                order.getStatus(), order.getOrderPrice(), order.getPayStatus());

        PageRequest pr = PageRequest.of(0, 10);
        List<Order> orders = List.of(order);
        orderPage = new PageImpl<>(orders, pr, orders.size());

        pageResponse = new OrderPageDto.Response(orderPage);
    }

    @Test
    void 주문() throws Exception {
        // given
        given(cartService.findCartByMember(anyLong())).willReturn(cart);
        given(orderService.order(anyLong(), any())).willReturn(order);
        given(customMapper.map(any(), any())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, token));

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().json(toJson(response)));
    }

    @Test
    void 주문_취소() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/orders/cancel")
                .param("orderId", "1"));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    void 유저의_주문내역() throws Exception {
        // given
        given(orderService.findAllByMemberId(any(), anyLong())).willReturn(orderPage);
        given(customMapper.map(any(), any())).willReturn(pageResponse);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/orders/{pageNum}", 0)
                .header(HttpHeaders.AUTHORIZATION, token));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(pageResponse)));
    }

    @Test
    void 유저의_주문내역_주문상태() throws Exception {
        // given
        given(orderService.findAllByMemberIdAndOrderStatus(any(), anyLong(), any())).willReturn(orderPage);
        given(customMapper.map(any(), any())).willReturn(pageResponse);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/orders/{pageNum}/order-status", 0)
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("status", OrderStatus.ORDER.name()));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(pageResponse)));
    }

    @Test
    void 유저의_주문상태_결제상태() throws Exception {
        // given
        given(orderService.findAllByMemberIdAndPayStatus(any(), anyLong(), any())).willReturn(orderPage);
        given(customMapper.map(any(), any())).willReturn(pageResponse);

        // when
        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/orders/{pageNum}/paid-status", 0)
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("status", OrderPayStatus.YES.name()));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(pageResponse)));
    }

    private String toJson(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }
}