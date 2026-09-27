package ru.asocial.learn.day2.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Добавляет префикс "/api" ко всем контроллерам
        configurer.addPathPrefix("/api", c -> c.getPackageName().startsWith("ru.asocial.learn"));
    }
}

