/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "좌석 정보 DTO")
public record SeatDto(
    @Schema(description = "좌석 ID", example = "A1") String seatId,
    @Schema(description = "행 라벨", example = "A") String rowLabel,
    @Schema(description = "열 번호", example = "1") int colNumber,
    @Schema(
            description = "좌석 상태",
            example = "AVAILABLE",
            allowableValues = {"AVAILABLE", "HELD", "RESERVED"})
        String status) {}
