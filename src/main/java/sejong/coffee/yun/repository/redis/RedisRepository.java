package sejong.coffee.yun.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> template;

    @Transactional
    public void setValues(String key, String value) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        valueOperations.set(key, value);
    }

    @Transactional
    public void setValues(String key, String value, Duration duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        valueOperations.set(key, value, duration);
    }

    public String getValues(String key) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        return valueOperations.get(key);
    }

    @Transactional
    public void deleteValues(String key) {
        template.delete(key);
    }
}