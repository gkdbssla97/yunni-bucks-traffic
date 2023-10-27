package sejong.coffee.yun.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.custom.annotation.MemberId;
import sejong.coffee.yun.custom.annotation.SlackNotification;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.dto.user.UserDto;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.UserService;
import sejong.coffee.yun.util.jwt.JwtUtil;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final CustomMapper customMapper;

    @PostMapping("")
    @SlackNotification
    ResponseEntity<UserDto.Response> signUp(@RequestBody @Valid UserDto.Sign.Up.Request request) {

        log.info("request = {}", request.email());

        Member member = userService.signUp(
                request.name(),
                request.email(),
                request.password(),
                request.address()
        );

        UserDto.Response response = customMapper.map(member, UserDto.Response.class);


        log.info("response = {}", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sign-in")
    @SlackNotification
    ResponseEntity<Void> signIn(@RequestBody @Valid UserDto.Sign.In.Request request) {
        String accessToken = userService.signIn(request.email(), request.password());

        UserDto.Sign.In.Response response = customMapper.map(accessToken, UserDto.Sign.In.Response.class);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, JwtUtil.getBearerToken(response.accessToken()))
                .build();
    }

    @PostMapping("/sign-out")
    ResponseEntity<String> signOut(@RequestHeader(AUTHORIZATION) String accessToken, @MemberId Long memberId) {
        log.info("log out = {} {}", accessToken, memberId);

        String s = userService.signOut(accessToken, memberId);

        return ResponseEntity.ok(s);
    }

    @GetMapping("")
    ResponseEntity<UserDto.Response> findById(@MemberId Long memberId) {
        Member member = userService.findMember(memberId);

        UserDto.Response response = customMapper.map(member, UserDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    ResponseEntity<List<UserDto.Response>> findAll() {
        List<Member> members = userService.findAll();

        List<UserDto.Response> collect = members.stream()
                .map(UserDto.Response::new)
                .toList();

        return ResponseEntity.ok(collect);
    }

    @DeleteMapping("")
    ResponseEntity<Void> delete(@MemberId Long memberId) {
        userService.deleteMember(memberId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/email")
    ResponseEntity<UserDto.Response> updateEmail(@MemberId Long memberId,
                                                        @RequestBody @Valid UserDto.Update.Email.Request request) {

        Member member = userService.updateEmail(memberId, request.updateEmail());

        UserDto.Response response = customMapper.map(member, UserDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/name")
    ResponseEntity<UserDto.Response> updateName(@MemberId Long memberId,
                                                       @RequestBody @Valid UserDto.Update.Name.Request request) {

        Member member = userService.updateName(memberId, request.updateName());

        UserDto.Response response = customMapper.map(member, UserDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    ResponseEntity<UserDto.Response> updatePassword(@MemberId Long memberId,
                                                           @RequestBody @Valid UserDto.Update.Password.Request request) {

        Member member = userService.updatePassword(memberId, request.updatePassword());

        UserDto.Response response = customMapper.map(member, UserDto.Response.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/duplication/name", produces = "application/json;charset=UTF-8")
    ResponseEntity<String> duplicateName(@RequestParam("name") String name) {
        String s = userService.duplicateName(name);

        return ResponseEntity.ok(s);
    }

    @GetMapping(value = "/duplication/email", produces = "application/json;charset=UTF-8")
    ResponseEntity<String> duplicateEmail(@RequestParam("email") String email) {
        String s = userService.duplicateEmail(email);

        return ResponseEntity.ok(s);
    }
}
