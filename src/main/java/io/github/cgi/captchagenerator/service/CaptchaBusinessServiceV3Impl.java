package io.github.cgi.captchagenerator.service;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;
import io.github.cgi.captchagenerator.repository.CaptchaRepositoryV3;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CaptchaBusinessServiceV3Impl implements CaptchaBusinessServiceV3 {

    @Autowired
    private CaptchaServiceV2 captchaServiceV2;

    @Autowired
    private CaptchaRepositoryV3 repositoryV3;

    @Override
    public CaptchaContext generateCaptcha() {
        CaptchaInfo captcha = captchaServiceV2.getCaptcha();

        CaptchaContext context = repositoryV3.save(captcha);

        return context;
    }

    @Override
    public CaptchaInfo getCaptcha(UUID uuid) {

        CaptchaInfo captchaInfo = repositoryV3.get(uuid);

        return captchaInfo;
    }

    @Override
    public boolean verifyCaptcha(UUID uuid, String code) {

        CaptchaInfo info = repositoryV3.get(uuid);

        // Проверку капчи можно выполнить только 1 раз.
        // поэтому удаляем значение после чтения - оно нам в любом случае больше не понадобится
        repositoryV3.delete(uuid);

        return info != null &&
                info.getResultCode() != null &&
                info.getResultCode().equalsIgnoreCase( code );
    }
}
