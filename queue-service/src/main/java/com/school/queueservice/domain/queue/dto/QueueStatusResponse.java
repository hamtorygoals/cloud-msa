/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.domain.queue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 상태 응답")
public record QueueStatusResponse(
    @Schema(example = "E1") String eventId,
    @Schema(example = "q_1f2a...") String queueToken,
    @Schema(example = "128") long position,
    @Schema(example = "640") long estimatedWaitSec,
    @Schema(example = "false") boolean allowed) {}
