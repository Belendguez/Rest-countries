package com.restcountries.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching  // Activa el soporte de caché en la aplicación
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Configura el tiempo de expiración
                .maximumSize(200));  // Limita el tamaño de la caché Paises

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(200)   // Cache 2 Vecinos, tamaño máximo
                .expireAfterWrite(5, TimeUnit.MINUTES));  // Expira después de 5 minutos

        return cacheManager;
    }
}

