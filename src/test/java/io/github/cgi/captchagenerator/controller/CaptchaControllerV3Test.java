package io.github.cgi.captchagenerator.controller;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import io.github.cgi.captchagenerator.repository.CaptchaRepositoryV3;
import io.github.cgi.captchagenerator.service.CaptchaBusinessServiceV3Impl;
import io.github.cgi.captchagenerator.service.CaptchaServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { CaptchaControllerV3.class })
class CaptchaControllerV3Test {

    public static final String UUID_REGEXP = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaptchaBusinessServiceV3Impl captchaBusinessServiceV3;

    @MockBean
    private CaptchaServiceV2 captchaServiceV2;

    @MockBean
    private CaptchaRepositoryV3 repositoryV3;

    @BeforeEach
    void setup(){
        captchaBusinessServiceV3.setCaptchaServiceV2(captchaServiceV2);
        captchaBusinessServiceV3.setRepositoryV3(repositoryV3);
    }

    @Test
    void init() throws Exception {
        mockMvc.perform(get("/v3/"))
                .andExpect( status().isOk() )
                .andExpect( content().contentTypeCompatibleWith("text/html") )
                .andExpect( content().string( containsString("src=\"captcha\"") ) );
    }

    /**
     * Пример неудачного теста:
     * Хотелось проверить поведение контроллера при отправке
     * повторных запросов с выставленными в предыдущем ответе куками.
     * Но в итоге пришлось писать моки почти на все сервисы,
     * и не факт что тест проверяет то что надо - это еще нужно проверить :(...
     *
     * Считаю это примером теста, который не получается провести
     * на данном уровне тестирования - это кандидат для проверки
     * при поднятии полного Spring-контекста.
     * @throws Exception
     */
    @Test
    void captchaGen() throws Exception {
        AtomicReference<Cookie> cookie = new AtomicReference<>();
        AtomicReference<byte[]> data = new AtomicReference<>();

        CaptchaInfo mockCaptcha = new CaptchaInfo(
                new byte[]{1, 2, 3},
                "testCode",
                new Date(),
                CaptchaInfo.Status.NEW);
        CaptchaContext mockContext = new CaptchaContext(
                mockCaptcha, UUID.randomUUID(), 120L, TimeUnit.SECONDS);

        Mockito.when(captchaServiceV2.getCaptcha())
                .thenReturn(mockCaptcha);

        Mockito.when(repositoryV3.save( Mockito.any(CaptchaInfo.class) ))
                        .thenReturn(mockContext);


        mockMvc.perform(get("/v3/captcha"))
                .andExpect( status().isOk() )
                .andExpect( content().contentTypeCompatibleWith("image/png") )
                .andExpect( cookie().value("cap", matchesRegex(UUID_REGEXP) ) )
                .andDo(result -> {
                    cookie.set(result.getResponse().getCookie("cap"));
                    data.set(result.getResponse().getContentAsByteArray());
                });

        // Проверяем, что второй вызов возвращает тоже значение...
        // Вот только этого ли уровня эта проверка?
        mockMvc.perform(get("/v3/captcha").cookie(cookie.get()))
                .andExpect(content().bytes(data.get()));

    }

    @Test
    void captchaVerNoCookie() throws Exception {
        mockMvc.perform(
                post("/v3/captcha/ver")
                        .param("code", "testCode")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect( status().is(400) );
    }

    @Test
    void captchaVerWithWrongCookie() throws Exception {
        mockMvc.perform(
                        post("/v3/captcha/ver")
                                .param("code", "testCode")
                                .cookie( new Cookie("cap", UUID.randomUUID().toString() ))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect( status().isOk() )
                .andExpect(content().json("{\"result\":false}"));
    }


    @Test
    void captchaVerWithCorrectCookie() throws Exception {
        CaptchaInfo mockCaptcha = new CaptchaInfo(
                new byte[]{1, 2, 3},
                "testCode",
                new Date(),
                CaptchaInfo.Status.NEW);

        Mockito.when(repositoryV3.get( Mockito.any(UUID.class) ))
                .thenReturn(mockCaptcha);

        mockMvc.perform(
                        post("/v3/captcha/ver")
                                .param("code", "testCode")
                                .cookie( new Cookie("cap", UUID.randomUUID().toString() ))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect( status().isOk() )
                .andExpect(content().json("{\"result\":true}"));
    }

}