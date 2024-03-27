package sejong.coffee.yun.repository.menu.impl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.repository.menu.jpa.JpaMenuRepository;

import java.util.List;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_MENU;
import static sejong.coffee.yun.domain.order.menu.QMenu.menu;

@Repository
@Primary
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuRepositoryImpl implements MenuRepository {

    private final JpaMenuRepository jpaMenuRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    @Transactional
    public Menu save(Menu menu) {
        return jpaMenuRepository.save(menu);
    }

    @Override
    public Menu findById(Long id) {
        return jpaMenuRepository.findById(id)
                .orElseThrow(NOT_FOUND_MENU::notFoundException);
    }

    @Override
    public Menu findByIdForPessimisticLock(Long id) {
        return jpaMenuRepository.findByIdForPessimisticLock(id)
                .orElseThrow(NOT_FOUND_MENU::notFoundException);
    }

    @Override
    public Menu findByIdForOptimisticLock(Long id) {
        return jpaMenuRepository.findByIdForOptimisticLock(id)
                .orElseThrow(NOT_FOUND_MENU::notFoundException);
    }

    @Override
    public List<Menu> findAll() {
        return jpaMenuRepository.findAll();
    }

    @Override
    public Page<Menu> findAllMenusPaged(Pageable pageable) {
        List<Menu> menus = jpaQueryFactory.selectFrom(menu)
                .orderBy(menu.createAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> jpaQuery = jpaQueryFactory.select(menu.count())
                .from(menu);

        return PageableExecutionUtils.getPage(menus, pageable, jpaQuery::fetchOne);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        jpaMenuRepository.deleteById(id);
    }

    @Override
    public void clear() {
        jpaMenuRepository.deleteAll();
    }

    @Override
    public Menu findByTitle(String menuTitle) {
        return jpaMenuRepository.findByTitle(menuTitle)
                .orElseThrow(NOT_FOUND_MENU::notFoundException);
    }
}
