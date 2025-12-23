/* 
 * Copyright (c) staff-api 
 */
package com.school.staffapi.global.config;

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
                .title("Staff API")
                .description("보안요원(스태프) 조회 전용 API - 4자리 코드로 예약 좌석 조회")
                .version("v1"));
  }
}
