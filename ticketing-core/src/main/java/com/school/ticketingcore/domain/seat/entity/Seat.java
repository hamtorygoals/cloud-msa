/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.seat.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(
    name = "seats",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_seat_event_seat",
            columnNames = {"eventId", "seatId"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Seat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String eventId;

  @Column(nullable = false)
  private String seatId; // e.g. "A1"

  @Column(nullable = false)
  private String rowLabel; // e.g. "A"

  @Column(nullable = false)
  private int colNumber; // e.g. 1
}
