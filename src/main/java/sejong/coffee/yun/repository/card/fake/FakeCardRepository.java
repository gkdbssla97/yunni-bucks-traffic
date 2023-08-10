package sejong.coffee.yun.repository.card.fake;

import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.repository.card.CardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static sejong.coffee.yun.domain.exception.ExceptionControl.NOT_FOUND_PAY_DETAILS;

public class FakeCardRepository implements CardRepository {

    private final AtomicLong atomicGeneratedId = new AtomicLong(0);
    private final List<Card> data = new ArrayList<>();

    @Override
    public Card save(Card card) {
        if (card.getId() == null || card.getId() == 0L) {
            Card buildCard = Card.builder()
                    .id(atomicGeneratedId.incrementAndGet())
                    .number(card.getNumber())
                    .cardPassword(card.getCardPassword())
                    .member(card.getMember())
                    .validThru(card.getValidThru())
                    .build();
            data.add(buildCard);
            return buildCard;
        }
        data.removeIf(element -> Objects.equals(element.getId(), card.getId()));
        data.add(card);
        return card;
    }

    @Override
    public Card findById(Long id) {
        return data.stream().filter(element -> element.getId().equals(id)).findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }

    @Override
    public List<Card> findAll() {
        return data;
    }

    @Override
    public Card findByMemberId(Long memberId) {
        return data.stream().filter(element -> element.getMember().getId().equals(memberId)).findAny()
                .orElseThrow(NOT_FOUND_PAY_DETAILS::paymentDetailsException);
    }
}
