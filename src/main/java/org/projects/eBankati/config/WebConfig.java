package org.projects.eBankati.config;

import org.projects.eBankati.filter.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthenticationFilter());

        // Protéger toutes les routes /api/ SAUF /api/auth/
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.addInitParameter("excludePatterns", "/api/auth/login,/api/auth/register");

        return registrationBean;
    }
}