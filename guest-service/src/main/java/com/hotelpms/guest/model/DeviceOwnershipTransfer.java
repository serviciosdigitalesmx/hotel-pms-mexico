package com.hotelpms.guest.model;

import com.hotelpms.guest.util.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable audit record for a customer asset ownership transfer. */
@Entity
@Table(name = "device_ownership_transfers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOwnershipTransfer {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "device_id", nullable = false)
  private UUID deviceId;

  @Column(name = "from_guest_id")
  private UUID fromGuestId;

  @Column(name = "to_guest_id", nullable = false)
  private UUID toGuestId;

  @Column(name = "actor_id")
  private UUID actorId;

  @Column(length = ValidationConstants.MAX_TRANSFER_EVIDENCE_LENGTH)
  private String evidence;

  @Column(nullable = false, updatable = false)
  private LocalDateTime transferredAt;
}
