package com.example.recordprocessor.config;

import com.example.recordprocessor.security.ApiKeyAuthentication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    private final ApiKeyAuthentication apiKeyAuthentication;

    public WebConfig(ApiKeyAuthentication apiKeyAuthentication) {
        this.apiKeyAuthentication = apiKeyAuthentication;
    }

    @Bean
    public FilterRegistrationBean<ApiKeyAuthentication> apiKeyFilter() {
        FilterRegistrationBean<ApiKeyAuthentication> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(apiKeyAuthentication);
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}

