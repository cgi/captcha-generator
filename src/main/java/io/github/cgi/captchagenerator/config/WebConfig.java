package io.github.cgi.captchagenerator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.nio.charset.StandardCharsets;

@Configuration
@ComponentScan({"io.github.cgi.captchagenerator"})
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Bean
    public FreeMarkerViewResolver freemarkerViewResolver() {
        logger.info("Start freemarkerViewResolver");
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        resolver.setContentType( (new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)).toString() );
        return resolver;
    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig() {
        logger.info("Start freemarkerConfig");
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:/WEB-INF/views/ftl/");
        return freeMarkerConfigurer;
    }

}
