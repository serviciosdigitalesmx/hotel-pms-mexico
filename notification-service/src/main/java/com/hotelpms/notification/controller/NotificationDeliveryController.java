package com.hotelpms.notification.controller;

import com.hotelpms.notification.domain.NotificationDelivery;
import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.service.NotificationDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal event-to-notification enqueue contract. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/events")
public class NotificationDeliveryController {
  private final NotificationDeliveryService service;

  @PostMapping
  public ResponseEntity<NotificationDelivery> enqueue(@Valid @RequestBody final EventNotificationRequest request) {
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.enqueue(request));
  }
}
