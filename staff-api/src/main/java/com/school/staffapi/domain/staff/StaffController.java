/* 
 * Copyright (c) staff-api 
 */
package com.school.staffapi.domain.staff;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Staff", description = "보안요원 전용 API")
@RestController
@RequestMapping("/staff")
public class StaffController {

  private final RestClient core;

  public StaffController(RestClient ticketingCoreClient) {
    this.core = ticketingCoreClient;
  }

  @Operation(summary = "예약 조회", description = "보안요원이 eventId + 4자리 코드로 예약 좌석을 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "400", description = "코드 없음/만료 등")
  })
  @GetMapping("/lookup")
  public Object lookup(
      @Parameter(description = "이벤트/회차 ID", example = "E1") @RequestParam String eventId,
      @Parameter(description = "4자리 인증 코드", example = "0421") @RequestParam String code4) {
    return core.get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/internal/reservations/lookup")
                    .queryParam("eventId", eventId)
                    .queryParam("code4", code4)
                    .build())
        .retrieve()
        .body(Object.class);
  }
}
