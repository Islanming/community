package com.example.community.config;



import com.example.community.controller.interceptor.DateInterceptor;
import com.example.community.controller.interceptor.LoginRequiredInterceptor;
import com.example.community.controller.interceptor.LoginTicketInterceptor;
import com.example.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * @author Lenovo
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DateInterceptor dateInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(dateInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
