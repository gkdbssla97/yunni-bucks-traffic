package sejong.coffee.yun.config.database;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseProperties {
    private DatabaseDetail master, postgres;
    private List<DatabaseDetail> slaves;

    @Data
    public static class DatabaseDetail {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
    }
}
