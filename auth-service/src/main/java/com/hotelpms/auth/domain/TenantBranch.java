package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A branch/property under a tenant. A branch UUID is only meaningful together with its {@code
 * hotelId}; every query must scope by both.
 */
@Entity
@Table(name = "tenant_branch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TenantBranch {

  private static final int NAME_MAX_LENGTH = 120;
  private static final int PHONE_MAX_LENGTH = 40;
  private static final int EXTRA_FIELD_MAX_LENGTH = 200;
  private static final int ADDRESS_MAX_LENGTH = 500;
  private static final int TIME_ZONE_MAX_LENGTH = 80;
  private static final int COUNTRY_MAX_LENGTH = 80;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = NAME_MAX_LENGTH)
  private String name;

  @Column(length = PHONE_MAX_LENGTH)
  private String phone;

  @Column(name = "extra_field", length = EXTRA_FIELD_MAX_LENGTH)
  private String extraField;

  @Column(length = ADDRESS_MAX_LENGTH)
  private String address;

  @Column(name = "time_zone", length = TIME_ZONE_MAX_LENGTH)
  private String timeZone;

  @Column(name = "date_format", length = 16)
  private String dateFormat;

  @Column(length = COUNTRY_MAX_LENGTH)
  private String country;

  @Column(length = 3)
  private String currency;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
