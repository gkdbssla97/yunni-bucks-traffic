package sejong.coffee.yun.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sejong.coffee.yun.dto.ocr.OcrDto;
import sejong.coffee.yun.dto.pay.CardPaymentDto;
import sejong.coffee.yun.infra.port.ClovaApiService;
import sejong.coffee.yun.infra.port.TossApiService;
import sejong.coffee.yun.infra.port.UuidHolder;

import java.io.IOException;

import static sejong.coffee.yun.dto.pay.CardPaymentDto.Request;
import static sejong.coffee.yun.dto.pay.CardPaymentDto.Response;

@Service
@RequiredArgsConstructor
public class ApiService {

    private TossApiService tossApiService;
    private ClovaApiService clovaApiService;

    @Autowired
    public ApiService(TossApiService tossApiService, ClovaApiService clovaApiService) {
        this.tossApiService = tossApiService;
        this.clovaApiService = clovaApiService;
    }

    public Response callExternalPayApi(Request request) throws IOException, InterruptedException {
        return tossApiService.callExternalApi(request);
    }

    public Response confirm(CardPaymentDto.Confirm confirm) throws IOException, InterruptedException {
        return tossApiService.confirm(confirm);
    }

    public OcrDto.Response callExternalOcrApi(OcrDto.Request request, UuidHolder uuidHolder) throws IOException, InterruptedException {
        return clovaApiService.callExternalApi(request, uuidHolder);
    }
}
