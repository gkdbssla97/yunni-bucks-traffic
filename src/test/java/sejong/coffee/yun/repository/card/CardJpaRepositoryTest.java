package sejong.coffee.yun.repository.card;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import sejong.coffee.yun.domain.pay.BeforeCreatedData;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.repository.card.jpa.JpaCardRepository;
import sejong.coffee.yun.repository.user.jpa.JpaUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = true)
@Deprecated
public class CardJpaRepositoryTest extends BeforeCreatedData {

    @Autowired private JpaCardRepository jpaCardRepository;
    @Autowired private JpaUserRepository jpaUserRepository;

    @Test
    void 카드를_저장한다() {
        //given
        //when
        Card save = jpaCardRepository.save(this.card);

        //then
        assertThat(card).isEqualTo(save);
    }

    @Test
    void 회원_id로_카드를_조회한다() {
        //given
        Member member = jpaUserRepository.save(this.member);
        jpaCardRepository.save(this.card);

        //when
        Optional<Member> findMember = jpaUserRepository.findById(member.getId());
        Optional<Card> byMemberId = jpaCardRepository.findByMemberId(findMember.get().getId());

        //then
        assertThat(card).isEqualTo(byMemberId.get());
    }

    @Test
    void 카드를_삭제한다() {
        //given
        Card save = jpaCardRepository.save(this.card);

        //when
        jpaCardRepository.delete(save);
        Optional<Card> byId = jpaCardRepository.findById(save.getId());

        //then
        assertThat(byId).isEmpty();
    }
}
