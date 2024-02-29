package sejong.coffee.yun.config.database;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sejong.coffee.yun.domain.order.menu.postgre.JpaPostgresSQLMenuReviewRepository;
import sejong.coffee.yun.domain.order.menu.postgre.PostgresMenuReviewRepository;
import sejong.coffee.yun.domain.order.menu.postgre.PostgresSQLMenuReviewRepositoryImpl;

import javax.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
public class PostgresJpaConfig {

    private final JpaPostgresSQLMenuReviewRepository jpaPostgresSQLMenuReviewRepository;

    @Bean
    public PostgresMenuReviewRepository postgresMenuReviewRepository(@Qualifier("postgresEntityManagerFactory") EntityManagerFactory em) {
        return new PostgresSQLMenuReviewRepositoryImpl(jpaPostgresSQLMenuReviewRepository, em.createEntityManager());
    }
}
