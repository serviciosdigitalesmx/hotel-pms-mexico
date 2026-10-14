package com.hotelpms.frontdesk.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Tenant-owned delivery-channel preferences for workshop notifications. */
@Entity
@Table(name = "operational_notification_preferences")
@Getter
@Setter
@NoArgsConstructor
public class OperationalNotificationPreferences {
  @Id
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "preferences", nullable = false, columnDefinition = "jsonb")
  private Map<String, Map<String, Boolean>> preferences;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
