package sejong.coffee.yun.repository.cart.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sejong.coffee.yun.domain.user.Cart;

import java.util.Optional;

public interface JpaCartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"member", "cartItems"})
    @Query("select c from Cart c where c.member.id = :memberId")
    Optional<Cart> findByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberId(Long memberId);
}
