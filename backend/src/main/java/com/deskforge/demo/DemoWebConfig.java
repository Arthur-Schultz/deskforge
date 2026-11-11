package com.deskforge.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DemoWebConfig implements WebMvcConfigurer {

    private final DemoReadOnlyPanelInterceptor readOnlyPanel;

    public DemoWebConfig(DemoReadOnlyPanelInterceptor readOnlyPanel) {
        this.readOnlyPanel = readOnlyPanel;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(readOnlyPanel).addPathPatterns("/api/admin/**");
    }
}
