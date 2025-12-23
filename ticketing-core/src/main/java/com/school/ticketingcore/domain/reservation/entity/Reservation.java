/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.reservation.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(
    name = "reservations",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_res_event_seat",
          columnNames = {"eventId", "seatId"}),
      @UniqueConstraint(
          name = "uk_res_event_code",
          columnNames = {"eventId", "code4"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation {

  @Id private String id; // UUID 문자열

  @Column(nullable = false)
  private String eventId;

  @Column(nullable = false)
  private String seatId;

  @Column(nullable = false, length = 4)
  private String code4; // 0000~9999

  @Column(nullable = false)
  private Instant reservedAt;

  public static Reservation create(String eventId, String seatId, String code4) {
    return Reservation.builder()
        .id(UUID.randomUUID().toString())
        .eventId(eventId)
        .seatId(seatId)
        .code4(code4)
        .reservedAt(Instant.now())
        .build();
  }
}
