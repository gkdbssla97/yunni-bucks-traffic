package sejong.coffee.yun.repository.menu.jpa;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sejong.coffee.yun.domain.order.menu.Menu;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface JpaMenuRepository extends JpaRepository<Menu, Long> {

    @NotNull
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Menu m where m.id = :id")
    Optional<Menu> findById(@NotNull @Param("id") Long id);

    Optional<Menu> findByTitle(@Param("title") String title);
}
