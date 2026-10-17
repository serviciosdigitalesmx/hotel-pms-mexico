package com.hotelpms.frontdesk.notifications;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated API for tenant-specific operational notification settings. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/preferences")
public class OperationalNotificationPreferencesController {
  private final OperationalNotificationPreferencesService service;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public Map<String, Map<String, Boolean>> get() {
    return service.get(hotelId());
  }

  @PutMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public Map<String, Map<String, Boolean>> update(
      @RequestBody final Map<String, Map<String, Boolean>> preferences) {
    return service.update(hotelId(), preferences);
  }

  @GetMapping("/recipients")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public Map<String, List<String>> getEmailRecipients() {
    return service.getEmailRecipients(hotelId());
  }

  @PutMapping("/recipients")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public Map<String, List<String>> updateEmailRecipients(
      @jakarta.validation.Valid @RequestBody final OperationalEmailRecipientsRequest request) {
    return service.updateEmailRecipients(hotelId(), request);
  }

  private UUID hotelId() {
    return UUID.fromString(String.valueOf(
        SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
