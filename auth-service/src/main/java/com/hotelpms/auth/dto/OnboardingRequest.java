package com.hotelpms.auth.dto;
import jakarta.validation.constraints.Size;
public record OnboardingRequest(@Size(max=40) String contactPhone, @Size(max=500) String businessHours) {}
