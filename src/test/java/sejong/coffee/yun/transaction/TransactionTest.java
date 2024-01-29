package sejong.coffee.yun.transaction;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sejong.coffee.yun.domain.user.Address;
import sejong.coffee.yun.domain.user.Member;
import sejong.coffee.yun.domain.user.Money;
import sejong.coffee.yun.domain.user.UserRank;
import sejong.coffee.yun.repository.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionTest {

    @Autowired
    private UserRepository userRepository;

    public Member member;

    @BeforeAll
    void init() {
        member = Member.builder()
                .name("홍길동")
                .userRank(UserRank.BRONZE)
                .password("qwer1234@A")
                .money(Money.ZERO)
                .coupon(null)
                .email("qwer1234@naver.com")
                .address(new Address("서울시", "광진구", "능동로 110 세종대학교", "100-100"))
                .orderCount(0)
                .build();

        userRepository.save(member);
    }

@Nested
class MasterSlaveTest {
//    @Transactional // master 접속
    @Test
    public void save() {
        userRepository.save(member);
        List<Member> all = userRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(1);
    }

//    @Transactional(readOnly = true) // slave-x 접속
    @Test
    public void findById() {
        Member byEmail = userRepository.findByEmail("qwer1234@naver.com");

        assertEquals("홍길동", byEmail.getName());
    }

}

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

