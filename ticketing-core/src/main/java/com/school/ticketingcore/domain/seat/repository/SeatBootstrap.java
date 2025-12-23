/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.seat.repository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.school.ticketingcore.domain.seat.entity.Seat;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SeatBootstrap implements CommandLineRunner {

  private final SeatRepository seatRepository;

  @Override
  public void run(String... args) {
    String eventId = "E1";

    // 이미 있으면 스킵
    if (seatRepository.existsByEventIdAndSeatId(eventId, "A1")) return;

    // 20 rows (A~T) * 10 cols (1~10) = 200
    for (char r = 'A'; r <= 'T'; r++) {
      for (int c = 1; c <= 10; c++) {
        String seatId = "" + r + c; // A1
        seatRepository.save(
            Seat.builder()
                .eventId(eventId)
                .seatId(seatId)
                .rowLabel(String.valueOf(r))
                .colNumber(c)
                .build());
      }
    }
  }
}
