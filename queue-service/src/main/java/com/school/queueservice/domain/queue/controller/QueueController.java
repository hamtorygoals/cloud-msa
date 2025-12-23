/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.domain.queue.controller;

import org.springframework.web.bind.annotation.*;

import com.school.queueservice.domain.queue.dto.EnterQueueResponse;
import com.school.queueservice.domain.queue.dto.QueueAllowResponse;
import com.school.queueservice.domain.queue.dto.QueueStatusResponse;
import com.school.queueservice.domain.queue.service.QueueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Queue", description = "Redis 기반 대기열 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queue")
public class QueueController {

  private final QueueService queueService;

  @Operation(summary = "대기열 입장", description = "대기열 토큰 발급 + 현재 순번/대기시간")
  @PostMapping("/enter")
  public EnterQueueResponse enter(@RequestParam String eventId) {
    return queueService.enter(eventId);
  }

  @Operation(summary = "대기열 상태", description = "token으로 순번/대기시간/허용 여부")
  @GetMapping("/status")
  public QueueStatusResponse status(@RequestParam String eventId, @RequestParam String token) {
    queueService.refreshAllowed(eventId);
    return queueService.status(eventId, token);
  }

  @Operation(summary = "좌석 접근 허용 여부", description = "token이 좌석 선택 가능한지")
  @GetMapping("/allow")
  public QueueAllowResponse allow(@RequestParam String eventId, @RequestParam String token) {
    queueService.refreshAllowed(eventId);
    return queueService.allow(eventId, token);
  }
}
