package io.github.cgi.captchagenerator.controller;

import com.wf.captcha.utils.CaptchaUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping( value = { "/v1" } )
public class CaptchaControllerV1 {

    /**
     * Метод возвращает html страницу
     */
    @GetMapping(value = "/")
    public String init() {
        return "v1/index.html";
    }

    /**
     * Метод возвращает картинку
     */
    @GetMapping("/captcha")
    public void captcha1(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CaptchaUtil.out(200, 64,7, request, response);
    }

    /**
     * Метод проверяет значение пользователя
     */
    @PostMapping(value = "/captcha/ver",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object captcha1Ver(HttpServletRequest request, @RequestParam("code") String code) throws Exception {
        boolean res = CaptchaUtil.ver(code, request);

        Map<String, Boolean> response = new HashMap<>();
        response.put("result", res);

        return response;
    }


}
