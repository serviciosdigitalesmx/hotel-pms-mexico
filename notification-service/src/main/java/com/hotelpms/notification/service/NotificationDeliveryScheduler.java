package com.hotelpms.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Polls the durable notification outbox and hands events to configured email providers. */
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("checkstyle:DesignForExtension")
public class NotificationDeliveryScheduler {
  private final NotificationDeliveryService deliveryService;

  /** Dispatches eligible tenant notifications on each configured poll interval. */
  @Scheduled(fixedDelayString = "${notification.delivery.poll-interval-ms:30000}")
  public void dispatchPending() {
    final int delivered = deliveryService.deliverPending();
    if (delivered > 0) {
      log.info("Delivered {} queued notification(s)", delivered);
    }
  }
}
