/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.ticketing.controller;

import org.springframework.web.bind.annotation.*;

import com.school.ticketingcore.domain.reservation.dto.LookupResponse;
import com.school.ticketingcore.domain.ticketing.service.TicketingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Internal", description = "내부 호출용 API (staff-api가 호출)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalStaffController {

  private final TicketingService ticketingService;

  @Operation(summary = "예약 조회(내부)", description = "eventId + code4로 예약 좌석을 조회합니다. (staff-api가 호출)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "400", description = "코드 없음/만료/예약 데이터 없음")
  })
  @GetMapping("/reservations/lookup")
  public LookupResponse lookup(
      @Parameter(description = "이벤트/회차 ID", example = "E1") @RequestParam String eventId,
      @Parameter(description = "4자리 인증 코드", example = "0421") @RequestParam String code4) {
    return ticketingService.lookupByCode(eventId, code4);
  }
}
