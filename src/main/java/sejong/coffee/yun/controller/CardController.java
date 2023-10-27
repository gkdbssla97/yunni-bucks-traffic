package sejong.coffee.yun.controller;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.custom.annotation.MemberId;
import sejong.coffee.yun.custom.annotation.SlackNotification;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.CardService;

import static sejong.coffee.yun.dto.card.CardDto.Request;
import static sejong.coffee.yun.dto.card.CardDto.Response;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
@Slf4j
@Builder

public class CardController {

    private final CardService cardService;
    private final CustomMapper customMapper;

    @PostMapping("/")
    @SlackNotification
    public ResponseEntity<Response> createCard(@MemberId Long memberId,
                                               @RequestBody Request request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customMapper.map(cardService.create(memberId, request), Response.class));
    }

    @GetMapping("/")
    @SlackNotification
    public ResponseEntity<Response> getByMemberId(@MemberId Long memberId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customMapper.map(cardService.getByMemberId(memberId), Response.class));
    }

    @DeleteMapping("/")
    @SlackNotification
    ResponseEntity<Void> removeCard(@MemberId Long memberId) {
        cardService.removeCard(memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/card-number-valid")
    @SlackNotification
    public ResponseEntity<Boolean> test() throws Exception {
        throw new Exception();
//        return ResponseEntity.ok(true);

    }

    @GetMapping("/hi1")
    @SlackNotification
    public ResponseEntity<Boolean> test1() {
//        throw new Exception();
        return ResponseEntity.ok(true);
    }
}
