package sejong.coffee.yun.domain.pay;

import sejong.coffee.yun.domain.exception.ExceptionControl;

public enum PaymentCancelReason {
    NOT_SATISFIED_SERVICE("서비스 및 상품 불만족", "0001"),
    DELAY_DELIVERY("상품 배송 지연", "0002"),
    CHANGE_PRODUCT("색상 및 사이즈 변경", "0003"),
    MIS_ORDER("다른 상품 잘못 주문", "0004"),
    NETWORK_CANCEL("망취소", "9999");

    private final String description;
    private final String code;

    PaymentCancelReason(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static PaymentCancelReason getByCode(String code) {
        for (PaymentCancelReason type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw ExceptionControl.NOT_MATCHED_CANCEL_STATUS.paymentException();
    }
}
