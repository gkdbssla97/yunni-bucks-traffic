package sejong.coffee.yun.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuSize;
import sejong.coffee.yun.domain.order.menu.Nutrients;
import sejong.coffee.yun.domain.user.*;
import sejong.coffee.yun.dto.cart.CartDto;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.jwt.JwtProvider;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.CartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_CART;
import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU;
import static sejong.coffee.yun.domain.user.CartControl.SIZE;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    CartService cartService;
    @MockBean
    JwtProvider jwtProvider;
    @MockBean
    CustomMapper customMapper;

    private static Cart cart;
    private static String token;
    private static Member member;
    private static CartDto.Response response;
    private static MenuDto.Response menuResponse;
    private static Menu menu;

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
            .build()
        );

        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        menu = Beverage.builder()
                .id(1L)
                .description("에티오피아산 커피")
                .title("커피")
                .price(Money.initialPrice(new BigDecimal(1000)))
                .nutrients(nutrients)
                .menuSize(MenuSize.M)
                .now(LocalDateTime.now())
                .build();

        menuResponse = new MenuDto.Response(menu);

        cart = Cart.builder()
                .member(member)
                .cartItems(new ArrayList<>())
                .build();

        token = "bearer accessToken";
        response = new CartDto.Response(1L, cart.getMember().getId(), List.of(menuResponse));
    }

    @Test
    void 카트_생성() throws Exception {
        // given
        given(cartService.createCart(anyLong())).willReturn(cart);
        given(customMapper.map(any(), any())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/carts")
                .header(HttpHeaders.AUTHORIZATION, token));

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().json(toJson(response)));
    }

    @Test
    void 카트_조회() throws Exception {
        // given
        given(cartService.getCartByMember(anyLong())).willReturn(cart);
        given(customMapper.map(any(), any())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/carts")
                .header(HttpHeaders.AUTHORIZATION, token));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(response)));
    }

    @Test
    void 카트_삭제() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/carts")
                .header(HttpHeaders.AUTHORIZATION, token));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    void 메뉴_추가() throws Exception {
        // given
        given(cartService.addMenu(anyLong(), anyLong())).willReturn(cart);
        given(customMapper.map(any(), any())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/carts/menu")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("menuId", "1"));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(response)));
    }

    @Test
    void 메뉴_삭제() throws Exception {
        // given
        given(cartService.removeMenu(anyLong(), anyInt())).willReturn(cart);
        given(customMapper.map(any(), any())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/carts/menu")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("menuIdx", "1"));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(response)));
    }

    @Test
    void 카트에서_메뉴_꺼내기() throws Exception {
        // given
        given(cartService.getMenu(anyLong(), anyInt())).willReturn(menu);
        given(customMapper.map(any(), any())).willReturn(menuResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/carts/menu")
                .param("menuIdx", "1")
                .header(HttpHeaders.AUTHORIZATION, token));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().json(toJson(menuResponse)));
    }

    @Test
    void 카트가_생성되지_않았는데_메뉴를_추가_할_경우() throws Exception {
        // given
        given(cartService.addMenu(anyLong(), anyLong())).willThrow(NOT_FOUND_CART.notFoundException());

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/carts/menu")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("menuId", "1"));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void 카트가_비어있는_상태에서_메뉴를_삭제할_경우() throws Exception {
        // given
        given(cartService.removeMenu(anyLong(), anyInt())).willThrow(NOT_FOUND_MENU.notFoundException());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/carts/menu")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("menuIdx", "1"));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void 카트가_비어있는_상태에서_메뉴를_가져올_경우() throws Exception {
        // given
        given(cartService.getMenu(anyLong(), anyInt())).willThrow(NOT_FOUND_MENU.notFoundException());

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/carts/menu")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("menuIdx", "1"));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void 카트는_메뉴를_담을때_10개를_초과할_수_없다() throws Exception {
        // given
        given(cartService.addMenu(anyLong(), anyLong()))
                .willThrow(new RuntimeException("카트는 메뉴를 " + SIZE.getSize() + "개만 담을 수 있습니다."));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/carts/menu")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("menuId", "1"));

        // then
        resultActions.andExpect(status().isInternalServerError());
    }

    private String toJson(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }
}