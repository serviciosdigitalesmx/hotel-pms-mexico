package com.hotelpms.guest.dto.response;

import com.hotelpms.guest.model.DeviceOwnershipTransfer;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant-scoped, read-only ownership audit view for a customer device.
 *
 * @param id transfer identifier
 * @param fromCustomerId previous customer identifier
 * @param toCustomerId new customer identifier
 * @param actorId user that recorded the transfer
 * @param evidence transfer evidence
 * @param transferredAt transfer timestamp
 */
public record DeviceOwnershipTransferResponse(
    UUID id,
    UUID fromCustomerId,
    UUID toCustomerId,
    UUID actorId,
    String evidence,
    LocalDateTime transferredAt) {
  /**
   * Maps a persisted ownership transfer into its API response.
   *
   * @param transfer persisted transfer record
   * @return API response
   */
  public static DeviceOwnershipTransferResponse from(final DeviceOwnershipTransfer transfer) {
    return new DeviceOwnershipTransferResponse(transfer.getId(), transfer.getFromGuestId(),
        transfer.getToGuestId(), transfer.getActorId(), transfer.getEvidence(),
        transfer.getTransferredAt());
  }
}
