package com.hotelpms.frontdesk.whatsapp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Type domain type. */
@RestController
@RequestMapping("/internal/whatsapp/webhook")
@SuppressWarnings("checkstyle:JavadocMethod")
public class WhatsAppWebhookController {
  private final WebhookEventRepository events;
  private final WhatsAppConversationService conversations;
  private final String secret;
  /**
   * Executes the operation.
   *
   * @param events parameter.
   *
   * @param conversations normalized conversation service.
   *
   * @param secret parameter.
   *
   */

  public WhatsAppWebhookController(
      final WebhookEventRepository events, final WhatsAppConversationService conversations,
      @Value("${evolution.webhook-secret:}") final String secret) {
    this.events = events;
    this.conversations = conversations;
    this.secret = secret;
  }
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param eventId parameter.
   *
   * @param signature parameter.
   *
   * @param body parameter.
   *
   * @return operation result.
   */

  @PostMapping("/{hotelId}")
  public ResponseEntity<Void> receive(
      @PathVariable final UUID hotelId,
      @RequestHeader("X-Webhook-Id") final String eventId,
      @RequestHeader("X-Webhook-Signature") final String signature,
      @RequestBody final String body) {
    if (secret.isBlank() || !valid(signature, body)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (events.existsByHotelIdAndEventId(hotelId, eventId)) {
      return ResponseEntity.ok().build();
    }
    events.save(new WebhookEvent(null, hotelId, eventId, LocalDateTime.now()));
    conversations.ingest(hotelId, eventId, body);
    return ResponseEntity.accepted().build();
  }

  private boolean valid(final String provided, final String body) {
    try {
      final Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      final String expected =
          Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
      return MessageDigest.isEqual(
          expected.getBytes(StandardCharsets.US_ASCII),
          provided.getBytes(StandardCharsets.US_ASCII));
    } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
      return false;
    }
  }
}
