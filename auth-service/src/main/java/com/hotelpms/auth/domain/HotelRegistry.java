package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Platform tenant registry, separate from each hotel's operational settings. */
@Entity
@Table(name = "hotel_registry")
@Getter
@Setter
public class HotelRegistry {
  private static final int NAME_MAX_LENGTH = 160;
  private static final int SLUG_MAX_LENGTH = 80;

  @Id private UUID id;

  @Column(nullable = false, length = NAME_MAX_LENGTH)
  private String name;

  @Column(nullable = false, unique = true, length = SLUG_MAX_LENGTH)
  private String slug;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false, length = 32)
  private String status = "ACTIVE";

  @Column(nullable = false)
  private boolean onboardingCompleted;

  @Column(length = 40)
  private String contactPhone;

  @Column(length = 500)
  private String businessHours;
}
