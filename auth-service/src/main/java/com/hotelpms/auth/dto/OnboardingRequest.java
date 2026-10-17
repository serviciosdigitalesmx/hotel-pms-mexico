package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional contact and schedule details used to activate a tenant.
 *
 * @param contactPhone tenant contact number
 * @param businessHours serialized business hours
 */
public record OnboardingRequest(
    @Size(max = MAX_CONTACT_PHONE_LENGTH) String contactPhone,
    @Size(max = MAX_BUSINESS_HOURS_LENGTH) String businessHours) {
  private static final int MAX_CONTACT_PHONE_LENGTH = 40;
  private static final int MAX_BUSINESS_HOURS_LENGTH = 500;
}
