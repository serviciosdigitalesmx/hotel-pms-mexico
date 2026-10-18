package com.hotelpms.frontdesk.notifications;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Authenticated API for tenant-specific operational notification settings. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/preferences")
public class OperationalNotificationPreferencesController {
    private static final String MANAGER_ROLES = "hasAnyRole('ADMIN', 'OWNER', 'MANAGER')";
    private final OperationalNotificationPreferencesService service;

    /**
     * Reads the authenticated tenant's notification preferences.
     *
     * @implSpec Overrides must continue deriving tenant identity from the authenticated context.
     * @return event-to-channel preferences
     */
    @GetMapping
    @PreAuthorize(MANAGER_ROLES)
    public Map<String, Map<String, Boolean>> get() {
        return service.get(hotelId());
    }

    /**
     * Replaces notification preferences for the authenticated tenant.
     *
     * @implSpec Overrides must continue deriving tenant identity from the authenticated context.
     * @param preferences requested event-to-channel preferences
     * @return persisted event-to-channel preferences
     */
    @PutMapping
    @PreAuthorize(MANAGER_ROLES)
    public Map<String, Map<String, Boolean>> update(
            @RequestBody final Map<String, Map<String, Boolean>> preferences) {
        return service.update(hotelId(), preferences);
    }

    /**
     * Reads configured operational email recipients for the authenticated tenant.
     *
     * @implSpec Overrides must continue deriving tenant identity from the authenticated context.
     * @return recipients grouped by operational event
     */
    @GetMapping("/recipients")
    @PreAuthorize(MANAGER_ROLES)
    public Map<String, List<String>> getEmailRecipients() {
        return service.getEmailRecipients(hotelId());
    }

    /**
     * Updates operational email recipients for the authenticated tenant.
     *
     * @implSpec Overrides must continue deriving tenant identity from the authenticated context.
     * @param request recipient lists to persist
     * @return persisted recipients grouped by event
     */
    @PutMapping("/recipients")
    @PreAuthorize(MANAGER_ROLES)
    public Map<String, List<String>> updateEmailRecipients(
            @jakarta.validation.Valid @RequestBody
                    final OperationalEmailRecipientsRequest request) {
        return service.updateEmailRecipients(hotelId(), request);
    }

    private UUID hotelId() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
