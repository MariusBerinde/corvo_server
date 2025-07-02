package marius.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsoConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Frontend Angular in container (porta 4200 mappata su 80)
                .allowedOrigins(
                        "http://localhost:4200",  // Frontend Angular
                        "http://127.0.0.1:4200",
                        "http://localhost",       // Nginx se presente
                        "http://127.0.0.1",
                        "http://localhost:80",
                        "http://127.0.0.1:80"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight per 1 ora
    }
}
