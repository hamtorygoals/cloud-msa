/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.global.config;

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
                .title("Ticketing Core API")
                .description("대기열/좌석 홀드/예약 확정/4자리 코드 발급을 담당하는 Core 서비스")
                .version("v1"));
  }
}
