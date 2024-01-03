package sejong.coffee.yun.repository.card;

import sejong.coffee.yun.domain.user.Card;

import java.util.List;

public interface CardRepository {

    Card save(Card card);
    Card findById(Long id);
    Card findByMemberId(Long memberId);
    void delete(Long id);
    void delete(Card card);
    void clear();
    List<Card> findAll();
}
