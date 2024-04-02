package sejong.coffee.yun.config.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sejong.coffee.yun.config.ssh.SSHConnection;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static sejong.coffee.yun.config.database.DatabaseProperties.DatabaseDetail;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(DatabaseProperties.class)
public class DataSourceConfig {

    private final SSHConnection sshConnection;

    @PostConstruct
    public void initializeSshConnection() {
        sshConnection.buildSshConnection();
        log.info("SSH 접속이 초기화되었습니다.");
    }

    @Bean
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource masterDataSource,
                                        @Qualifier("slaveDataSources") List<DataSource> slaveDataSources) {

        Map<Object, Object> dataSources = new LinkedHashMap<>();
        dataSources.put("master", masterDataSource);

        IntStream.range(0, slaveDataSources.size())
                .forEach(i -> {
                    DataSource slaveDataSource = slaveDataSources.get(i);
                    dataSources.put(String.format("slave-%d", (i + 1)), slaveDataSource);
                });

        List<Object> onlySlaveDataSources = new ArrayList<>(dataSources.values());
        onlySlaveDataSources.remove(masterDataSource);

        List<String> slaveDataSourceNames = dataSources.keySet().stream()
                .map(Object::toString)
                .filter(key -> key.startsWith("slave")).toList();

        RoutingDataSource routingDataSource = new RoutingDataSource(onlySlaveDataSources, slaveDataSourceNames);
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
        log.info("Master DB detail: {}", databaseProperties.getMaster());
        return createDataSource(databaseProperties.getMaster());
    }

    @Bean("slaveDataSources")
    public List<DataSource> createSlaveDataSources(DatabaseProperties databaseProperties) {
        log.info("Creating slave data sources...");
        List<DataSource> slaveDataSources = new ArrayList<>();
        for (DatabaseDetail slave : databaseProperties.getSlaves()) {
            log.info("Slave DB detail: {}", slave);
            slaveDataSources.add(createDataSource(slave));
        }
        return slaveDataSources;
    }

    @Bean("postgresDataSource")
    public DataSource createPostgresDataSource(DatabaseProperties databaseProperties) {
        return createDataSource(databaseProperties.getPostgres());
    }

    private DataSource createDataSource(DatabaseDetail databaseDetail) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseDetail.getUrl());
        dataSource.setDriverClassName(databaseDetail.getDriverClassName());
        dataSource.setUsername(databaseDetail.getUsername());
        dataSource.setPassword(databaseDetail.getPassword());

        return dataSource;
    }

    @Slf4j
    private static class RoutingDataSource extends AbstractRoutingDataSource {
        private final AtomicInteger index = new AtomicInteger(0);
        private final List<Object> slaveDataSources;
        private final List<String> slaveDataSourceNames;

        public RoutingDataSource(List<Object> slaveDataSources, List<String> slaveDataSourceNames) {
            this.slaveDataSources = slaveDataSources;
            this.slaveDataSourceNames = slaveDataSourceNames;
        }

        @Override
        protected Object determineCurrentLookupKey() {
            String dataSourceName = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                    ? slaveDataSourceNames.get(index.getAndIncrement() % slaveDataSources.size())
                    : "master";
            log.info("[DATA_SOURCE_NAME] : {}", dataSourceName);
            return dataSourceName;
        }
    }

    @Bean
//    @Qualifier("masterJdbcTemplate")
    public JdbcTemplate masterJdbcTemplate(DatabaseProperties databaseProperties) {
        DataSource masterDataSource = createDataSource(databaseProperties.getMaster());
        return new JdbcTemplate(masterDataSource);
    }

    @Bean
//    @Qualifier("postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(DatabaseProperties databaseProperties) {
        DataSource postgresDataSource = createDataSource(databaseProperties.getPostgres());
        return new JdbcTemplate(postgresDataSource);
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        int corePoolSize = 8;
        int maxPoolSize = 16;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.SECONDS;
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
