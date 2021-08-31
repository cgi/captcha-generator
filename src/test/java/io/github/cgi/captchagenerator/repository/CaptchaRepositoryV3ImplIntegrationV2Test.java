package io.github.cgi.captchagenerator.repository;

import io.github.cgi.captchagenerator.config.RedisConfig;
import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Пример класса с интеграционным тестом.
 *
 * Для подключения Redis тут используется библиотека
 * Testcontainers - она позволяет работать с внешними
 * зависимостями прямо из кода без дополнительных ручных действий.
 *
 * В данном примере все делается максимально честно
 * относительно Spring-контекста:
 * - Spring запускается
 * - Используется стандартная конфигурация контекста
 *      основного приложения
 *
 * Точно такую же логику можно применить и к другим контейнерам:
 * 1. инициализируем из как статическую переменную
 * 2. регистрируем специальный обработчик
 * 3. в обработчике получаем параметры работающего контейнера
 *      и прописываем их как свойства контекста.
 *
 *  Оригинальный рецепт был найден для jdbc параметров, а не для Redis.
 */
@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = {CaptchaRepositoryV3ImplIntegrationV2Test.Initializer.class})
class CaptchaRepositoryV3ImplIntegrationV2Test {

    // Статическая переменная - мы не хотим пересоздавать редис для каждого теста
    @Container
    public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6.2.5-alpine"))
            .withExposedPorts(6379);

    @Autowired
    CaptchaRepositoryV3Impl repositoryV3;


    @Test
    void save() {
        CaptchaContext context = repositoryV3.save(new CaptchaInfo());
        CaptchaInfo captchaInfo = repositoryV3.get(context.getUuid());
        assertNotNull(captchaInfo);
    }

    /**
     * Класс добавляет в конфигурацию контекста параметры,
     * значения которых зависят от контейнера.
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "captcha.redis.hostName=" + redis.getHost(),
                    "captcha.redis.port=" + redis.getFirstMappedPort()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}