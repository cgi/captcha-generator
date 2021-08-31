package io.github.cgi.captchagenerator.repository;

import io.github.cgi.captchagenerator.model.CaptchaContext;
import io.github.cgi.captchagenerator.model.CaptchaInfo;

import java.util.UUID;

public interface CaptchaRepositoryV3 {

    CaptchaContext save(CaptchaInfo info);

    CaptchaInfo get(UUID uuid);

    void delete(UUID uuid);
}
