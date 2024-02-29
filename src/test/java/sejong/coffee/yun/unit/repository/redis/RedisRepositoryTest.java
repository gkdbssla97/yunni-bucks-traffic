package sejong.coffee.yun.unit.repository.redis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.mock.repository.CustomValueOperation;
import sejong.coffee.yun.mock.repository.CustomValueOperationImpl;
import sejong.coffee.yun.mock.repository.FakeNoSqlRepository;
import sejong.coffee.yun.repository.redis.NoSqlRepository;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RedisRepositoryTest {

    private final NoSqlRepository noSqlRepository;

    public RedisRepositoryTest() {
        CustomValueOperation customValueOperation = new CustomValueOperationImpl();
        this.noSqlRepository = new FakeNoSqlRepository(customValueOperation);
    }

    @Test
    void 키_벨류_유효기간을_저장한다() {
        // given
        String key = "key";
        String value = "value";
        Duration duration = Duration.ofMillis(1000);

        // when
        noSqlRepository.setValues(key, value, duration);

        // then
        assertThat(noSqlRepository.getValues(key)).isEqualTo(value);
    }

    @Test
    void 키_벨류_저장한다() {
        // given
        String key = "key";
        String value = "value";

        // when
        noSqlRepository.setValues(key, value);

        // then
        assertThat(noSqlRepository.getValues(key)).isEqualTo(value);
    }

    @Test
    @Disabled
    void 유효기간이_지난_키는_자동삭제된다() throws InterruptedException {
        // given
        String key = "key";
        String value = "value";
        Duration duration = Duration.ofMillis(10);

        // when
        noSqlRepository.setValues(key, value, duration);

        // then
        Thread.sleep(11);
        assertThat(noSqlRepository.getValues(key)).isNull();
    }

    @Test
    void 벨류_삭제() {
        // given
        String key = "key";
        String value = "value";

        noSqlRepository.setValues(key, value);

        // when
        noSqlRepository.deleteValues(key);

        // then
        assertThat(noSqlRepository.getValues(key)).isNull();
    }
}