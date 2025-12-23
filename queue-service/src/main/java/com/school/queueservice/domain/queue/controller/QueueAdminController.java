/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.domain.queue.controller;

import org.springframework.web.bind.annotation.*;

import com.school.queueservice.domain.queue.dto.QueueAdminRequest;
import com.school.queueservice.domain.queue.service.QueueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Queue(Admin)", description = "대기열 설정(데모용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/queue")
public class QueueAdminController {

  private final QueueService queueService;

  @Operation(summary = "대기열 설정", description = "enabled/allowedCount/avgSecPerUser")
  @PostMapping("/config")
  public void config(@RequestParam String eventId, @RequestBody QueueAdminRequest req) {
    queueService.setConfig(eventId, req.enabled(), req.allowedCount(), req.avgSecPerUser());
  }
}
