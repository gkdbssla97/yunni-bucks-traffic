package sejong.coffee.yun.util.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import sejong.coffee.yun.domain.exception.ExceptionControl;
import sejong.coffee.yun.dto.pay.CardPaymentDto;

import static sejong.coffee.yun.dto.ocr.OcrDto.Response;


public class JsonParsing {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String parsePaymentStringByJson(Object request) throws JsonProcessingException {
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.writeValueAsString(request);
    }

    public static String parsePaymentStringByJson(CardPaymentDto.Response response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response);
    }

    public static CardPaymentDto.Response parsePaymentObjectByJson(String responseBody) throws JsonProcessingException {
        ObjectMapper mapper = objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(responseBody, CardPaymentDto.Response.class);
    }

    public static Response parseOcrObjectByJson(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonData = objectMapper.readTree(responseBody); // jsonString은 주어진 JSON 문자열

        JsonNode cardsNode = jsonData.get("images");
        if (cardsNode != null && cardsNode.isArray()) {
            for (JsonNode cardNode : cardsNode) {
                JsonNode numberNode = cardNode.path("creditCard").path("result").path("number").path("text");
                JsonNode validThruNode = cardNode.path("creditCard").path("result").path("validThru").path("text");
                JsonNode uidNode = cardNode.path("uid");
                JsonNode nameNode = cardNode.path("name");
                JsonNode messageNode = cardNode.path("message");

                if (numberNode.isTextual() && validThruNode.isTextual() && uidNode.isTextual() && nameNode.isTextual() && messageNode.isTextual()) {
                    String cardNumber = numberNode.asText();
                    String validThru = validThruNode.asText();
                    String uid = uidNode.asText();
                    String name = nameNode.asText();
                    String message = messageNode.asText();

                    // Record Dto 생성
                    return Response.create(uid, name, message, cardNumber, validThru);

                    // 이제 생성된 Response 객체를 활용하면 됩니다.
                }
            }
        }
        throw ExceptionControl.NOT_FOUND_OCR_RESPONSE_BODY.ocrException();
    }
}
