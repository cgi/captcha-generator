package io.github.cgi.captchagenerator.controller;

import com.wf.captcha.utils.CaptchaUtil;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import io.github.cgi.captchagenerator.service.CaptchaServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Требуется:
 * + сохранять сгенерированную картинку и информацию о ней в течение времени на прохождение ( 5 мин )
 * сохранять будем в редис
 * Параметры для генерации капчи должны браться из параметров через Spring
 */
@Controller
@RequestMapping( value = { "/v2" } )
public class CaptchaControllerV2 {


    public static final String CAPTCHA_SESSION_KEY = "c";
    @Autowired
    private CaptchaServiceV2 captchaServiceV2;

    @GetMapping(value = "/")
    public String init() {
        return "v2/index.html";
    }

    /**
     * Кешируем в сесии и возвращаем при воторном запросе его же.
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping("/captcha")
    public void captcha1(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CaptchaInfo captcha = null;
        Object o = request.getSession().getAttribute(CAPTCHA_SESSION_KEY);

        if( o instanceof CaptchaInfo){
            captcha = (CaptchaInfo) o;
        }

        if (captcha == null) {
            captcha = captchaServiceV2.getCaptcha();
            request.getSession().setAttribute(CAPTCHA_SESSION_KEY, captcha);
        }

        response.setContentType("image/png");

        response.getOutputStream().write( captcha.getData() );
    }

    @PostMapping(value = "/captcha/ver",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object captcha1Ver(HttpServletRequest request, @RequestParam("code") String code) throws Exception {
        Object o = request.getSession().getAttribute(CAPTCHA_SESSION_KEY);
        boolean res = false;

        if (o instanceof CaptchaInfo ){
            CaptchaInfo info = (CaptchaInfo) o;
            res = info.getResultCode() != null && info.getResultCode().equalsIgnoreCase( code );
        }

        request.getSession().removeAttribute(CAPTCHA_SESSION_KEY);

        Map<String, Boolean> response = new HashMap<>();
        response.put("result", res);

        return response;
    }


}
