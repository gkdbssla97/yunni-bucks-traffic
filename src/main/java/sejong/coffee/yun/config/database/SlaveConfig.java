package sejong.coffee.yun.config.database;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class SlaveConfig {

    public static final String TRANSACTION_MANAGER_BEAN_NAME = "slaveDBTransactionManager";
    public static final String ENTITY_MANAGER_BEAN_NAME = "slaveDBEntityManager";
    private static final String DATASOURCE_BEAN_NAME = "slaveDataSource";
    private static final String DATASOURCE_PROPERTIES_PREFIX = "spring.datasource.slave";
    private static final String DATASOURCE_PROPERTIES = "slaveDataSourceProperties";
    private static final String HIBERNATE_PROPERTIES = "slaveHibernateProperties";

    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix="spring.datasource.slave.hikari")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

}


