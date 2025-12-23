/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.reservation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.ticketingcore.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
  boolean existsByEventIdAndSeatId(String eventId, String seatId);

  Optional<Reservation> findByEventIdAndSeatId(String eventId, String seatId);

  Optional<Reservation> findByEventIdAndCode4(String eventId, String code4);
}
