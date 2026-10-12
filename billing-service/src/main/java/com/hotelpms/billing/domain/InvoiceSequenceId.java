package com.hotelpms.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Composite primary key for {@link InvoiceSequence}: (hotel_id, year). */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSequenceId implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "year", nullable = false)
  private int year;
}
