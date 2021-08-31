package io.github.cgi.captchagenerator.service;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;

import java.util.UUID;

public interface CaptchaBusinessServiceV3 {

    public CaptchaContext generateCaptcha();

    public CaptchaInfo getCaptcha(UUID uuid);

    public boolean verifyCaptcha(UUID uuid, String code);

}
