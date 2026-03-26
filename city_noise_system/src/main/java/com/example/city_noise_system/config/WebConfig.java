package com.example.city_noise_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于配置静态资源访问路径等Web相关设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源访问路径
     * 将 /uploads/** 映射到项目根目录下的 uploads/ 目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取应用运行目录的绝对路径
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        System.out.println("静态资源路径: " + uploadPath);
        
        // 配置上传文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600);
    }
}
