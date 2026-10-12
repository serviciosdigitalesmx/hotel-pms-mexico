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

/** Tenant-scoped WhatsApp conversation and human/AI routing state. */
@Entity
@Table(name = "whatsapp_conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"checkstyle:AnnotationLocation", "checkstyle:MagicNumber",
    "checkstyle:ExplicitInitialization"})
public class Conversation {
  // CHECKSTYLE: AnnotationLocation|MagicNumber|ExplicitInitialization OFF
  private static final int CHANNEL_KEY_LENGTH = 160;
  private static final int AI_MODE_LENGTH = 20;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;
  @Column(name = "channel_key", nullable = false, length = CHANNEL_KEY_LENGTH)
  private String channelKey;
  @Column(name = "customer_id")
  private UUID customerId;
  @Column(name = "device_id")
  private UUID deviceId;
  @Column(name = "service_order_id")
  private UUID serviceOrderId;
  @Column(name = "quotation_id")
  private UUID quotationId;
  @Column(name = "payment_id")
  private UUID paymentId;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = AI_MODE_LENGTH)
  @Builder.Default
  private AiMode aiMode = AiMode.HUMAN_ONLY;
  @Column(nullable = false)
  private boolean handoff;
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /** Supported routing modes. */
  public enum AiMode { HUMAN_ONLY, AI_ASSISTED, AI_AUTOMATIC }
  // CHECKSTYLE: AnnotationLocation|MagicNumber|ExplicitInitialization ON
}
