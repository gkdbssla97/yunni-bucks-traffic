package sejong.coffee.yun.infra.port;

import sejong.coffee.yun.dto.pay.CardPaymentDto;

import java.io.IOException;

public interface TossApiService {
    CardPaymentDto.Response callExternalApi(CardPaymentDto.Request cardPaymentDto) throws IOException, InterruptedException;

    CardPaymentDto.Response confirm(CardPaymentDto.Confirm cardPaymentDto) throws IOException, InterruptedException;
}
