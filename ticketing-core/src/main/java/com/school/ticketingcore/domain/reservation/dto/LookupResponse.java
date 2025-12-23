/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.reservation.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 조회 응답(보안요원용)")
public record LookupResponse(
    @Schema(description = "이벤트/회차 ID", example = "E1") String eventId,
    @Schema(description = "좌석 ID", example = "A1") String seatId,
    @Schema(description = "4자리 인증 코드", example = "0421") String code4,
    @Schema(description = "예약 확정 시간") Instant reservedAt) {}
