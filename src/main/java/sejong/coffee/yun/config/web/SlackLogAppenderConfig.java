package sejong.coffee.yun.config.web;

import net.gpedro.integrations.slack.SlackApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class SlackLogAppenderConfig {

    @Value("${logging.slack.token}")
    private String token;

    @Bean
    public SlackApi slackApi() {
        return new SlackApi("https://hooks.slack.com/services/" + token);
    }
}
