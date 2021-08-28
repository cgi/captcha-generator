package io.github.cgi.captchagenerator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CaptchaInfo {

    private byte[] data;

    private String resultCode;

    private Date created;

    private Status status;

    @Getter
    @AllArgsConstructor
    public enum Status {
        NEW(true),
        SOLVED(false),
        FAILED(false);

        private final boolean isActive;

    }
}
