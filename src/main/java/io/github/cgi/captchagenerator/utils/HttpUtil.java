package io.github.cgi.captchagenerator.utils;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static Cookie createCaptchaCookie(String cookieName, CaptchaContext context){
        Cookie cookie = new Cookie(cookieName, context.getUuid().toString());
        cookie.setHttpOnly(true);

        long secTtl = TimeUnit.SECONDS.convert(context.getTtl(), context.getTimeUnit()) ;
        cookie.setMaxAge( (int) secTtl );

        return cookie;
    }

    public static UUID getCaptchaUuidFromCookie(Cookie cookie){
        Objects.requireNonNull(cookie, "captcha id cookie is null");

        String value = cookie.getValue();
        Objects.requireNonNull(value, "captcha id cookie value is null");

        UUID uuid = null;
        try {
            uuid = UUID.fromString(value);
        } catch (Exception e){
            logger.error("Error decoding cookie value to UUID. Cookie: {}", cookie, e);
        }

        // При проблемах парсинга возвращаем null
        return uuid;
    }
}
