package sejong.coffee.yun.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TransactionTest {

    @Test
    @Transactional
    public void testTransactionActive() {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Transaction is not active");
    }

    @Test
    @Transactional(readOnly = true)
    public void testTransactionReadOnly() {
        assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Transaction is not read-only");
    }

    @Test
    @Transactional
    public void testTransactionNotReadOnly() {
        assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Transaction is read-only");
    }
}

