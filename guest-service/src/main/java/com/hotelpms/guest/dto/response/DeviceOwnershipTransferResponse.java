package com.hotelpms.guest.dto.response;

import com.hotelpms.guest.model.DeviceOwnershipTransfer;
import java.time.LocalDateTime;
import java.util.UUID;

/** Tenant-scoped, read-only ownership audit view for a customer device. */
public record DeviceOwnershipTransferResponse(
    UUID id,
    UUID fromCustomerId,
    UUID toCustomerId,
    UUID actorId,
    String evidence,
    LocalDateTime transferredAt) {
  public static DeviceOwnershipTransferResponse from(final DeviceOwnershipTransfer transfer) {
    return new DeviceOwnershipTransferResponse(transfer.getId(), transfer.getFromGuestId(),
        transfer.getToGuestId(), transfer.getActorId(), transfer.getEvidence(),
        transfer.getTransferredAt());
  }
}
