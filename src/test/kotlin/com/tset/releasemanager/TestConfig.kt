package com.tset.releasemanager

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@EnableWebMvc
@Configuration
class TestConfig {
    @Bean
    fun getReleaseManagerService() = ReleaseManagerServiceImpl()
}