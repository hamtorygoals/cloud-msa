/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.seat.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 홀드 요청")
public record HoldSeatRequest(
    @Schema(description = "이벤트/회차 ID", example = "E1") @NotBlank String eventId,
    @Schema(description = "좌석 ID", example = "A1") @NotBlank String seatId,
    @NotBlank String queueToken) {} // 추가
