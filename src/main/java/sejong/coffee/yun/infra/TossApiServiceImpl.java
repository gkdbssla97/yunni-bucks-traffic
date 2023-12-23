package sejong.coffee.yun.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.infra.port.TossApiService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static sejong.coffee.yun.util.parse.JsonParsing.parsePaymentObjectByJson;
import static sejong.coffee.yun.util.parse.JsonParsing.parsePaymentStringByJson;

@Service("tossApiServiceImpl")
@Slf4j
public class TossApiServiceImpl implements TossApiService {

    private final String apiUri;
    private final String secretKey;

    public TossApiServiceImpl(@Value("${secrets.toss.apiUri}") final String apiUri,
                              @Value("${secrets.toss.secret-key}") final String secretKey) {

        this.apiUri = apiUri;
        this.secretKey = secretKey;
    }

    @Override
    public CardPaymentDto.Response callExternalApi(CardPaymentDto.Request cardPaymentDto) throws IOException, InterruptedException {

        // 외부 API 호출하는 로직
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", secretKey)
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(parsePaymentStringByJson(cardPaymentDto)))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        // 예외처리(토스 커스텀 -> 내부 프로젝트 커스텀) TODO
        log.info("s -> {}" + response.body());
        return parsePaymentObjectByJson(response.body());
    }

    @Override
    public CardPaymentDto.Response confirm(CardPaymentDto.Confirm confirm) throws IOException, InterruptedException {

        // 외부 API 호출하는 로직
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", secretKey)
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(parsePaymentStringByJson(confirm)))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(5)) // 5분의 연결 시간 제한 설정
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // 예외처리(토스 커스텀 -> 내부 프로젝트 커스텀) TODO
        log.info("s -> {}" + response.body());
        return parsePaymentObjectByJson(response.body());
    }
}
