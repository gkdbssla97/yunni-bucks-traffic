package sejong.coffee.yun.controller.advise;

import lombok.RequiredArgsConstructor;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@RestControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
public class ErrorDetectAdvisor extends GlobalExceptionHandler {

    private final SlackApi slackApi;
    private final ThreadPoolExecutor threadPoolExecutor;

    @ExceptionHandler(Exception.class)
    public void handleException(HttpServletRequest req, Exception e) {
        SlackAttachment slackAttachment = new SlackAttachment();
        slackAttachment.setFallback("Error");
        slackAttachment.setColor("danger");
        slackAttachment.setTitle("Error Detect");
        slackAttachment.setTitleLink(req.getContextPath());
        slackAttachment.setText(Arrays.toString(e.getStackTrace()));
        slackAttachment.setColor("danger");
        slackAttachment.setFields(List.of(
                new SlackField().setTitle("Request URL").setValue(req.getRequestURL().toString()),
                new SlackField().setTitle("Request Method").setValue(req.getMethod()),
                new SlackField().setTitle("Request Time").setValue(new Date().toString()),
                new SlackField().setTitle("Request IP").setValue(req.getRemoteAddr()),
                new SlackField().setTitle("Request User-Agent").setValue(req.getHeader("User-Agent"))
        ));

        SlackMessage slackMessage = new SlackMessage();
        slackMessage.setAttachments(Collections.singletonList(slackAttachment));
        slackMessage.setIcon(":ghost:");
        slackMessage.setText("긴급 상황");
        slackMessage.setUsername("Error Bot");

        threadPoolExecutor.execute(() -> {
            slackApi.call(slackMessage);
        });

        throw new RuntimeException(e);
    }
}
