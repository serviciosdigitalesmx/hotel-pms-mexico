package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param branchId value.
 *
 * @param customerId value.
 *
 * @param deviceId value.
 *
 * @param summary value.
 */
public record ServiceOrderRequest(
    @NotNull UUID customerId, UUID deviceId, UUID branchId, @NotBlank String summary,
    @NotBlank String reportedFailure, @NotBlank String intakeChecklist,
    @FutureOrPresent LocalDateTime promisedAt, String intakeEvidence,
    @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority,
    @Size(max = 200) String serviceName,
    @DecimalMin("0.00") BigDecimal depositAmount,
    UUID assignedUserId) {
  public ServiceOrderRequest(UUID customerId, UUID deviceId, UUID branchId, String summary,
      String reportedFailure, String intakeChecklist, LocalDateTime promisedAt, String intakeEvidence) {
    this(customerId, deviceId, branchId, summary, reportedFailure, intakeChecklist,
        promisedAt, intakeEvidence, "NORMAL", null, BigDecimal.ZERO, null);
  }
}
