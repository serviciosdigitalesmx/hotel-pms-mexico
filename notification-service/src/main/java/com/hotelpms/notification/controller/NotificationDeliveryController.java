package com.hotelpms.notification.controller;

import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.dto.EventNotificationResponse;
import com.hotelpms.notification.service.NotificationDeliveryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal event-to-notification enqueue contract. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/notifications/events")
@SuppressWarnings("checkstyle:DesignForExtension")
public class NotificationDeliveryController {
  private final NotificationDeliveryService service;

  /**
   * Persists an authenticated tenant event for asynchronous delivery.
   *
   * @param request validated event envelope
   * @param authenticatedHotelId tenant identity from the internal-authenticated header
   * @return accepted response with the persisted delivery identity
   */
  @PostMapping
  public ResponseEntity<EventNotificationResponse> enqueue(
      @Valid @RequestBody final EventNotificationRequest request,
      @RequestHeader("X-Auth-Hotel") final UUID authenticatedHotelId) {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(EventNotificationResponse.from(service.enqueue(request, authenticatedHotelId)));
  }
}
