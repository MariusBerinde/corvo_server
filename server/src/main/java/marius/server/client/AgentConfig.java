package marius.server.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Class used for config the AngentConfig
 * @author  Marius Berinde
 */
@Configuration
public class AgentConfig {
    @Bean
    public RestTemplate restTemplate (RestTemplateBuilder builder){
        return builder.build();
    }
}
