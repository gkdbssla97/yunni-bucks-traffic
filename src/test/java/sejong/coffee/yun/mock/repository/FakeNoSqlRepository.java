package sejong.coffee.yun.mock.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import sejong.coffee.yun.repository.redis.NoSqlRepository;

import java.time.Duration;

@Repository
@Primary
public class FakeNoSqlRepository implements NoSqlRepository {
    private final CustomValueOperation customValueOperation;

    public FakeNoSqlRepository(CustomValueOperation customValueOperation) {
        this.customValueOperation = customValueOperation;
    }

    @Override
    public void setValues(String key, String value) {
        customValueOperation.set(key, value);
    }

    @Override
    public void setValues(String key, String value, Duration duration) {
        customValueOperation.set(key, value, duration);
    }

    @Override
    public String getValues(String key) {
        return customValueOperation.get(key);
    }

    @Override
    public void deleteValues(String key) {
        customValueOperation.remove(key);
    }

    @Override
    public void clear() {
        customValueOperation.clear();
    }
}
