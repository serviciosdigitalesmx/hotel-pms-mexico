package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tenant-owned multi-industry configuration contract. */
@Entity
@Table(name = "tenant_industry_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantIndustryProfile {
  private static final int INDUSTRY_KEY_LENGTH = 80;
  private static final String TEXT_COLUMN_DEFINITION = "TEXT";

  @Id private UUID tenantId;

  @Column(nullable = false, length = INDUSTRY_KEY_LENGTH)
  private String industryKey;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String enabledModulesJson;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String dynamicFieldsJson;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String formsLabelsJson;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String catalogsTemplatesJson;

  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
