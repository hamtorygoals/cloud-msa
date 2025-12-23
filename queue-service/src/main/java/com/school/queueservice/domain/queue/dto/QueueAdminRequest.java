/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.domain.queue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 설정 요청(관리자)")
public record QueueAdminRequest(
    @Schema(example = "true") boolean enabled,
    @Schema(example = "200") int allowedCount,
    @Schema(example = "5") int avgSecPerUser) {}
