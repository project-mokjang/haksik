package com.example.haksikmokjang.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.url-prefix}")
    private String uploadUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 팩트: 절대 경로(uploadDir)를 그대로 URI 형태로 변환하여 정적 리소스 매핑
        String uploadPath = Path.of(uploadDir).toUri().toString();

        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations(uploadPath);
    }
}