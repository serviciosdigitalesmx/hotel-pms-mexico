package com.hotelpms.guest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** A persistent customer-owned asset. It is deliberately not embedded in an order. */
@Entity
@Table(name = "devices", indexes = {
  @Index(name = "idx_devices_hotel_customer", columnList = "hotel_id,guest_id")
})
@SQLDelete(sql = "UPDATE devices SET active = false WHERE id = ?")
@SQLRestriction("active = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
  /** Maximum category length. */
  public static final int CATEGORY_MAX_LENGTH = 80;
  /** Maximum length for manufacturer, model and identity values. */
  public static final int IDENTITY_MAX_LENGTH = 150;
  /** Maximum custom-field payload length. */
  public static final int CUSTOM_FIELDS_MAX_LENGTH = 2000;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "guest_id", nullable = false)
  private UUID guestId;

  @Column(nullable = false, length = CATEGORY_MAX_LENGTH)
  private String category;

  @Column(length = IDENTITY_MAX_LENGTH)
  private String manufacturer;

  @Column(length = IDENTITY_MAX_LENGTH)
  private String model;

  @Column(length = IDENTITY_MAX_LENGTH)
  private String serialNumber;

  @Column(length = IDENTITY_MAX_LENGTH)
  private String imei;

  @Column(length = CUSTOM_FIELDS_MAX_LENGTH)
  private String customFields;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
