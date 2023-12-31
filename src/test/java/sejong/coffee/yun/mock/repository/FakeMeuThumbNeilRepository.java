package sejong.coffee.yun.mock.repository;

import org.springframework.stereotype.Repository;
import sejong.coffee.yun.domain.order.menu.MenuThumbnail;
import sejong.coffee.yun.repository.thumbnail.ThumbNailRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU_THUMBNAIL;

@Repository
public class FakeMeuThumbNeilRepository implements ThumbNailRepository {

    private final List<MenuThumbnail> thumbnails = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong id = new AtomicLong(0);

    @Override
    public MenuThumbnail save(MenuThumbnail menuThumbnail) {
        if(menuThumbnail.getId() == null || menuThumbnail.getId() == 0L) {
            MenuThumbnail newThumbNail = MenuThumbnail.from(id.incrementAndGet(), menuThumbnail);

            thumbnails.add(newThumbNail);

            return newThumbNail;
        }
        thumbnails.removeIf(t -> Objects.equals(t.getId(), menuThumbnail.getId()));
        thumbnails.add(menuThumbnail);

        return menuThumbnail;
    }

    @Override
    public List<MenuThumbnail> findAllByMenuId(Long menuId) {
        return thumbnails.stream()
                .filter(thumbnail -> Objects.equals(thumbnail.getMenu().getId(), menuId))
                .toList();
    }

    @Override
    public MenuThumbnail findById(Long thumbnailId) {
        return thumbnails.stream()
                .filter(thumbnail -> Objects.equals(thumbnail.getId(), thumbnailId))
                .findAny()
                .orElseThrow(NOT_FOUND_MENU_THUMBNAIL::notFoundException);
    }

    @Override
    public MenuThumbnail findByMenuId(Long menuId) {
        return thumbnails.stream()
                .filter(thumbnail -> Objects.equals(thumbnail.getMenu().getId(), menuId))
                .findAny()
                .orElseThrow(NOT_FOUND_MENU_THUMBNAIL::notFoundException);
    }

    @Override
    public void delete(Long thumbnailId) {
        thumbnails.removeIf(thumbnail -> Objects.equals(thumbnail.getId(), thumbnailId));
    }

    @Override
    public void deleteByMenuId(Long menuId) {
        thumbnails.removeIf(thumbnail -> Objects.equals(thumbnail.getMenu().getId(), menuId));
    }

    public void clear() {
        thumbnails.clear();
    }
}
