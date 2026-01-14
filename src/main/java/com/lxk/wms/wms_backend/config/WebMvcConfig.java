package com.lxk.wms.wms_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 URL 请求 /files/** 映射到 本地磁盘路径
        // 注意：Windows下路径需要以 file: 开头
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);
    }
}