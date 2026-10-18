package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Validated request for creating a tenant-scoped Fixi repair order.
 *
 * @param customerId tenant-owned customer identifier
 * @param deviceId customer device identifier
 * @param branchId branch receiving the repair order
 * @param summary brief repair summary
 * @param reportedFailure customer-reported symptom
 * @param intakeChecklist recorded device intake condition
 * @param promisedAt expected completion date
 * @param intakeEvidence optional intake evidence reference
 * @param priority operational priority
 * @param serviceName optional service catalog label
 * @param depositAmount initial payment amount
 * @param assignedUserId optional technician identifier
 * @param depositPaymentMethod payment method for the initial deposit
 */
public record ServiceOrderRequest(
        @NotNull UUID customerId,
        UUID deviceId,
        UUID branchId,
        @NotBlank String summary,
        @NotBlank String reportedFailure,
        @NotBlank String intakeChecklist,
        @FutureOrPresent LocalDateTime promisedAt,
        String intakeEvidence,
        @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority,
        @Size(max = SERVICE_NAME_LENGTH) String serviceName,
        @DecimalMin("0.00") @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
                BigDecimal depositAmount,
        UUID assignedUserId,
        @Pattern(regexp = "CASH|CHECK|BANK_DEPOSIT|PAYPAL|CARD|OTHER")
                String depositPaymentMethod) {
    private static final int SERVICE_NAME_LENGTH = 200;

    /**
     * Creates a request with no explicit deposit payment method.
     */
    public ServiceOrderRequest(
            final UUID customerId,
            final UUID deviceId,
            final UUID branchId,
            final String summary,
            final String reportedFailure,
            final String intakeChecklist,
            final LocalDateTime promisedAt,
            final String intakeEvidence,
            final String priority,
            final String serviceName,
            final BigDecimal depositAmount,
            final UUID assignedUserId) {
        this(
                customerId,
                deviceId,
                branchId,
                summary,
                reportedFailure,
                intakeChecklist,
                promisedAt,
                intakeEvidence,
                priority,
                serviceName,
                depositAmount,
                assignedUserId,
                null);
    }

    /**
     * Creates a basic intake request with normal priority and no deposit.
     */
    public ServiceOrderRequest(
            final UUID customerId,
            final UUID deviceId,
            final UUID branchId,
            final String summary,
            final String reportedFailure,
            final String intakeChecklist,
            final LocalDateTime promisedAt,
            final String intakeEvidence) {
        this(
                customerId,
                deviceId,
                branchId,
                summary,
                reportedFailure,
                intakeChecklist,
                promisedAt,
                intakeEvidence,
                "NORMAL",
                null,
                BigDecimal.ZERO,
                null,
                null);
    }
}
