/* 
 * Copyright (c) ticketing-core 
 */
package com.school.ticketingcore.domain.ticketing.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.school.ticketingcore.domain.reservation.dto.ConfirmReservationResponse;
import com.school.ticketingcore.domain.reservation.dto.LookupResponse;
import com.school.ticketingcore.domain.reservation.entity.Reservation;
import com.school.ticketingcore.domain.reservation.repository.ReservationRepository;
import com.school.ticketingcore.domain.seat.dto.HoldSeatResponse;
import com.school.ticketingcore.domain.seat.dto.SeatDto;
import com.school.ticketingcore.domain.seat.entity.Seat;
import com.school.ticketingcore.domain.seat.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketingService {

  private static final int HOLD_TTL_SEC = 30;
  private static final Duration CODE_TTL = Duration.ofHours(12); // 행사 시간에 맞게 조절
  private final SeatRepository seatRepository;
  private final ReservationRepository reservationRepository;
  private final StringRedisTemplate redis;
  private final RestClient queueClient;

  private final Random random = new Random();

  public List<SeatDto> listSeats(String eventId) {
    List<Seat> seats = seatRepository.findAllByEventIdOrderByRowLabelAscColNumberAsc(eventId);

    return seats.stream()
        .map(
            seat -> {
              boolean reserved =
                  reservationRepository.existsByEventIdAndSeatId(eventId, seat.getSeatId());
              String holdKey = holdKey(eventId, seat.getSeatId());
              boolean held = Boolean.TRUE.equals(redis.hasKey(holdKey));

              String status = reserved ? "RESERVED" : (held ? "HELD" : "AVAILABLE");
              return SeatDto.builder()
                  .seatId(seat.getSeatId())
                  .rowLabel(seat.getRowLabel())
                  .colNumber(seat.getColNumber())
                  .status(status)
                  .build();
            })
        .toList();
  }

  public HoldSeatResponse holdSeat(String eventId, String seatId, String queueToken) {
    ensureAllowed(eventId, queueToken); // 추가

    seatRepository
        .findByEventIdAndSeatId(eventId, seatId)
        .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));

    if (reservationRepository.existsByEventIdAndSeatId(eventId, seatId)) {
      throw new IllegalStateException("이미 예약된 좌석입니다.");
    }

    String holdToken = UUID.randomUUID().toString();
    String key = holdKey(eventId, seatId);

    Boolean ok = redis.opsForValue().setIfAbsent(key, holdToken, Duration.ofSeconds(HOLD_TTL_SEC));
    if (Boolean.TRUE.equals(ok)) {
      return new HoldSeatResponse(holdToken, HOLD_TTL_SEC);
    }
    throw new IllegalStateException("이미 다른 사용자가 선점(hold)한 좌석입니다.");
  }

  @Transactional
  public ConfirmReservationResponse confirm(
      String eventId, String seatId, String holdToken, String queueToken) {
    ensureAllowed(eventId, queueToken); // 추가

    seatRepository
        .findByEventIdAndSeatId(eventId, seatId)
        .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));

    // 이미 예약이면 기존 코드 반환 (중복 클릭 대비)
    Optional<Reservation> already = reservationRepository.findByEventIdAndSeatId(eventId, seatId);
    if (already.isPresent()) {
      return new ConfirmReservationResponse(already.get().getCode4());
    }

    // hold 검증
    String key = holdKey(eventId, seatId);
    String stored = redis.opsForValue().get(key);
    if (stored == null) throw new IllegalStateException("홀드가 만료되었습니다. 다시 선택해주세요.");
    if (!stored.equals(holdToken)) throw new IllegalStateException("홀드 토큰이 일치하지 않습니다.");

    // hold 소비(삭제) - 여기서부터 확정 흐름
    redis.delete(key);

    // 4자리 코드 발급 + Redis 매핑 선점
    String code4 = allocateCode4(eventId);

    Reservation reservation = Reservation.create(eventId, seatId, code4);

    try {
      reservationRepository.save(reservation);
    } catch (DataIntegrityViolationException e) {
      // 혹시나 경쟁 상태로 seat unique 충돌 시 (거의 없음), 안내
      throw new IllegalStateException("동시 요청으로 예약 확정에 실패했습니다. 다시 시도해주세요.");
    }

    // 코드 -> 예약ID 매핑 (보안요원 조회)
    String codeKey = codeKey(eventId, code4);
    redis.opsForValue().set(codeKey, reservation.getId(), CODE_TTL);

    return new ConfirmReservationResponse(code4);
  }

  public LookupResponse lookupByCode(String eventId, String code4) {
    String codeKey = codeKey(eventId, code4);
    String reservationId = redis.opsForValue().get(codeKey);
    if (reservationId == null) throw new IllegalArgumentException("해당 코드가 없거나 만료되었습니다.");

    Reservation r =
        reservationRepository
            .findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

    return new LookupResponse(r.getEventId(), r.getSeatId(), r.getCode4(), r.getReservedAt());
  }

  private String allocateCode4(String eventId) {
    // 0000~9999 중 랜덤, Redis setIfAbsent로 충돌 회피
    for (int i = 0; i < 30; i++) {
      String code = String.format("%04d", random.nextInt(10000));
      String key = codeKey(eventId, code);

      Boolean ok = redis.opsForValue().setIfAbsent(key, "PENDING", Duration.ofMinutes(2));
      if (Boolean.TRUE.equals(ok)) {
        // 임시 선점(PENDING) -> confirm에서 reservationId로 덮어씀
        return code;
      }
    }
    throw new IllegalStateException("인증 코드가 부족합니다. 잠시 후 다시 시도해주세요.");
  }

  private String holdKey(String eventId, String seatId) {
    return "hold:%s:%s".formatted(eventId, seatId);
  }

  private String codeKey(String eventId, String code4) {
    return "code:%s:%s".formatted(eventId, code4);
  }

  // 큐로 인해 추가
  private void ensureAllowed(String eventId, String queueToken) {
    AllowResponse res =
        queueClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/queue/allow")
                        .queryParam("eventId", eventId)
                        .queryParam("token", queueToken)
                        .build())
            .retrieve()
            .body(AllowResponse.class);

    if (res == null || !res.allowed()) {
      throw new IllegalStateException("아직 좌석 선택 순번이 아닙니다. 대기열에서 기다려주세요.");
    }
  }

  public record AllowResponse(String eventId, boolean allowed) {}
}
