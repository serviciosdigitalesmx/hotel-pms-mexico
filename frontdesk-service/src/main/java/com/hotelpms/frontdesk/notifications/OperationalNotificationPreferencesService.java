package com.hotelpms.frontdesk.notifications;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Reads and validates per-tenant workshop notification channel preferences. */
@Service
@RequiredArgsConstructor
public class OperationalNotificationPreferencesService {
    private static final int MAX_EMAIL_RECIPIENTS_PER_EVENT = 20;
    private static final Set<String> EVENTS =
            Set.of(
                    "ORDER_ASSIGNED",
                    "STOCK_LOW",
                    "DELIVERY_DUE",
                    "PAYMENT_DUE",
                    "ORDER_READY",
                    "DAY_SUMMARY");
    private static final Set<String> CHANNELS = Set.of("EMAIL", "WEB", "APP");
    private final OperationalNotificationPreferencesRepository repository;

    /**
     * Reads notification preferences for one tenant.
     *
     * @implSpec Overrides must read only the requested tenant's preferences.
     * @param hotelId tenant identifier
     * @return event-to-channel preference map, including defaults
     */
    @Transactional
    public Map<String, Map<String, Boolean>> get(final UUID hotelId) {
        return repository
                .findById(hotelId)
                .map(OperationalNotificationPreferences::getPreferences)
                .orElseGet(OperationalNotificationPreferencesService::defaults);
    }

    /**
     * Reads configured recipient lists for one tenant.
     *
     * @implSpec Overrides must preserve tenant scoping and supported event filtering.
     * @param hotelId tenant identifier
     * @return validated recipient lists by event
     */
    @Transactional(readOnly = true)
    public Map<String, List<String>> getEmailRecipients(final UUID hotelId) {
        final Map<String, List<String>> stored =
                repository
                        .findById(hotelId)
                        .map(OperationalNotificationPreferences::getEmailRecipients)
                        .orElse(Map.of());
        final Map<String, List<String>> result = emptyRecipientDefaults();
        if (stored != null) {
            stored.forEach(
                    (event, recipients) -> {
                        if (EVENTS.contains(event) && recipients != null) {
                            result.put(event, List.copyOf(new LinkedHashSet<>(recipients)));
                        }
                    });
        }
        return result;
    }

    /**
     * Updates operational email recipients for a tenant.
     *
     * @implSpec Overrides must preserve tenant scoping, event allowlisting, and recipient limits.
     * @param hotelId tenant identifier
     * @param request recipient lists to update
     * @return persisted recipient lists by event
     */
    @Transactional
    public Map<String, List<String>> updateEmailRecipients(
            final UUID hotelId, final OperationalEmailRecipientsRequest request) {
        if (request == null
                || request.recipients() == null
                || request.recipients().keySet().stream()
                        .anyMatch(event -> !EVENTS.contains(event))) {
            throw new IllegalArgumentException("UNKNOWN_NOTIFICATION_EVENT");
        }
        final Map<String, List<String>> recipients = getEmailRecipients(hotelId);
        request.recipients()
                .forEach(
                        (event, addresses) -> {
                            if (addresses == null
                                    || addresses.size() > MAX_EMAIL_RECIPIENTS_PER_EVENT
                                    || addresses.stream()
                                            .anyMatch(
                                                    address ->
                                                            address == null || address.isBlank())) {
                                throw new IllegalArgumentException("NOTIFICATION_EMAIL_REQUIRED");
                            }
                            recipients.put(
                                    event,
                                    addresses.stream()
                                            .map(String::trim)
                                            .map(
                                                    address ->
                                                            address.toLowerCase(
                                                                    java.util.Locale.ROOT))
                                            .distinct()
                                            .toList());
                        });
        final OperationalNotificationPreferences entity =
                repository.findById(hotelId).orElseGet(OperationalNotificationPreferences::new);
        entity.setHotelId(hotelId);
        entity.setEmailRecipients(recipients);
        entity.setPreferences(
                entity.getPreferences() == null ? defaults() : entity.getPreferences());
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
        return recipients;
    }

    /**
     * Updates channel preferences for a tenant.
     *
     * @implSpec Overrides must preserve tenant scoping and event/channel allowlists.
     * @param hotelId tenant identifier
     * @param requested event/channel values to update
     * @return merged preferences after persistence
     */
    @Transactional
    public Map<String, Map<String, Boolean>> update(
            final UUID hotelId, final Map<String, Map<String, Boolean>> requested) {
        if (requested == null
                || requested.keySet().stream().anyMatch(event -> !EVENTS.contains(event))) {
            throw new IllegalArgumentException("UNKNOWN_NOTIFICATION_EVENT");
        }
        final Map<String, Map<String, Boolean>> merged = defaults();
        requested.forEach(
                (event, channels) -> {
                    if (channels == null
                            || channels.keySet().stream()
                                    .anyMatch(channel -> !CHANNELS.contains(channel))) {
                        throw new IllegalArgumentException("UNKNOWN_NOTIFICATION_CHANNEL");
                    }
                    channels.forEach(
                            (channel, enabled) -> {
                                if (enabled == null) {
                                    throw new IllegalArgumentException(
                                            "NOTIFICATION_VALUE_REQUIRED");
                                }
                                merged.get(event).put(channel, enabled);
                            });
                });
        final OperationalNotificationPreferences entity =
                repository.findById(hotelId).orElseGet(OperationalNotificationPreferences::new);
        entity.setHotelId(hotelId);
        entity.setPreferences(merged);
        if (entity.getEmailRecipients() == null) {
            entity.setEmailRecipients(emptyRecipientDefaults());
        }
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

    private static Map<String, List<String>> emptyRecipientDefaults() {
        final Map<String, List<String>> result = new LinkedHashMap<>();
        EVENTS.forEach(event -> result.put(event, List.of()));
        return result;
    }
}
