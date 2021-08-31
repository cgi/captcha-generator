package io.github.cgi.captchagenerator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Вспомогательный класс для хранения метаданных
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaContext {

    private CaptchaInfo info;

    private UUID uuid;

    private long ttl;

    private TimeUnit timeUnit;
}
