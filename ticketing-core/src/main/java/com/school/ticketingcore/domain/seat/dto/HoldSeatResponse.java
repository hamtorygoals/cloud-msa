/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 홀드 응답")
public record HoldSeatResponse(
    @Schema(description = "홀드 토큰(확정 시 필요)", example = "b1c2d3e4-...") String holdToken,
    @Schema(description = "홀드 만료(초)", example = "30") int expiresInSec) {}
