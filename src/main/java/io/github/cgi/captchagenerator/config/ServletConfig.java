package io.github.cgi.captchagenerator.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;

@Configuration
@AllArgsConstructor
public class ServletConfig {

    DispatcherServlet servlet;

    @PostConstruct
    public void init() {
        // Debug mode
        servlet.setEnableLoggingRequestDetails(true);

    }
}