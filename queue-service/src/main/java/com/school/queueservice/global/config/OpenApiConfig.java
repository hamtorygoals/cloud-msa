/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Queue Service API")
                .description("Redis 기반 대기열 서비스 (enter/status/allow/admin-config)")
                .version("v1"));
  }
}
