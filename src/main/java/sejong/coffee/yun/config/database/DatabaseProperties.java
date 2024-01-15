package sejong.coffee.yun.config.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
        private Hibernate hibernate;
    }

    @Data
    public static class Hibernate {
        private String ddlAuto;
        private String dialect;
        private Naming naming;
        private String metadataBuilderContributor;

        public static Map<String, Object> propertiesToMap(Hibernate hibernateProperties) {
            Map<String, Object> properties = new HashMap<>();

            if (hibernateProperties.getDdlAuto() != null) {
                properties.put("hibernate.hbm2ddl.auto", "spring.jpa.hibernate.ddl-auto");
            }
            if (hibernateProperties.getMetadataBuilderContributor() != null) {
                properties.put("hibernate.metadata_builder_contributor", hibernateProperties.getMetadataBuilderContributor());
            }
            DatabaseProperties.Naming hibernateNaming = hibernateProperties.getNaming();
            if (hibernateNaming != null) {
                if (hibernateNaming.getImplicitStrategy() != null) {
                    properties.put("hibernate.implicit_naming_strategy", hibernateNaming.getImplicitStrategy());
                }
                if (hibernateNaming.getPhysicalStrategy() != null) {
                    properties.put("hibernate.physical_naming_strategy", hibernateNaming.getPhysicalStrategy());
                }
            }

            return properties;
        }
    }

    @Data
    public static class Naming {
        private String implicitStrategy;
        private String physicalStrategy;
    }
}
