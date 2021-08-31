package io.github.cgi.captchagenerator;

import io.github.cgi.captchagenerator.config.RedisConfig;
import io.github.cgi.captchagenerator.config.ServletConfig;
import io.github.cgi.captchagenerator.config.WebConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
@Import( { WebConfig.class, ServletConfig.class, RedisConfig.class } )
public class CaptchaGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaptchaGeneratorApplication.class, args);
	}

}
