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

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = NAME_MAX_LENGTH)
  private String name;

  @Column(length = 40)
  private String phone;

  @Column(name = "extra_field", length = 200)
  private String extraField;

  @Column(length = 500)
  private String address;

  @Column(name = "time_zone", length = 80)
  private String timeZone;

  @Column(name = "date_format", length = 16)
  private String dateFormat;

  @Column(length = 80)
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
