package sejong.coffee.yun.repository.order.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sejong.coffee.yun.domain.order.Order;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByCartMemberId(Long memberId);
    Optional<Order> findByCartMemberId(Long memberId);
    Page<Order> findAllByMemberIdOrderByCreateAt(Pageable pageable, Long memberId);

    @NotNull
    @Query("select o from Order o join fetch o.cart where o.id = :id")
    Optional<Order> findById(@NotNull @Param("id") Long id);
}
