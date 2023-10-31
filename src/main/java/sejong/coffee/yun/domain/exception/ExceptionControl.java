package sejong.coffee.yun.domain.exception;

import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Getter
public enum ExceptionControl {

    // Order, member, menuList
    DUPLICATE("중복되었습니다."),
    DUPLICATE_USER_NAME("이름이 중복되었습니다."),
    DUPLICATE_USER_EMAIL("이메일이 중복되었습니다."),
    NOT_MATCH_USER("아이디 혹은 비밀번호가 다릅니다."),
    NOT_FOUND_ORDER("주문 내역이 존재하지 않습니다."),
    NOT_FOUND_USER("유저가 존재하지 않습니다."),
    EMPTY_MENUS("메뉴리스트가 비어 있습니다."),
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    FAIL_DELETE_MEMBER("회원 탈퇴에 실패하였습니다."),
    NOT_FOUND_MENU_LIST("메뉴 리스트를 찾을 수 없습니다."),
    NOT_FOUND_MENU("메뉴를 찾을 수 없습니다."),
    NOT_FOUND_CART("장바구니를 찾을 수 없습니다."),
    INPUT_ERROR("입력 값을 다시 한번 확인해주세요"),
    DO_NOT_PAID("결제를 하지 않은 상태에서 배달은 불가능합니다."),
    NOT_FOUND_DELIVERY("배달 내역을 찾을 수 없습니다."),
    DELIVERY_CANCEL_EXCEPTION("취소할 수 없습니다."),
    DELIVERY_EXCEPTION("배송할 수 없습니다."),
    DELIVERY_COMPLETE_EXCEPTION("배송 완료를 할 수 없습니다."),
    NOT_FOUND_MENU_REVIEW("메뉴 리뷰를 찾을 수 없습니다."),

    // Menu
    NOT_FOUND_MENU_THUMBNAIL("메뉴 썸네일을 찾을 수 없습니다."),
    QUANTITY_INDEX_OF_BOUND_ERROR("수량은 0 미만으로 내릴 수 없습니다."),
    INSUFFICIENT_STOCK_QUANTITY("재고가 부족해 주문을 할 수 없습니다."),
    INVALID_MENU_TYPE("해당 메뉴 종류는 없습니다."),

    // Card
    INVALID_CARD_EXPIRATION_DATE("카드 유효기간이 올바르지 않습니다."),
    NOT_FOUND_REGISTER_CARD("등록된 카드가 존재하지 않습니다."),
    INVALID_CARD_NUMBER_LENGTH("카드번호가 유효하지 않습니다.(숫자로 20자 내외)"),
    INVALID_CARD_PASSWORD("카드 비밀번호가 유효하지 않습니다.(4자리 입력)"),

    // Pay
    NOT_FOUND_PAYMENT_TYPE("올바르지 않은 결제 수단입니다."),
    NOT_FOUND_ORDER_ID_FOR_PAYMENT("결제를 진행할 주문 내역이 없습니다."),
    NOT_MATCHED_CANCEL_STATUS("결제취소 사유가 올바르지 않습니다."),

    // PayDetails
    NOT_FOUND_PAY_DETAILS("해당 결제내역이 존재하지 않습니다."),
    NOT_MATCHED_PAYMENT_KEY("결제 키가 일치하지 않습니다."),

    // OCR
    NOT_FOUND_OCR_RESPONSE_BODY("올바른 OCR 응답을 받을 수 없습니다."),
    INVALID_FILE_EXTENSION_FORMAT("올바른 파일 경로가 아닙니다.");

    private final String message;

    ExceptionControl(String message) {
        this.message = message;
    }

    public MenuException throwException() {
        return new MenuException(this.message);
    }

    public NotFoundException notFoundException() {
        return new NotFoundException(this.message);
    }

    public NotMatchUserException notMatchUserException() {
        return new NotMatchUserException(this.message);
    }

    public PaymentException paymentException() {
        return new PaymentException(this.message);
    }

    public PaymentDetailsException paymentDetailsException() {
        return new PaymentDetailsException(this.message);
    }

    public CardException cardException() {
        return new CardException(this.message);
    }

    public DuplicatedException duplicatedException() {
        return new DuplicatedEmailException(this.message);
    }

    public DuplicatedEmailException duplicatedEmailException() {
        return new DuplicatedEmailException(this.message);
    }

    public DuplicatedNameException duplicatedNameException() {
        return new DuplicatedNameException(this.message);
    }

    public JwtException tokenExpiredException() {
        return new JwtException(this.message);
    }

    public MenuException menuException() {
        return new MenuException(this.message);
    }

    public CardException ocrException() {
        return new CardException(this.message);
    }
}
