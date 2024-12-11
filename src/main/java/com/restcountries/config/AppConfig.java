package com.restcountries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // Definir un RestTemplate para hacer las solicitudes HTTP
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
