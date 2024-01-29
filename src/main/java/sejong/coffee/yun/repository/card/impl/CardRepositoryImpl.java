package sejong.coffee.yun.repository.card.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.card.jpa.JpaCardRepository;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final JpaCardRepository jpaCardRepository;

    @Override
    public Card save(Card card) {
        return jpaCardRepository.save(card);
    }

    @Override
    public Card findById(Long id) {
        return jpaCardRepository.findById(id)
                .orElseThrow(ExceptionControl.NOT_FOUND_REGISTER_CARD::cardException);
    }

    @Override
    public Card findByMemberId(Long memberId) {
        return jpaCardRepository.findByMemberId(memberId)
                .orElseThrow(ExceptionControl.NOT_FOUND_REGISTER_CARD::cardException);
    }

    @Override
    public void delete(Long id) {
        jpaCardRepository.delete(getCard(id));
    }

    @Override
    public void delete(Card card) {
        jpaCardRepository.delete(card);
    }

    private Card getCard(Long id) {
        return jpaCardRepository.findById(id).orElseThrow(ExceptionControl.NOT_FOUND_REGISTER_CARD::cardException);
    }

    @Override
    public void clear() {
        List<Card> cards = jpaCardRepository.findAll();
        cards.stream()
                .map(Card::getId)
                .map(this::getCard)
                .forEach(jpaCardRepository::delete);
    }

    @Override
    public List<Card> findAll() {
        return jpaCardRepository.findAll();
    }
}
