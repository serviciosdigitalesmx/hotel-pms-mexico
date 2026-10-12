package com.hotelpms.frontdesk.whatsapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Normalized inbound/outbound WhatsApp message. */
@Entity
@Table(name = "whatsapp_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"checkstyle:AnnotationLocation", "checkstyle:MagicNumber"})
public class WhatsAppMessage {
  // CHECKSTYLE: AnnotationLocation|MagicNumber OFF
  private static final int PROVIDER_MESSAGE_ID_LENGTH = 180;
  private static final int DIRECTION_LENGTH = 10;
  private static final int BODY_LENGTH = 10000;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;
  @Column(name = "conversation_id", nullable = false)
  private UUID conversationId;
  @Column(name = "provider_message_id", nullable = false, length = PROVIDER_MESSAGE_ID_LENGTH)
  private String providerMessageId;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = DIRECTION_LENGTH)
  private Direction direction;
  @Column(nullable = false, length = BODY_LENGTH)
  private String body;
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Message direction. */
  public enum Direction { INBOUND, OUTBOUND }
  // CHECKSTYLE: AnnotationLocation|MagicNumber ON
}
