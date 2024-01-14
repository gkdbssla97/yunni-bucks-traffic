package sejong.coffee.yun.config.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Bean
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource masterDataSource,
                                        @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        Map<Object, Object> dataSources = new LinkedHashMap<>();
        dataSources.put("master", masterDataSource);
        dataSources.put("slave", slaveDataSource);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        return routingDataSource;
    }

    @Bean
    @DependsOn({"routingDataSource"})
    public DataSource dataSource(DataSource routingDataSource) {
        // 트랜잭션 실행시에 Connection 객체를 가져오기 위해 LazyConnectionDataSourceProxy로 설정
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean("masterDataSource")
    public DataSource createMasterDataSource(DatabaseProperties databaseProperties) {
        return createDataSource(databaseProperties.getMaster());
    }

    @Bean("slaveDataSource")
    public DataSource createSlaveDataSource(DatabaseProperties databaseProperties) {
        return createDataSource(databaseProperties.getSlave());
    }

    private DataSource createDataSource(DatabaseProperties.DatabaseDetail databaseDetail) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseDetail.getUrl());
        dataSource.setDriverClassName(databaseDetail.getDriverClassName());
        dataSource.setUsername(databaseDetail.getUsername());
        dataSource.setPassword(databaseDetail.getPassword());
        return dataSource;
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class RoutingDataSource extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            String dataSourceName = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                    ? "slave"
                    : "master";

            log.info("[DATA_SOURCE_NAME] : {}", dataSourceName);

            return dataSourceName;
        }
    }

    @Bean
    @Qualifier("masterJdbcTemplate")
    public JdbcTemplate masterJdbcTemplate(DatabaseProperties databaseProperties) {
        DataSource masterDataSource = createDataSource(databaseProperties.getMaster());
        return new JdbcTemplate(masterDataSource);
    }
}
