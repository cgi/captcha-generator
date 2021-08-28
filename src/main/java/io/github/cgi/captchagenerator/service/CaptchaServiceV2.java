package io.github.cgi.captchagenerator.service;

import com.wf.captcha.SpecCaptcha;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

@NoArgsConstructor
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "captcha")
public class CaptchaServiceV2 {

    public static final int IMG_START_SIZE = 10240;

    private static Logger logger = LoggerFactory.getLogger(CaptchaServiceV2.class);

    private int width;

    private int height;

    private int captchaLen;

    private int fontNum;


    public CaptchaInfo getCaptcha() {
        CaptchaInfo info = new CaptchaInfo();

        SpecCaptcha captcha = new SpecCaptcha(width, height, captchaLen);
        setCaptchaFont(captcha);

        info.setStatus( CaptchaInfo.Status.NEW );
        info.setCreated( new Date() );
        info.setResultCode( captcha.text() );

        ByteArrayOutputStream os = new ByteArrayOutputStream( IMG_START_SIZE );
        captcha.out( os );

        info.setData( os.toByteArray() );
        return info;
    }

    private void setCaptchaFont(SpecCaptcha captcha) {
        try {
            captcha.setFont(fontNum);
        } catch (IOException | FontFormatException e) {
            logger.error("Error setting captcha font", e);
        }
    }

}
