package sejong.coffee.yun.unit.repository.redis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sejong.coffee.yun.mock.repository.CustomValueOperation;
import sejong.coffee.yun.mock.repository.CustomValueOperationImpl;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CustomValueOperationImplTest {

    private final CustomValueOperation customValueOperation;

    public CustomValueOperationImplTest() {
        this.customValueOperation = new CustomValueOperationImpl();
    }

    @Test
    void set_타임인_테스트() {
        // given
        String key = "key";
        String value = "value";
        customValueOperation.set(key, value, Duration.ofMillis(10000));

        // when
        String v = customValueOperation.get(key);

        // then
        assertThat(v).isEqualTo(value);
    }

    @Test
    @Disabled
    void set_타임아웃_테스트() throws InterruptedException{
        // given
        String key = "key";
        String value = "value";
        Duration duration = Duration.ofMillis(10);
        customValueOperation.set(key, value, duration);

        // when
        Thread.sleep(11);
        String v = customValueOperation.get(key);

        // then
        assertThat(v).isNull();
    }
}