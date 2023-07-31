package sejong.coffee.yun.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.domain.order.menu.*;
import sejong.coffee.yun.domain.user.Money;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class MenuListTest {

    private Menu menu1;
    private Menu menu2;
    private Menu menu3;

    @BeforeEach
    void init() {
        Nutrients nutrients = new Nutrients(80, 80, 80, 80);

        menu1 = new Beverage("커피", "에티오피아산 커피",
                Money.initialPrice(new BigDecimal(1000)), nutrients, MenuSize.M);
        menu2 = new Beverage("아이스티", "복숭아 아이스티",
                Money.initialPrice(new BigDecimal(1000)), nutrients, MenuSize.M);
        menu3 = new Bread("소라빵", "소라빵",
                Money.initialPrice(new BigDecimal(1000)), nutrients, MenuSize.M);
    }

    @Test
    void 메뉴들이_메뉴리스트에_담긴다() {
        // given
        MenuList menuList = new MenuList(new ArrayList<>());

        // when
        menuList.addMenu(menu1);

        // then
        assertThat(menuList.getMenus().size()).isEqualTo(1);
    }

    @Test
    void 메뉴리스트에_담긴_메뉴를_삭제한다_오브젝트_파라미터() {
        // given
        MenuList menuList = new MenuList(new ArrayList<>());
        menuList.addMenu(menu1);

        // when
        menuList.removeMenuBy(menu1);

        // then
        assertThat(menuList.getMenus().size()).isEqualTo(0);
    }

    @Test
    void 메뉴리스트에_담긴_메뉴를_삭제한다_인덱스() {
        // given
        MenuList menuList = new MenuList(new ArrayList<>());
        menuList.addMenu(menu1);

        // when
        menuList.removeMenuBy(0);

        // then
        assertThat(menuList.getMenus().size()).isEqualTo(0);
    }

    @Test
    void 메뉴리스트에_담긴_메뉴를_조회한다() {
        // given
        MenuList menuList = new MenuList(new ArrayList<>());
        menuList.addMenu(menu1);

        // when
        Menu findMenu = menuList.getMenuBy(0);

        // then
        assertThat(findMenu).isEqualTo(menu1);
    }
}