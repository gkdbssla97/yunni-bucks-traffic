package sejong.coffee.yun.config.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseProperties {
    private DatabaseDetail master, slave;
    private DatabaseDetail postgres;

    @Data
    public static class DatabaseDetail {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
    }
}
