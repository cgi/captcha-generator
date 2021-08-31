package io.github.cgi.captchagenerator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.OxmSerializer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration
public class RedisConfig {

    @Bean
    @ConfigurationProperties(prefix = "captcha.redis")
    public RedisStandaloneConfiguration redisConfiguration(){
        return new RedisStandaloneConfiguration();
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(
            RedisStandaloneConfiguration redisConfiguration) {
        return new JedisConnectionFactory(redisConfiguration);
    }

    @Bean
    public RedisTemplate<UUID, CaptchaInfo> redisCaptchaTemplate(
            JedisConnectionFactory connectionFactory
    ) {
        final RedisTemplate<UUID, CaptchaInfo> template = new RedisTemplate<>();
        template.setConnectionFactory( connectionFactory );
        // Ошибка в тесте если так инициализировать
        //template.setValueSerializer( new GenericJackson2JsonRedisSerializer(new ObjectMapper() ) );
        template.setValueSerializer( new GenericJackson2JsonRedisSerializer("class") );
        template.setKeySerializer( new GenericToStringSerializer<>(UUID.class, StandardCharsets.UTF_8) );
        return template;
    }
}
