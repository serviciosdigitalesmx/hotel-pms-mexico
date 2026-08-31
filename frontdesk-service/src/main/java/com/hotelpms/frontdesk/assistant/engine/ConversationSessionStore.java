package com.hotelpms.frontdesk.assistant.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Redis-backed assistant state with tenant/operator isolation and TTL. */
@Component
@RequiredArgsConstructor
public final class ConversationSessionStore {

    private static final Duration SESSION_TTL = Duration.ofMinutes(10);
    private static final Duration LOCK_TTL = Duration.ofSeconds(45);
    private static final String SESSION_PREFIX = "assistant:conversation:";
    private static final String LOCK_PREFIX = "assistant:lock:";
    private static final DefaultRedisScript<Long> RELEASE_LOCK = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Loads the current session or returns a new empty session.
     *
     * @param hotelId authenticated tenant
     * @param userId authenticated operator
     * @return current conversational state
     */
    public ConversationSession load(final UUID hotelId, final String userId) {
        final String json = redisTemplate.opsForValue().get(sessionKey(hotelId, userId));
        if (json == null) {
            return new ConversationSession();
        }
        try {
            return objectMapper.readValue(json, ConversationSession.class);
        } catch (final JsonProcessingException ex) {
            throw new ExternalServiceException("ASSISTANT_SESSION_INVALID", ex);
        }
    }

    /**
     * Persists the session and refreshes its inactivity TTL.
     *
     * @param hotelId authenticated tenant
     * @param userId authenticated operator
     * @param session state to persist
     */
    public void save(final UUID hotelId, final String userId, final ConversationSession session) {
        try {
            redisTemplate.opsForValue().set(
                    sessionKey(hotelId, userId), objectMapper.writeValueAsString(session), SESSION_TTL);
        } catch (final JsonProcessingException ex) {
            throw new ExternalServiceException("ASSISTANT_SESSION_SERIALIZATION_FAILED", ex);
        }
    }

    /**
     * Deletes one tenant/operator session.
     *
     * @param hotelId authenticated tenant
     * @param userId authenticated operator
     */
    public void clear(final UUID hotelId, final String userId) {
        redisTemplate.delete(sessionKey(hotelId, userId));
    }

    /**
     * Runs an operation under a short tenant/operator-scoped Redis lock.
     *
     * @param hotelId authenticated tenant
     * @param userId authenticated operator
     * @param operation protected operation
     * @param <T> operation return type
     * @return protected operation result
     */
    public <T> T withLock(final UUID hotelId, final String userId, final Supplier<T> operation) {
        final String lockKey = LOCK_PREFIX + scopedHash(hotelId, userId);
        final String token = UUID.randomUUID().toString();
        final Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new ConflictException("ASSISTANT_OPERATION_IN_PROGRESS");
        }
        try {
            return operation.get();
        } finally {
            redisTemplate.execute(RELEASE_LOCK, List.of(lockKey), token);
        }
    }

    private static String sessionKey(final UUID hotelId, final String userId) {
        return SESSION_PREFIX + scopedHash(hotelId, userId);
    }

    private static String scopedHash(final UUID hotelId, final String userId) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return hotelId + ":" + HexFormat.of().formatHex(
                    digest.digest(userId.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
