/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.ticketing.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.school.ticketingcore.domain.reservation.dto.ConfirmReservationRequest;
import com.school.ticketingcore.domain.reservation.dto.ConfirmReservationResponse;
import com.school.ticketingcore.domain.seat.dto.HoldSeatRequest;
import com.school.ticketingcore.domain.seat.dto.HoldSeatResponse;
import com.school.ticketingcore.domain.seat.dto.SeatDto;
import com.school.ticketingcore.domain.ticketing.service.TicketingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ticketing (Public)", description = "사용자(웹/모바일)용 티켓팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TicketingController {

  private final TicketingService ticketingService;

  @Operation(
      summary = "좌석 목록 조회",
      description = "eventId에 해당하는 전체 좌석(200석) 목록과 상태(AVAILABLE/HELD/RESERVED)를 반환합니다.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
  @GetMapping("/seats")
  public List<SeatDto> seats(
      @Parameter(description = "이벤트/회차 ID", example = "E1") @RequestParam String eventId) {
    return ticketingService.listSeats(eventId);
  }

  @Operation(
      summary = "좌석 홀드(임시 선점)",
      description = "좌석을 TTL(기본 30초) 동안 임시 선점합니다. 성공 시 holdToken을 반환합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "홀드 성공"),
    @ApiResponse(responseCode = "400", description = "요청값 오류"),
    @ApiResponse(responseCode = "409", description = "이미 홀드/예약된 좌석")
  })
  @PostMapping("/seats/hold")
  public HoldSeatResponse hold(@RequestBody @Valid HoldSeatRequest req) {
    return ticketingService.holdSeat(req.eventId(), req.seatId(), req.queueToken());
  }

  @Operation(
      summary = "예약 확정 + 4자리 코드 발급",
      description = "holdToken을 검증한 뒤 예약을 확정하고 4자리 인증 코드를 발급합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "예약 확정 성공"),
    @ApiResponse(responseCode = "400", description = "요청값 오류/토큰 불일치/만료"),
    @ApiResponse(responseCode = "409", description = "동시 요청 충돌")
  })
  @PostMapping("/reservations/confirm")
  public ConfirmReservationResponse confirm(@RequestBody @Valid ConfirmReservationRequest req) {
    return ticketingService.confirm(req.eventId(), req.seatId(), req.holdToken(), req.queueToken());
  }
}
