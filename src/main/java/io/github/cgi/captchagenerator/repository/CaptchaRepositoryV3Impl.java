package io.github.cgi.captchagenerator.repository;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "captcha.store")
public class CaptchaRepositoryV3Impl implements CaptchaRepositoryV3 {

    @Autowired
    private RedisTemplate<UUID, CaptchaInfo> redisTemplate;

    private long ttl;

    private TimeUnit unit;

    /**
     * В этом методе есть потенциальная проблема в части многопоточности.
     * Где же она и в чем заключается?
     * @param info - сохраняемые данные
     * @return CaptchaContext c метаданными сохранения капчи.
     */
    @Override
    public CaptchaContext save(CaptchaInfo info) {
        Objects.requireNonNull(info, "Параметр info не должен быть null");
        CaptchaContext context = new CaptchaContext();

        UUID uuid = UUID.randomUUID();
        context.setUuid(uuid);

        context.setTtl(ttl);
        context.setTimeUnit(unit);
        context.setInfo(info);

        redisTemplate.opsForValue().set(uuid, info, ttl, unit);
        return context;
    }

    @Override
    public CaptchaInfo get(UUID uuid) {
        Objects.requireNonNull(uuid, "Параметр uuid не должен быть null");

        CaptchaInfo info = redisTemplate.opsForValue().get(uuid);

        return info;
    }

    @Override
    public void delete(UUID uuid) {
        Objects.requireNonNull(uuid, "Параметр uuid не должен быть null");

        redisTemplate.delete(uuid);
    }
}
