package com.hotelpms.frontdesk.notifications;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads and validates per-tenant workshop notification channel preferences. */
@Service
@RequiredArgsConstructor
public class OperationalNotificationPreferencesService {
  private static final Set<String> EVENTS = Set.of(
      "ORDER_ASSIGNED", "STOCK_LOW", "DELIVERY_DUE", "PAYMENT_DUE", "ORDER_READY", "DAY_SUMMARY");
  private static final Set<String> CHANNELS = Set.of("EMAIL", "WEB", "APP");
  private final OperationalNotificationPreferencesRepository repository;

  @Transactional
  public Map<String, Map<String, Boolean>> get(final UUID hotelId) {
    return repository.findById(hotelId).map(OperationalNotificationPreferences::getPreferences)
        .orElseGet(OperationalNotificationPreferencesService::defaults);
  }

  @Transactional
  public Map<String, Map<String, Boolean>> update(
      final UUID hotelId, final Map<String, Map<String, Boolean>> requested) {
    if (requested == null || requested.keySet().stream().anyMatch(event -> !EVENTS.contains(event))) {
      throw new IllegalArgumentException("UNKNOWN_NOTIFICATION_EVENT");
    }
    final Map<String, Map<String, Boolean>> merged = defaults();
    requested.forEach((event, channels) -> {
      if (channels == null || channels.keySet().stream().anyMatch(channel -> !CHANNELS.contains(channel))) {
        throw new IllegalArgumentException("UNKNOWN_NOTIFICATION_CHANNEL");
      }
      channels.forEach((channel, enabled) -> {
        if (enabled == null) throw new IllegalArgumentException("NOTIFICATION_VALUE_REQUIRED");
        merged.get(event).put(channel, enabled);
      });
    });
    final OperationalNotificationPreferences entity = repository.findById(hotelId)
        .orElseGet(OperationalNotificationPreferences::new);
    entity.setHotelId(hotelId);
    entity.setPreferences(merged);
    entity.setUpdatedAt(LocalDateTime.now());
    return repository.save(entity).getPreferences();
  }

  private static Map<String, Map<String, Boolean>> defaults() {
    final Map<String, Map<String, Boolean>> result = new LinkedHashMap<>();
    for (final String event : EVENTS) {
      final Map<String, Boolean> channels = new LinkedHashMap<>();
      channels.put("EMAIL", true);
      channels.put("WEB", true);
      channels.put("APP", false);
      result.put(event, channels);
    }
    return result;
  }
}
