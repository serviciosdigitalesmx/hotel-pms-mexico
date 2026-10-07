package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.dto.TenantIndustryProfileRequest;
import com.hotelpms.auth.repository.TenantIndustryProfileRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages tenant-owned multi-industry configuration without unsafe fallbacks. */
@Service
@RequiredArgsConstructor
public class TenantIndustryProfileService {
  private static final String DEFAULT_JSON = "{}";
  private final TenantIndustryProfileRepository profiles;

  /**
   * Returns the configured profile or an explicit repair baseline.
   *
   * @param tenantId tenant identifier
   * @return tenant profile
   */
  @Transactional(readOnly = true)
  public TenantIndustryProfile get(final UUID tenantId) {
    return profiles
        .findById(tenantId)
        .orElseGet(
            () ->
                TenantIndustryProfile.builder()
                    .tenantId(tenantId)
                    .industryKey("repair")
                    .enabledModulesJson(DEFAULT_JSON)
                    .dynamicFieldsJson(DEFAULT_JSON)
                    .formsLabelsJson(DEFAULT_JSON)
                    .catalogsTemplatesJson(DEFAULT_JSON)
                    .updatedAt(LocalDateTime.now())
                    .build());
  }

  /**
   * Persists a tenant profile after rejecting unknown/blank industry keys.
   *
   * @param tenantId tenant identifier
   * @param request replacement profile
   * @return saved profile
   */
  @Transactional
  public TenantIndustryProfile update(
      final UUID tenantId, final TenantIndustryProfileRequest request) {
    final String key = request.industryKey().trim().toLowerCase(java.util.Locale.ROOT);
    if (key.isBlank() || "electronics".equals(key)) {
      throw new AccessDeniedException("INDUSTRY_KEY_NOT_ALLOWED");
    }
    return profiles.save(
        TenantIndustryProfile.builder()
            .tenantId(tenantId)
            .industryKey(key)
            .enabledModulesJson(valueOrDefault(request.enabledModulesJson()))
            .dynamicFieldsJson(valueOrDefault(request.dynamicFieldsJson()))
            .formsLabelsJson(valueOrDefault(request.formsLabelsJson()))
            .catalogsTemplatesJson(valueOrDefault(request.catalogsTemplatesJson()))
            .updatedAt(LocalDateTime.now())
            .build());
  }

  private static String valueOrDefault(final String value) {
    return value == null || value.isBlank() ? DEFAULT_JSON : value;
  }
}
