package com.lwq.config;

import com.lwq.Controller.Interceptor.LoginRequiredInterceptor;
import com.lwq.Controller.Interceptor.LoginTicketInterceptor;
import com.lwq.Controller.Interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {



    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/static/**");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/static/**");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/static/**");
    }

}
