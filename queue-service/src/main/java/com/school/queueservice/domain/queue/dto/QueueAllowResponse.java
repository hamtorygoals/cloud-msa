/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.domain.queue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 페이지 접근 허용 여부")
public record QueueAllowResponse(
    @Schema(example = "E1") String eventId, @Schema(example = "true") boolean allowed) {}
