package br.ueg.meueg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // libera todas as rotas
                .allowedOrigins("*") // libera qualquer origem (ideal para desenvolvimento)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // libera métodos
                .allowedHeaders("*") // libera qualquer cabeçalho
                .allowCredentials(false); // false porque estamos usando sem cookies/sessão
    }
}
