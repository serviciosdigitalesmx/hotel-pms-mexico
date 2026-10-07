package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Complete replacement payload for tenant industry configuration.
 *
 * @param industryKey industry identifier
 * @param enabledModulesJson enabled modules
 * @param dynamicFieldsJson dynamic fields
 * @param formsLabelsJson configurable labels
 * @param catalogsTemplatesJson catalogs and templates
 */
public record TenantIndustryProfileRequest(
    @NotBlank String industryKey,
    String enabledModulesJson,
    String dynamicFieldsJson,
    String formsLabelsJson,
    String catalogsTemplatesJson) { }
