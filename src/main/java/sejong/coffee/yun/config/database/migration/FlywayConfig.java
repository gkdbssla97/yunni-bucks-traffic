package sejong.coffee.yun.config.database.migration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway masterFlyway(@Qualifier("masterDataSource") DataSource masterDataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(masterDataSource)
                .locations("classpath:database/migration/mysql/master")
                .load();
        flyway.migrate();
        return flyway;
    }

//    @Bean
//    @DependsOn("masterFlyway")
//    public Flyway slaveFlyway(@Qualifier("slaveDataSource") DataSource slaveDataSource) {
//        Flyway flyway = Flyway.configure()
//                .dataSource(slaveDataSource)
//                .locations("classpath:database/migration/mysql/master")
//                .load();
//        flyway.migrate();
//        return flyway;
//    }

//    @Bean
//    public Flyway subFlyway(DataSource subDataSource) {
//        Flyway flyway = Flyway.configure()
//                .dataSource(subDataSource)
//                .locations("classpath:database/migration/sub")
//                .load();
//        flyway.migrate();
//        return flyway;
//    }
}

