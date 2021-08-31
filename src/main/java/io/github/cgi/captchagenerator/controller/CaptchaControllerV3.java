package io.github.cgi.captchagenerator.controller;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import io.github.cgi.captchagenerator.service.CaptchaBusinessServiceV3;
import io.github.cgi.captchagenerator.service.CaptchaBusinessServiceV3Impl;
import io.github.cgi.captchagenerator.service.CaptchaServiceV2;
import io.github.cgi.captchagenerator.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Требуется:
 * + сохранять сгенерированную картинку и информацию о ней в течение времени на прохождение ( 5 мин )
 * сохранять будем в редис
 * Параметры для генерации капчи должны браться из параметров через Spring
 */
@Controller
@RequestMapping( value = { "/v3" } )
public class CaptchaControllerV3 {


    public static final String CAPTCHA_COOKIES_NAME = "cap";

    @Autowired
    private CaptchaBusinessServiceV3 captchaService;

    @GetMapping(value = "/")
    public String init() {
        return "v3/index.html";
    }

    /**
     * Кешируем в сесии и возвращаем при воторном запросе его же.
     * @param response
     */
    @GetMapping(value = "/captcha", produces = "image/png")
    @ResponseBody
    public byte[] captchaGen(
            @CookieValue(value = CAPTCHA_COOKIES_NAME, required = false) Cookie captchaCookie,
            HttpServletResponse response
    ) {

        CaptchaInfo captcha = null;

        if (captchaCookie != null) {
            UUID uuid = HttpUtil.getCaptchaUuidFromCookie(captchaCookie);
            if (uuid != null) {
                captcha = captchaService.getCaptcha(uuid);
            }
        }

        if (captcha == null) {
            CaptchaContext context = captchaService.generateCaptcha();
            Cookie cookie = HttpUtil.createCaptchaCookie(CAPTCHA_COOKIES_NAME, context);
            response.addCookie(cookie);
            captcha = context.getInfo();
        }

        return captcha.getData();
    }

    @PostMapping(value = "/captcha/ver",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object captchaVer(
            @CookieValue(CAPTCHA_COOKIES_NAME) Cookie captchaCookie,
            @RequestParam("code") String code
    ) throws Exception {
        boolean res = false;

        if ( captchaCookie != null && code != null ) {
            UUID uuid = HttpUtil.getCaptchaUuidFromCookie(captchaCookie);
            res = captchaService.verifyCaptcha(uuid, code);
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("result", res);

        return response;
    }

}
