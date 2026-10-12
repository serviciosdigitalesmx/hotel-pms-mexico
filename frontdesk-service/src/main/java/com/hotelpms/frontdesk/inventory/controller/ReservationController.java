package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReservationRequest;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Type domain type. */
@RestController("inventoryReservationController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/reservations")
public class ReservationController {
  private final InventoryService service;

  /**
   * Reserves stock for a service order.
   *
   * @param r reservation request
   * @return stock reservation
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
  public StockReservation reserve(@Valid @RequestBody final ReservationRequest r) {
    return service.reserve(hotel(), r);
  }

  /**
   * Releases a stock reservation.
   *
   * @param order service order identifier
   * @param product product identifier
   * @return released reservation
   */
  @PostMapping("/{order}/{product}/release")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
  public StockReservation release(@PathVariable final UUID order, @PathVariable final UUID product) {
    return service.release(hotel(), order, product);
  }

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
