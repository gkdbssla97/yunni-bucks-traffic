package sejong.coffee.yun.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.order.menu.Beverage;
import sejong.coffee.yun.domain.order.menu.Bread;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.domain.order.menu.MenuType;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.util.wrapper.RestPage;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;

    public Menu create(MenuDto.Request request) {
        Menu menu = createMenu(request);

        return menuRepository.save(menu);
    }

    private Menu createMenu(MenuDto.Request request) {
        MenuType menuType = request.menuType();
        if (menuType == MenuType.BEVERAGE) {
            return Beverage.builder()
                    .title(request.title())
                    .description(request.description())
                    .price(request.price())
                    .nutrients(request.nutrients())
                    .menuSize(request.menuSize())
                    .stock(request.stock())
                    .build();
        } else if (menuType == MenuType.BREAD) {
            return Bread.builder()
                    .title(request.title())
                    .description(request.description())
                    .price(request.price())
                    .nutrients(request.nutrients())
                    .menuSize(request.menuSize())
                    .stock(request.stock())
                    .build();
        }
        throw ExceptionControl.INVALID_MENU_TYPE.menuException();
    }

    @Transactional
    @Cacheable(value = "Contents", cacheManager = "cacheManager")
    public RestPage<MenuDto.Response> findAllByCaching(Pageable pageable) {
        Page<Menu> allMenusPaged = menuRepository.findAllMenusPaged(pageable);
        return new RestPage<>(allMenusPaged.map(MenuDto.Response::new));
    }

    public Page<MenuDto.Response> findAll(Pageable pageable) {
        Page<Menu> allMenusPaged = menuRepository.findAllMenusPaged(pageable);
        return allMenusPaged.map(MenuDto.Response::new);
    }
}
