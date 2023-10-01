package sejong.coffee.yun.webhook;

import lombok.RequiredArgsConstructor;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Aspect
@Component
@RequiredArgsConstructor
@Profile(value = {"local", "dev"})
public class SlackNotificationAspect {

    private final SlackApi slackApi;
    private final ThreadPoolExecutor threadPoolExecutor;

    @Around("@annotation(sejong.coffee.yun.custom.annotation.SlackNotification)")
    public Object slackNotification(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        threadPoolExecutor.execute(() -> sendSlackMessage(proceedingJoinPoint, request));

        return proceedingJoinPoint.proceed();
    }

    private void sendSlackMessage(ProceedingJoinPoint proceedingJoinPoint, HttpServletRequest req) {
        SlackAttachment slackAttachment = new SlackAttachment();
        slackAttachment.setFallback(req.getMethod());
        slackAttachment.setColor("good");
        slackAttachment.setTitle(String.format("Data %s detected", req.getMethod()));
        slackAttachment.setFields(List.of(
                new SlackField().setTitle("Arguments").setValue(Arrays.toString(proceedingJoinPoint.getArgs())),
                new SlackField().setTitle("method").setValue(proceedingJoinPoint.getSignature().getName())
        ));

        SlackMessage slackMessage = new SlackMessage();
        slackMessage.setAttachments(Collections.singletonList(slackAttachment));
        slackMessage.setIcon(":gear:");
        slackMessage.setText(String.format("%s Request", req.getMethod()));
        slackMessage.setUsername("Method Bot");

        slackApi.call(slackMessage);
    }
}
