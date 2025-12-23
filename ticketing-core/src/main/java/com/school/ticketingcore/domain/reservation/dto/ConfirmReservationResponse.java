/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 확정 응답")
public record ConfirmReservationResponse(
    @Schema(description = "4자리 인증 코드", example = "0421") String code4) {}
