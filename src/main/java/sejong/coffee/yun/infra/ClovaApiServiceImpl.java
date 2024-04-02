package sejong.coffee.yun.infra;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sejong.coffee.yun.dto.ocr.OcrDto;
import sejong.coffee.yun.infra.port.ClovaApiService;
import sejong.coffee.yun.infra.port.UuidHolder;
import sejong.coffee.yun.util.parse.JsonParsing;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static sejong.coffee.yun.util.parse.ParsingUtil.parsingDataTimePattern;

@Service("clovaApiServiceImpl")
@Getter
public class ClovaApiServiceImpl implements ClovaApiService {

    private final String authorizeUri;
    private final String secretKey;

    public ClovaApiServiceImpl(@Value("${secrets.clova.authorizeUri}") final String authorizeUri,
                               @Value("${secrets.clova.secret-key}") final String secretKey) {

        this.authorizeUri = authorizeUri;
        this.secretKey = secretKey;
    }

    @Override
    public OcrDto.Response callExternalApi(OcrDto.Request request, UuidHolder uuidHolder) throws IOException, InterruptedException {
        OcrDto.Response ocrResponse = null;

        String uuid = uuidHolder.random();

        try {
            URL url = new URL(authorizeUri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", uuid);
            json.put("timestamp", System.currentTimeMillis());
            JSONObject image = new JSONObject();
            image.put("format", request.format());
            try (FileInputStream inputStream = new FileInputStream(request.path())) {
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();
                image.put("data", buffer);
                image.put("name", parsingDataTimePattern());
                JSONArray images = new JSONArray();
                images.put(image);
                json.put("images", images);
                String postParams = json.toString();

                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
            }
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            String s = String.valueOf(response);
            ocrResponse = JsonParsing.parseOcrObjectByJson(s);
//            System.out.println(s);
//            System.out.println(ocrResponse);
        } catch (Exception e) {
            System.out.println(e);
        }

        return ocrResponse;
    }
}