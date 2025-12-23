/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class QueueClientConfig {

  @Bean
  RestClient queueClient(@Value("${queue.base-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }
}
