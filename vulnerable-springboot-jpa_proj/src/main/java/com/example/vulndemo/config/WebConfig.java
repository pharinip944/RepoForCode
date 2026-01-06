
package com.example.vulndemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Intentionally permissive CORS: allows everything. */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")   // Overly permissive
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
