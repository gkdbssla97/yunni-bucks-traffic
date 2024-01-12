package sejong.coffee.yun.util.log;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.persistence.EntityManagerFactory;

@Slf4j
public class LoggingJpaTransactionManager extends JpaTransactionManager {

    private final String entityManagerFactoryName;

    public LoggingJpaTransactionManager(EntityManagerFactory emf, String entityManagerFactoryName) {
        super(emf);
        this.entityManagerFactoryName = entityManagerFactoryName;
    }

    @Override
    protected void doCommit(@NotNull DefaultTransactionStatus status) {
        log.info("Transaction commit with entityManagerFactory: " + this.entityManagerFactoryName);
        super.doCommit(status);
    }
}


