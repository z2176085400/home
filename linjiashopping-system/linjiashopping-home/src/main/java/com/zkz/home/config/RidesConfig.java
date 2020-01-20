package com.zkz.home.config;

import com.zkz.common.api.util.JedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RidesConfig {
    @Bean
    public JedisUtil createJU() {
        return JedisUtil.getInstance();
    }

}
