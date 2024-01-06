package sejong.coffee.yun.config.database;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static sejong.coffee.yun.config.database.PrimaryConfig.*;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
@EnableJpaRepositories(basePackages = {"sejong.coffee.yun.repository"},
        entityManagerFactoryRef = ENTITY_MANAGER_BEAN_NAME,
        transactionManagerRef = TRANSACTION_MANAGER_BEAN_NAME)
public class PrimaryConfig {
    public static final String TRANSACTION_MANAGER_BEAN_NAME = "oneDBTransactionManager";
    public static final String ENTITY_MANAGER_BEAN_NAME = "oneDBEntityManager";
    private static final String DATASOURCE_BEAN_NAME = "oneDataSource";
    private static final String DATASOURCE_PROPERTIES_PREFIX = "spring.datasource.main";
    private static final String DATASOURCE_PROPERTIES = "oneDataSourceProperties";
    private static final String HIBERNATE_PROPERTIES = "oneHibernateProperties";

    @Primary
    @Bean(name = ENTITY_MANAGER_BEAN_NAME)
    public LocalContainerEntityManagerFactoryBean entityManager(EntityManagerFactoryBuilder builder, @Qualifier(DATASOURCE_BEAN_NAME) DataSource dataSource,
                                                                @Qualifier(HIBERNATE_PROPERTIES) DatabaseProperties.Hibernate hibernateProperties) {

        return builder.dataSource(dataSource).packages("sejong.coffee.yun.domain")
                .persistenceUnit(ENTITY_MANAGER_BEAN_NAME)
                .properties(DatabaseProperties.Hibernate.propertiesToMap(hibernateProperties)).build();
    }

    @Bean(name = HIBERNATE_PROPERTIES)
    @ConfigurationProperties(DATASOURCE_PROPERTIES_PREFIX + ".hibernate")
    public DatabaseProperties.Hibernate hibernateProperties() {
        return new DatabaseProperties.Hibernate();
    }

    @Bean(name = DATASOURCE_PROPERTIES)
    @ConfigurationProperties(DATASOURCE_PROPERTIES_PREFIX)
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = DATASOURCE_BEAN_NAME)
    @ConfigurationProperties(prefix = DATASOURCE_PROPERTIES_PREFIX + ".hikari")
    public DataSource dataSource(@Qualifier(DATASOURCE_PROPERTIES) DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = TRANSACTION_MANAGER_BEAN_NAME)
    public PlatformTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER_BEAN_NAME) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
