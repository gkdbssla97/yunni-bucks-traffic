package sejong.coffee.yun.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.dto.card.CardDto;
import sejong.coffee.yun.repository.card.CardRepository;
import sejong.coffee.yun.repository.user.UserRepository;

import static sejong.coffee.yun.domain.user.Card.createCard;

@Service
@RequiredArgsConstructor
@Builder
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Card create(Long memberId, CardDto.Request request) {
        Member findMember = userRepository.findById(memberId);
        Card card = createCard(request.number(), request.validThru(), request.cardPassword(), findMember);
        return cardRepository.save(card);
    }

    public Card findById(Long id) {
        return cardRepository.findById(id);
    }

    public Card getByMemberId(Long memberId) {
        return cardRepository.findByMemberId(memberId);
    }

    @Transactional
    public void removeCard(Long memberId) {
        Card findCard = cardRepository.findByMemberId(memberId);
        cardRepository.delete(findCard);
    }
}
