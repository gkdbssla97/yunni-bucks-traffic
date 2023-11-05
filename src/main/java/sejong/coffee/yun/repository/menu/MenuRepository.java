package sejong.coffee.yun.repository.menu;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sejong.coffee.yun.domain.order.menu.Menu;

import java.util.List;

public interface MenuRepository {

    Menu save(Menu menu);
    Menu findById(Long id);
    List<Menu> findAll();
    Page<Menu> findAllMenusPaged(Pageable pageable);
    void delete(Long id);
    void clear();

    Menu findByTitle(String menuTitle);
}
