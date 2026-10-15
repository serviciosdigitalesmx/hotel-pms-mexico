package com.hotelpms.frontdesk.portal;

import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.service.ServiceOrderService;
import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.service.DocumentEvidenceService;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Issues and resolves opaque, single-resource customer portal tokens. */
@Service
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:JavadocMethod", "checkstyle:JavadocType",
    "checkstyle:MagicNumber", "checkstyle:InnerTypeLast"})
public class PublicPortalTokenService {
  // CHECKSTYLE: JavadocMethod|JavadocType|MagicNumber|InnerTypeLast OFF
  private final PublicPortalTokenRepository tokens;
  private final ServiceOrderRepository orders;
  private final ServiceOrderService orderService;
  private final PortalOtpChallengeRepository challenges;
  private final DocumentEvidenceService evidenceService;
  private final SecureRandom random = new SecureRandom();

  /** Issues a token for a tenant-owned service order. */
  @Transactional
  public String issue(final UUID hotelId, final UUID orderId, final long ttlHours) {
    final ServiceOrder order = orders.findByIdAndHotelId(orderId, hotelId)
        .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
    final String raw = randomToken();
    tokens.save(PublicPortalToken.builder().hotelId(hotelId)
        .resourceType(PublicPortalToken.ResourceType.SERVICE_ORDER).resourceId(orderId)
        .customerId(order.getCustomerId()).tokenHash(hash(raw))
        .expiresAt(LocalDateTime.now().plusHours(Math.max(1, Math.min(ttlHours, 720))))
        .createdAt(LocalDateTime.now()).build());
    return raw;
  }

  /** Resolves a token without exposing tenant-owned records across tenants. */
  @Transactional(readOnly = true)
  public PublicPortalToken resolve(final String raw) {
    final PublicPortalToken token = tokens.findByTokenHashAndRevokedFalse(hash(raw))
        .orElseThrow(() -> new NotFoundException("PORTAL_TOKEN_INVALID"));
    if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new NotFoundException("PORTAL_TOKEN_EXPIRED");
    }
    return token;
  }

  /** Revokes a token within its tenant. */
  @Transactional
  public void revoke(final UUID hotelId, final UUID id) {
    final PublicPortalToken token = tokens.findById(id)
        .filter(value -> hotelId.equals(value.getHotelId()))
        .orElseThrow(() -> new NotFoundException("PORTAL_TOKEN_NOT_FOUND"));
    token.setRevoked(true);
    tokens.save(token);
  }

  /** Creates a short-lived local OTP challenge for a valid portal token. */
  @Transactional
  public OtpResult createOtp(final String rawToken) {
    final PublicPortalToken token = resolve(rawToken);
    final String code = String.format("%06d", random.nextInt(1_000_000));
    challenges.save(PortalOtpChallenge.builder().tokenId(token.getId()).codeHash(hash(code))
        .expiresAt(LocalDateTime.now().plusMinutes(10)).attempts(0).consumed(false)
        .createdAt(LocalDateTime.now()).build());
    return new OtpResult(code, token.getExpiresAt());
  }

  /** Verifies and consumes the latest challenge, with a five-attempt ceiling. */
  @Transactional
  public boolean verifyOtp(final String rawToken, final String code) {
    final PublicPortalToken token = resolve(rawToken);
    final PortalOtpChallenge challenge = challenges
        .findTopByTokenIdAndConsumedFalseOrderByCreatedAtDesc(token.getId())
        .orElseThrow(() -> new NotFoundException("PORTAL_OTP_NOT_FOUND"));
    if (challenge.getExpiresAt().isBefore(LocalDateTime.now()) || challenge.getAttempts() >= 5) {
      throw new NotFoundException("PORTAL_OTP_EXPIRED");
    }
    challenge.setAttempts(challenge.getAttempts() + 1);
    final boolean valid = MessageDigest.isEqual(hash(code).getBytes(StandardCharsets.UTF_8),
        challenge.getCodeHash().getBytes(StandardCharsets.UTF_8));
    if (valid) {
      challenge.setConsumed(true);
      token.setVerifiedAt(LocalDateTime.now());
      tokens.save(token);
    }
    challenges.save(challenge);
    return valid;
  }

  /** Authorizes the scoped service order after the customer has verified its OTP. */
  @Transactional
  public void authorize(final String rawToken, final String evidence) {
    final PublicPortalToken token = resolve(rawToken);
    if (token.getVerifiedAt() == null) {
      throw new IllegalStateException("PORTAL_OTP_REQUIRED");
    }
    orderService.authorize(token.getHotelId(), token.getResourceId(), token.getCustomerId(),
        new AuthorizationRequest(evidence));
  }

  /** Lists only documents belonging to the resource represented by the token. */
  public List<DocumentEvidence> documents(final String rawToken) {
    final PublicPortalToken token = resolve(rawToken);
    return evidenceService.list(token.getHotelId(), token.getResourceId()).stream()
        .filter(document -> "CUSTOMER".equalsIgnoreCase(document.getAccessLevel())).toList();
  }

  /** Downloads only customer-visible evidence linked to the token's service order. */
  public byte[] documentContent(final String rawToken, final UUID documentId) {
    final PublicPortalToken token = resolve(rawToken);
    final boolean visible = evidenceService.list(token.getHotelId(), token.getResourceId()).stream()
        .anyMatch(document -> document.getId().equals(documentId)
            && "CUSTOMER".equalsIgnoreCase(document.getAccessLevel()));
    if (!visible) throw new com.hotelpms.frontdesk.exception.NotFoundException("DOCUMENT_NOT_FOUND");
    return evidenceService.content(token.getHotelId(), documentId);
  }

  /** Local delivery result; production adapters can send the code without changing the contract. */
  public record OtpResult(String localTestCode, LocalDateTime tokenExpiresAt) { }

  private String randomToken() {
    final byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hash(final String raw) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (final NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256_UNAVAILABLE", ex);
    }
  }
  // CHECKSTYLE: JavadocMethod|JavadocType|MagicNumber|InnerTypeLast ON
}
