package com.hotelpms.frontdesk.portal;

import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.service.DocumentEvidenceService;
import com.hotelpms.frontdesk.serviceorder.service.ServiceOrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Issues and resolves opaque, single-resource Fixi customer portal tokens. */
@Service
@RequiredArgsConstructor
public class PublicPortalTokenService {
    private static final int MAX_TOKEN_TTL_HOURS = 720;
    private static final int OTP_CODE_DIGITS = 6;
    private static final int OTP_CODE_RANGE = 1_000_000;
    private static final int OTP_TTL_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int TOKEN_BYTES = 32;

    private final PublicPortalTokenRepository tokens;
    private final ServiceOrderRepository orders;
    private final ServiceOrderService orderService;
    private final PortalOtpChallengeRepository challenges;
    private final DocumentEvidenceService evidenceService;
    private final SecureRandom random = new SecureRandom();

    /**
     * Issues an opaque token for a tenant-owned service order.
     *
     * @param hotelId tenant identifier
     * @param orderId tenant-owned service order identifier
     * @param ttlHours requested lifetime, bounded to a safe maximum
     * @return unpersisted token secret; only its hash is stored
     * @implSpec Overrides must preserve tenant-scoped order lookup and token hashing.
     */
    @Transactional
    public String issue(final UUID hotelId, final UUID orderId, final long ttlHours) {
        final ServiceOrder order =
                orders.findByIdAndHotelId(orderId, hotelId)
                        .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
        final String raw = randomToken();
        tokens.save(
                PublicPortalToken.builder()
                        .hotelId(hotelId)
                        .resourceType(PublicPortalToken.ResourceType.SERVICE_ORDER)
                        .resourceId(orderId)
                        .customerId(order.getCustomerId())
                        .tokenHash(hash(raw))
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusHours(Math.max(1, Math.min(ttlHours, MAX_TOKEN_TTL_HOURS))))
                        .createdAt(LocalDateTime.now())
                        .build());
        return raw;
    }

    /**
     * Resolves a valid, non-revoked, unexpired portal token.
     *
     * @param raw presented token secret
     * @return the token's tenant and resource scope
     * @implSpec Overrides must compare only the stored token hash and enforce expiry/revocation.
     */
    @Transactional(readOnly = true)
    public PublicPortalToken resolve(final String raw) {
        final PublicPortalToken token =
                tokens.findByTokenHashAndRevokedFalse(hash(raw))
                        .orElseThrow(() -> new NotFoundException("PORTAL_TOKEN_INVALID"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new NotFoundException("PORTAL_TOKEN_EXPIRED");
        }
        return token;
    }

    /**
     * Revokes one token owned by the specified tenant.
     *
     * @param hotelId tenant identifier
     * @param id token identifier
     * @implSpec Overrides must not revoke tokens owned by another tenant.
     */
    @Transactional
    public void revoke(final UUID hotelId, final UUID id) {
        final PublicPortalToken token =
                tokens.findById(id)
                        .filter(value -> hotelId.equals(value.getHotelId()))
                        .orElseThrow(() -> new NotFoundException("PORTAL_TOKEN_NOT_FOUND"));
        token.setRevoked(true);
        tokens.save(token);
    }

    /**
     * Creates a short-lived OTP challenge for a valid portal token.
     *
     * @param rawToken presented token secret
     * @return challenge code and token expiry metadata
     * @implSpec Overrides must persist only a hash of the challenge code.
     */
    @Transactional
    public OtpResult createOtp(final String rawToken) {
        final PublicPortalToken token = resolve(rawToken);
        final String code =
                String.format("%0" + OTP_CODE_DIGITS + "d", random.nextInt(OTP_CODE_RANGE));
        challenges.save(
                PortalOtpChallenge.builder()
                        .tokenId(token.getId())
                        .codeHash(hash(code))
                        .expiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES))
                        .attempts(0)
                        .consumed(false)
                        .createdAt(LocalDateTime.now())
                        .build());
        return new OtpResult(code, token.getExpiresAt());
    }

    /**
     * Verifies and consumes the latest challenge, enforcing its expiry and attempt ceiling.
     *
     * @param rawToken presented token secret
     * @param code submitted one-time code
     * @return {@code true} when the code matched and was consumed
     * @implSpec Overrides must retain constant-time comparison and the maximum-attempt limit.
     */
    @Transactional
    public boolean verifyOtp(final String rawToken, final String code) {
        final PublicPortalToken token = resolve(rawToken);
        final PortalOtpChallenge challenge =
                challenges
                        .findTopByTokenIdAndConsumedFalseOrderByCreatedAtDesc(token.getId())
                        .orElseThrow(() -> new NotFoundException("PORTAL_OTP_NOT_FOUND"));
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())
                || challenge.getAttempts() >= MAX_OTP_ATTEMPTS) {
            throw new NotFoundException("PORTAL_OTP_EXPIRED");
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        final boolean valid =
                MessageDigest.isEqual(
                        hash(code).getBytes(StandardCharsets.UTF_8),
                        challenge.getCodeHash().getBytes(StandardCharsets.UTF_8));
        if (valid) {
            challenge.setConsumed(true);
            token.setVerifiedAt(LocalDateTime.now());
            tokens.save(token);
        }
        challenges.save(challenge);
        return valid;
    }

    /**
     * Authorizes the token-scoped service order after customer OTP verification.
     *
     * @param rawToken presented token secret
     * @param evidence customer authorization evidence
     * @implSpec Overrides must require verified OTP and preserve the token's tenant/resource scope.
     */
    @Transactional
    public void authorize(final String rawToken, final String evidence) {
        final PublicPortalToken token = resolve(rawToken);
        if (token.getVerifiedAt() == null) {
            throw new IllegalStateException("PORTAL_OTP_REQUIRED");
        }
        orderService.authorize(
                token.getHotelId(),
                token.getResourceId(),
                token.getCustomerId(),
                new AuthorizationRequest(evidence));
    }

    /**
     * Lists customer-visible documents attached to the token's service order.
     *
     * @param rawToken presented token secret
     * @return visible evidence for the scoped service order
     * @implSpec Overrides must preserve token scope and customer visibility filtering.
     */
    public List<DocumentEvidence> documents(final String rawToken) {
        final PublicPortalToken token = resolve(rawToken);
        return evidenceService.list(token.getHotelId(), token.getResourceId()).stream()
                .filter(document -> "CUSTOMER".equalsIgnoreCase(document.getAccessLevel()))
                .toList();
    }

    /**
     * Downloads customer-visible evidence attached to the token's service order.
     *
     * @param rawToken presented token secret
     * @param documentId evidence identifier
     * @return evidence content bytes
     * @implSpec Overrides must verify token scope and customer visibility before reading content.
     */
    public byte[] documentContent(final String rawToken, final UUID documentId) {
        final PublicPortalToken token = resolve(rawToken);
        final boolean visible =
                evidenceService.list(token.getHotelId(), token.getResourceId()).stream()
                        .anyMatch(
                                document ->
                                        document.getId().equals(documentId)
                                                && "CUSTOMER"
                                                        .equalsIgnoreCase(
                                                                document.getAccessLevel()));
        if (!visible) {
            throw new NotFoundException("DOCUMENT_NOT_FOUND");
        }
        return evidenceService.content(token.getHotelId(), documentId);
    }

    private String randomToken() {
        final byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(final String raw) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256_UNAVAILABLE", ex);
        }
    }

    /**
     * OTP delivery metadata returned to a caller or delivery adapter.
     *
     * @param localTestCode challenge code returned by the configured local adapter
     * @param tokenExpiresAt expiry of the parent portal token
     */
    public record OtpResult(String localTestCode, LocalDateTime tokenExpiresAt) { }
}
