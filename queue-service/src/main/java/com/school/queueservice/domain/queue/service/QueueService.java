/* 
 * Copyright (c) queue-service 
 */
package com.school.queueservice.domain.queue.service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.school.queueservice.domain.queue.dto.EnterQueueResponse;
import com.school.queueservice.domain.queue.dto.QueueAllowResponse;
import com.school.queueservice.domain.queue.dto.QueueStatusResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

  private final StringRedisTemplate redis;

  private static final Duration TOKEN_TTL = Duration.ofHours(2);

  private static final boolean DEFAULT_ENABLED = true;
  private static final int DEFAULT_ALLOWED_COUNT = 10;
  private static final int DEFAULT_AVG_SEC_PER_USER = 5;

  public EnterQueueResponse enter(String eventId) {
    boolean enabled = getEnabled(eventId);

    String token = issueToken();
    redis.opsForValue().set(tokenKey(token), eventId, TOKEN_TTL);

    if (!enabled) {
      addAllowed(eventId, token);
      return new EnterQueueResponse(eventId, token, 0, 0, true);
    }

    long now = System.currentTimeMillis();
    redis.opsForZSet().add(queueKey(eventId), token, now);

    refreshAllowed(eventId);
    return toEnterResponse(status(eventId, token));
  }

  public QueueStatusResponse status(String eventId, String token) {
    String savedEvent = redis.opsForValue().get(tokenKey(token));
    if (savedEvent == null || !savedEvent.equals(eventId)) {
      throw new IllegalArgumentException("유효하지 않은 대기열 토큰입니다.");
    }

    if (isAllowed(eventId, token)) {
      return new QueueStatusResponse(eventId, token, 0, 0, true);
    }

    Long rank = redis.opsForZSet().rank(queueKey(eventId), token);
    if (rank == null) {
      throw new IllegalArgumentException("대기열에서 토큰을 찾을 수 없습니다(만료/삭제).");
    }

    long position = rank + 1;
    int avgSec = getAvgSecPerUser(eventId);
    long estimatedWaitSec = position * (long) avgSec;

    return new QueueStatusResponse(eventId, token, position, estimatedWaitSec, false);
  }

  public QueueAllowResponse allow(String eventId, String token) {
    QueueStatusResponse st = status(eventId, token);
    return new QueueAllowResponse(eventId, st.allowed());
  }

  public void refreshAllowed(String eventId) {
    int allowedCount = getAllowedCount(eventId);

    Set<String> top = redis.opsForZSet().range(queueKey(eventId), 0, allowedCount - 1);
    if (top == null || top.isEmpty()) return;

    for (String token : top) addAllowed(eventId, token);
  }

  public void setConfig(String eventId, boolean enabled, int allowedCount, int avgSecPerUser) {
    redis.opsForHash().put(configKey(eventId), "enabled", String.valueOf(enabled));
    redis.opsForHash().put(configKey(eventId), "allowedCount", String.valueOf(allowedCount));
    redis.opsForHash().put(configKey(eventId), "avgSecPerUser", String.valueOf(avgSecPerUser));
  }

  // ===== helpers =====
  private EnterQueueResponse toEnterResponse(QueueStatusResponse st) {
    return new EnterQueueResponse(
        st.eventId(), st.queueToken(), st.position(), st.estimatedWaitSec(), st.allowed());
  }

  private void addAllowed(String eventId, String token) {
    redis.opsForSet().add(allowedKey(eventId), token);
  }

  private boolean isAllowed(String eventId, String token) {
    Boolean ok = redis.opsForSet().isMember(allowedKey(eventId), token);
    return Boolean.TRUE.equals(ok);
  }

  private String issueToken() {
    return "q_" + UUID.randomUUID();
  }

  private boolean getEnabled(String eventId) {
    Object v = redis.opsForHash().get(configKey(eventId), "enabled");
    if (v == null) return DEFAULT_ENABLED;
    return Boolean.parseBoolean(v.toString());
  }

  private int getAllowedCount(String eventId) {
    Object v = redis.opsForHash().get(configKey(eventId), "allowedCount");
    if (v == null) return DEFAULT_ALLOWED_COUNT;
    return Integer.parseInt(v.toString());
  }

  private int getAvgSecPerUser(String eventId) {
    Object v = redis.opsForHash().get(configKey(eventId), "avgSecPerUser");
    if (v == null) return DEFAULT_AVG_SEC_PER_USER;
    return Integer.parseInt(v.toString());
  }

  // ===== keys =====
  private String queueKey(String eventId) {
    return "queue:%s".formatted(eventId);
  }

  private String allowedKey(String eventId) {
    return "queue:allowed:%s".formatted(eventId);
  }

  private String tokenKey(String token) {
    return "queue:token:%s".formatted(token);
  }

  private String configKey(String eventId) {
    return "queue:config:%s".formatted(eventId);
  }
}
