package io.github.cgi.captchagenerator.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cgi.captchagenerator.config.RedisConfig;
import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Пример класса с интеграционным тестом.
 *
 * Для подключения Redis тут используется библиотека
 * Testcontainers - она позволяет работать с внешними
 * зависимостями прямо из кода без дополнительных ручных действий.
 *
 * В данном классе не лучший вариант инициализации - по
 * сути часть работы Spring делается вручную. Это работает,
 * но не очень красиво и поддерживаемо - при изменении
 * основного конфига тут нужно повторять действия уже вручную.
 *
 * Плюсы - контекст Spring не создается - можем работать
 * изолировано на низком уровне (условно).
 */
@Testcontainers
class CaptchaRepositoryV3ImplIntegrationTest {

    // Статическая переменная - мы не хотим пересоздавать редис для каждого теста
    @Container
    public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6.2.5-alpine"))
            .withExposedPorts(6379);

    private static CaptchaRepositoryV3Impl captchaRepositoryV3;


    @BeforeAll
    public static void setup(){

        String address = redis.getHost();
        Integer port = redis.getFirstMappedPort();

        RedisConfig redisConfig = new RedisConfig();

        RedisStandaloneConfiguration standaloneConfiguration = redisConfig.redisConfiguration();
        standaloneConfiguration.setDatabase(0);
        standaloneConfiguration.setHostName(address);
        standaloneConfiguration.setPort(port);

        JedisConnectionFactory connectionFactory = redisConfig.jedisConnectionFactory(standaloneConfiguration);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<UUID, CaptchaInfo> redisTemplate = redisConfig.redisCaptchaTemplate(connectionFactory);

        redisTemplate.afterPropertiesSet();

        captchaRepositoryV3 = new CaptchaRepositoryV3Impl();
        captchaRepositoryV3.setRedisTemplate(redisTemplate);
        captchaRepositoryV3.setUnit(TimeUnit.MINUTES);
        captchaRepositoryV3.setTtl(123L);

    }

    @Test
    void save() {
        CaptchaContext context = captchaRepositoryV3.save(new CaptchaInfo());
        CaptchaInfo captchaInfo = captchaRepositoryV3.get(context.getUuid());
        assertNotNull(captchaInfo);
    }

    @Test
    void get() {
    }

    @Test
    void delete() {
    }
}