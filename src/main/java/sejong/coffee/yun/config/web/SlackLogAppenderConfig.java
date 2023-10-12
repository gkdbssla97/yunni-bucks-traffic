package sejong.coffee.yun.config.web;

import net.gpedro.integrations.slack.SlackApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Configuration
public class SlackLogAppenderConfig {

    @Value("${logging.slack.token}")
    private String token;

    @Bean
    public SlackApi slackApi() {
        return new SlackApi("https://hooks.slack.com/services/" + token);
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        int corePoolSize = 10;
        int maxPoolSize = 20;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.SECONDS;
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
