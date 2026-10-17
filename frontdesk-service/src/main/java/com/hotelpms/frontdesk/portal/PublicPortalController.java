package com.hotelpms.frontdesk.portal;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import com.hotelpms.frontdesk.serviceorder.controller.DocumentEvidenceController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Staff token management and public, token-scoped portal access. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@SuppressWarnings("checkstyle:JavadocMethod")
public class PublicPortalController {
  // CHECKSTYLE: JavadocMethod|JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup OFF
  private final PublicPortalTokenService service;

  /** Issues an opaque token for a customer portal operation.
   * @param orderId service order identifier
   * @param ttlHours token lifetime in hours
   * @return the newly issued token
   */
  @PostMapping("/v1/portal/tokens/service-orders/{orderId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")
  public ResponseEntity<Map<String, Object>> issue(@PathVariable final UUID orderId,
      @RequestParam(defaultValue = "72") final long ttlHours) {
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token",
        service.issue(hotel(), orderId, ttlHours), "resourceId", orderId));
  }

  /** Returns the scoped operation metadata for the customer portal.
   * @param token opaque portal token
   * @return scoped operation metadata
   */
  @GetMapping("/public/portal/operations/{token}")
  public Map<String, Object> operation(@PathVariable final String token) {
    final PublicPortalToken value = service.resolve(token);
    return Map.of("resourceType", value.getResourceType(), "resourceId", value.getResourceId(),
        "customerId", value.getCustomerId(), "expiresAt", value.getExpiresAt());
  }

  /** Returns documents visible on the token-scoped customer portal.
   * @param token opaque portal token
   * @return visible document metadata
   */
  @GetMapping("/public/portal/operations/{token}/documents")
  public List<DocumentEvidenceController.EvidenceView> documents(@PathVariable final String token) {
    return service.documents(token).stream().map(document -> {
      final var view = DocumentEvidenceController.view(document);
      return new DocumentEvidenceController.EvidenceView(view.id(), view.serviceOrderId(), view.customerId(),
          view.fileName(), view.contentType(), "/api/public/portal/operations/" + token
              + "/documents/" + view.id() + "/content", view.accessLevel(), view.createdAt());
    }).toList();
  }

  @GetMapping("/public/portal/operations/{token}/documents/{id}/content")
  public ResponseEntity<byte[]> documentContent(@PathVariable final String token,
      @PathVariable final UUID id) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidence-" + id + "\"")
        .body(service.documentContent(token, id));
  }

  /** Starts a local OTP challenge; the returned code is for deterministic local validation only.
   * @param token opaque portal token
   * @return challenge delivery metadata
   */
  @PostMapping("/public/portal/operations/{token}/otp/challenge")
  public Map<String, Object> challenge(@PathVariable final String token) {
    final PublicPortalTokenService.OtpResult result = service.createOtp(token);
    return Map.of("deliveryMode", "SIMULATED", "testCode", result.localTestCode(),
        "expiresAt", result.tokenExpiresAt());
  }

  /** Verifies the one-time portal challenge.
   * @param token opaque portal token
   * @param code one-time code
   * @return verification result
   */
  @PostMapping("/public/portal/operations/{token}/otp/verify")
  public Map<String, Object> verify(@PathVariable final String token,
      @RequestParam final String code) {
    return Map.of("verified", service.verifyOtp(token, code));
  }

  /** Records customer authorization against the token-scoped order.
   * @param token opaque portal token
   * @param evidence customer authorization evidence
   * @return authorization result
   */
  @PostMapping("/public/portal/operations/{token}/authorize")
  public Map<String, Object> authorize(@PathVariable final String token,
      @RequestParam final String evidence) {
    service.authorize(token, evidence);
    return Map.of("authorized", true);
  }

  /** Revokes a previously issued token.
   * @param id token identifier
   * @return empty response
   */
  @PostMapping("/v1/portal/tokens/{id}/revoke")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  public ResponseEntity<Void> revoke(@PathVariable final UUID id) {
    service.revoke(hotel(), id);
    return ResponseEntity.noContent().build();
  }

  private UUID hotel() {
    return UUID.fromString(String.valueOf(
        SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
  // CHECKSTYLE: JavadocMethod|JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup ON
}
