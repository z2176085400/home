package com.zkz.home.config;

import com.zkz.common.api.util.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author
 */
@Configuration
public class IdGeneratorConfig {
    @Bean
    public IdGenerator createIG() {
        return new IdGenerator();
    }
}
