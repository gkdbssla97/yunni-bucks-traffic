package sejong.coffee.yun.message;

import lombok.Getter;

@Getter
public enum SuccessOrFailMessage {

    // success
    SUCCESS_DUPLICATE_NAME("이름 중복 체크에 성공하였습니다."),
    SUCCESS_DUPLICATE_EMAIL("이메일 중복 체크에 성공하였습니다."),
    SUCCESS_SIGN_OUT("로그아웃에 성공하였습니다."),
    SUCCESS_SIGN_IN("로그인에 성공하였습니다."),
    SUCCESS_DELETE_MEMBER("회원 탈퇴에 성공하였습니다."),

    // fail,
    FAILED_TIMEOUT("망취소: 타임아웃 5분 경과");
    private final String message;

    SuccessOrFailMessage(String message) {
        this.message = message;
    }
}
