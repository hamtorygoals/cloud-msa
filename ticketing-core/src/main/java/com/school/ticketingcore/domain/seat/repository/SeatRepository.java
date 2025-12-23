/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.seat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.ticketingcore.domain.seat.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

  List<Seat> findAllByEventIdOrderByRowLabelAscColNumberAsc(String eventId);

  Optional<Seat> findByEventIdAndSeatId(String eventId, String seatId);

  boolean existsByEventIdAndSeatId(String eventId, String seatId);
}
