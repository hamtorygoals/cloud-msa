/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 확정 요청")
public record ConfirmReservationRequest(
    @Schema(description = "이벤트/회차 ID", example = "E1") @NotBlank String eventId,
    @Schema(description = "좌석 ID", example = "A1") @NotBlank String seatId,
    @Schema(description = "홀드 토큰", example = "b1c2d3e4-...") @NotBlank String holdToken,
    @NotBlank String queueToken) {} // 추가
