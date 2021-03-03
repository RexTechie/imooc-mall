package com.imooc.mall.config;

import com.imooc.mall.common.Constant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置地址映射
 * @author Rex
 * @create 2021-02-09 13:22
 */
@Configuration
public class ImoocMallWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:"+ Constant.FILE_UPLOAD_DIR);
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");


    }
}
