package sejong.coffee.yun.integration;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import sejong.coffee.yun.domain.user.Card;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.dto.card.CardDto;
import sejong.coffee.yun.dto.ocr.OcrDto;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.infra.port.UuidHolder;

import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class SubIntegrationTest extends MainIntegrationTest {

    public static final String CARD_API_PATH = "/api/card";
    public static final String OCR_API_PATH = "/api/ocr";
    public static final String PAY_API_PATH = "/api/payments";

    public CardDto.Request registerRequest() {
        return CardDto.Request.create(card().getNumber(), card().getCardPassword(), card().getValidThru());
    }

    public CardDto.Request badCardNumberRegisterRequest() {
        return CardDto.Request.create("9446032384143059944603238", card().getCardPassword(), card().getValidThru());
    }

    public CardDto.Request badCardPasswordRegisterRequest() {
        return CardDto.Request.create(card().getNumber(), "12345", card().getValidThru());
    }

    public CardDto.Request badCardValidDateRegisterRequest() {
        return CardDto.Request.create(card().getNumber(), card().getCardPassword(), "23/10");
    }

    public Card card() {
        return Card.builder()
                .member(member())
                .number("9446032384143059")
                .cardPassword("1234")
                .validThru("11/23")
                .build();
    }

    /**
     *
     * @param member // member().getName()
     * @param path //"/Users/hayoon/Downloads/ocrtest/src/main/resources/static/images/img_1.png"
     * @param format //.png
     * @return
     */

    public OcrDto.Request ocrRequest(Member member, String path, String format) {
        return OcrDto.Request.create(member.getName(), path, format);
    }

    public OcrDto.Request badOcrRequest(String username, String path, String format) {
        return new OcrDto.Request(username, path, format);
    }

    public CardPaymentDto.Request paymentRequest(UuidHolder uuidHolder) {
        return CardPaymentDto.Request.create(card(), order(cart(member())), uuidHolder);
    }

    protected static List<FieldDescriptor> getCardRequests() {
        return List.of(
                fieldWithPath("number").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("cardPassword").type(JsonFieldType.STRING).description("카드 비밀번호"),
                fieldWithPath("validThru").type(JsonFieldType.STRING).description("카드 유효연월")
        );
    }

    protected static List<FieldDescriptor> getCardResponses() {
        return List.of(
                fieldWithPath("cardId").type(JsonFieldType.NUMBER).description("카드 Id"),
                fieldWithPath("number").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("cardPassword").type(JsonFieldType.STRING).description("카드 비밀번호"),
                fieldWithPath("validThru").type(JsonFieldType.STRING).description("카드 유효연월"),
                fieldWithPath("createAt").description("생성 시간")
        );
    }

    protected static List<FieldDescriptor> getOcrRequests() {
        return List.of(
                fieldWithPath("username").type(JsonFieldType.NUMBER).description("회원 이름"),
                fieldWithPath("path").type(JsonFieldType.STRING).description("이미지 경로"),
                fieldWithPath("format").type(JsonFieldType.STRING).description("이미지 확장자명")
        );
    }

    protected static List<FieldDescriptor> getOcrResponses() {
        return List.of(
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("OCR 요청 ID"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("이미지 이름"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("OCR 상태"),
                fieldWithPath("cardNumber").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("validThru").type(JsonFieldType.STRING).description("카드 유효연월")
        );
    }

    protected static List<FieldDescriptor> getCardPaymentResponses() {
        return List.of(
                fieldWithPath("paymentId").type(JsonFieldType.NUMBER).description("결제내역 ID"),
                fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 Uuid"),
                fieldWithPath("orderName").type(JsonFieldType.STRING).description("주문명"),
                fieldWithPath("cardNumber").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("cardExpirationYear").type(JsonFieldType.STRING).description("카드 유효년도"),
                fieldWithPath("cardExpirationMonth").type(JsonFieldType.STRING).description("카드 유효월"),
                fieldWithPath("totalAmount").type(JsonFieldType.STRING).description("주문 총 금액"),
                fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("결제의 키 값"),
                fieldWithPath("paymentStatus").type(JsonFieldType.STRING).description("결제 상태"),
                fieldWithPath("requestedAt").type(JsonFieldType.STRING).description("결제 요청시각"),
                fieldWithPath("approvedAt").type(JsonFieldType.STRING).description("결제 승인시각"),
                fieldWithPath("orderDto").type(JsonFieldType.OBJECT).description("주문 정보"),
                fieldWithPath("orderDto.orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                fieldWithPath("orderDto.name").type(JsonFieldType.STRING).description("주문 정보명"),
                fieldWithPath("orderDto.menuList").type(JsonFieldType.ARRAY).description("메뉴 리스트"),
                fieldWithPath("orderDto.menuList[]").description("메뉴 리스트"),
                fieldWithPath("orderDto.menuList[].menuId").description("메뉴 ID"),
                fieldWithPath("orderDto.menuList[].title").description("메뉴 제목"),
                fieldWithPath("orderDto.menuList[].description").description("메뉴 설명"),
                fieldWithPath("orderDto.menuList[].price.totalPrice").description("메뉴 가격"),
                fieldWithPath("orderDto.menuList[].nutrients").description("영양 정보"),
                fieldWithPath("orderDto.menuList[].nutrients.kcal").description("칼로리"),
                fieldWithPath("orderDto.menuList[].nutrients.carbohydrates").description("탄수화물"),
                fieldWithPath("orderDto.menuList[].nutrients.fats").description("지방"),
                fieldWithPath("orderDto.menuList[].nutrients.proteins").description("단백질"),
                fieldWithPath("orderDto.menuList[].menuSize").description("메뉴 크기"),
                fieldWithPath("orderDto.menuList[].stock").description("재고"),
                fieldWithPath("orderDto.menuList[].viewCount").description("조회수"),
                fieldWithPath("orderDto.menuList[].orderCount").description("주문수"),
                fieldWithPath("orderDto.status").description("주문 상태"),
                fieldWithPath("orderDto.money.totalPrice").description("총 주문 가격"),
                fieldWithPath("orderDto.payStatus").description("결제 상태"),
                fieldWithPath("cancelReason").type(JsonFieldType.NULL).description("결제 취소 사유")
        );
    }

    protected static List<FieldDescriptor> findCardPaymentResponses() {
        return List.of(
                fieldWithPath("paymentId").type(JsonFieldType.NUMBER).description("결제내역 ID"),
                fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 Uuid"),
                fieldWithPath("orderName").type(JsonFieldType.STRING).description("주문명"),
                fieldWithPath("cardNumber").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("cardExpirationYear").type(JsonFieldType.STRING).description("카드 유효년도"),
                fieldWithPath("cardExpirationMonth").type(JsonFieldType.STRING).description("카드 유효월"),
                fieldWithPath("totalAmount").type(JsonFieldType.STRING).description("주문 총 금액"),
                fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("결제의 키 값"),
                fieldWithPath("paymentStatus").type(JsonFieldType.STRING).description("결제 상태"),
                fieldWithPath("requestedAt").type(JsonFieldType.STRING).description("결제 요청시각"),
                fieldWithPath("approvedAt").type(JsonFieldType.STRING).description("결제 승인시각"),
                fieldWithPath("orderDto").type(JsonFieldType.OBJECT).description("주문 정보"),
                fieldWithPath("orderDto.orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                fieldWithPath("orderDto.name").type(JsonFieldType.STRING).description("주문 정보명"),
                fieldWithPath("orderDto.menuList").type(JsonFieldType.ARRAY).description("메뉴 리스트"),
                fieldWithPath("orderDto.menuList[]").description("메뉴 리스트"),
                fieldWithPath("orderDto.status").description("주문 상태"),
                fieldWithPath("orderDto.money.totalPrice").description("총 주문 가격"),
                fieldWithPath("orderDto.payStatus").description("결제 상태"),
                fieldWithPath("cancelReason").type(JsonFieldType.NULL).description("결제 취소 사유")
        );
    }

    protected static List<FieldDescriptor> cancelCardPaymentResponses() {
        return List.of(
                fieldWithPath("paymentId").type(JsonFieldType.NUMBER).description("결제내역 ID"),
                fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 Uuid"),
                fieldWithPath("orderName").type(JsonFieldType.STRING).description("주문명"),
                fieldWithPath("cardNumber").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("cardExpirationYear").type(JsonFieldType.STRING).description("카드 유효년도"),
                fieldWithPath("cardExpirationMonth").type(JsonFieldType.STRING).description("카드 유효월"),
                fieldWithPath("totalAmount").type(JsonFieldType.STRING).description("주문 총 금액"),
                fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("결제의 키 값"),
                fieldWithPath("paymentStatus").type(JsonFieldType.STRING).description("결제 상태"),
                fieldWithPath("requestedAt").type(JsonFieldType.STRING).description("결제 요청시각"),
                fieldWithPath("approvedAt").type(JsonFieldType.STRING).description("결제 승인시각"),
                fieldWithPath("orderDto").type(JsonFieldType.OBJECT).description("주문 정보"),
                fieldWithPath("orderDto.orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                fieldWithPath("orderDto.name").type(JsonFieldType.STRING).description("주문 정보명"),
                fieldWithPath("orderDto.menuList").type(JsonFieldType.ARRAY).description("메뉴 리스트"),
                fieldWithPath("orderDto.menuList[]").description("메뉴 리스트"),
                fieldWithPath("orderDto.status").description("주문 상태"),
                fieldWithPath("orderDto.money.totalPrice").description("총 주문 가격"),
                fieldWithPath("orderDto.payStatus").description("결제 상태"),
                fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("결제 취소 사유")
        );
    }

    protected static List<FieldDescriptor> getPaymentPageResponse() {
        return List.of(
                fieldWithPath("pageNumber").description("페이지 번호"),
                fieldWithPath("cardPayments").type(JsonFieldType.ARRAY).description("결제 내역 리스트"),
                fieldWithPath("cardPayments[].orderId").type(JsonFieldType.STRING).description("주문 ID"),
                fieldWithPath("cardPayments[].paymentId").type(JsonFieldType.NUMBER).description("결제 ID"),
                fieldWithPath("cardPayments[].orderName").type(JsonFieldType.STRING).description("주문명"),
                fieldWithPath("cardPayments[].cardNumber").type(JsonFieldType.STRING).description("카드 번호"),
                fieldWithPath("cardPayments[].cardExpirationYear").type(JsonFieldType.STRING).description("카드 만료 연도"),
                fieldWithPath("cardPayments[].cardExpirationMonth").type(JsonFieldType.STRING).description("카드 만료 월"),
                fieldWithPath("cardPayments[].totalAmount").type(JsonFieldType.STRING).description("총 결제 금액"),
                fieldWithPath("cardPayments[].paymentKey").type(JsonFieldType.STRING).description("결제 키"),
                fieldWithPath("cardPayments[].paymentStatus").type(JsonFieldType.STRING).description("결제 상태"),
                fieldWithPath("cardPayments[].requestedAt").type(JsonFieldType.STRING).description("요청 일자"),
                fieldWithPath("cardPayments[].approvedAt").type(JsonFieldType.STRING).description("승인 일자"),
                fieldWithPath("cardPayments[].orderDto").type(JsonFieldType.OBJECT).description("주문 정보"),
                fieldWithPath("cardPayments[].orderDto.orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                fieldWithPath("cardPayments[].orderDto.name").type(JsonFieldType.STRING).description("주문명"),
                fieldWithPath("cardPayments[].orderDto.menuList").type(JsonFieldType.ARRAY).description("주문한 메뉴 리스트"),
                fieldWithPath("cardPayments[].orderDto.status").description("주문 상태"),
                fieldWithPath("cardPayments[].orderDto.money.totalPrice").description("총 주문 가격"),
                fieldWithPath("cardPayments[].orderDto.payStatus").description("결제 상태"),
                fieldWithPath("cardPayments[].cancelReason").optional().description("취소 사유")

        // 필요한 추가 필드 및 설명을 여기에 추가
        );
    }
}
