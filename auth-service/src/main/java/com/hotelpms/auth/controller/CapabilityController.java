package com.hotelpms.auth.controller;

import com.hotelpms.auth.service.CapabilityService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/capabilities")
@RequiredArgsConstructor
public class CapabilityController {
    private final CapabilityService service;

    @GetMapping("/{capabilityKey}")
    public ResponseEntity<Map<String, Object>> evaluate(
            @RequestHeader("X-Auth-Hotel") final UUID tenantId,
            @PathVariable final String capabilityKey,
            final Authentication authentication) {
        final boolean enabled = service.isEnabled(authentication.getName(), tenantId, capabilityKey);
        return ResponseEntity.ok(Map.of("tenantId", tenantId, "capability", capabilityKey, "enabled", enabled));
    }
}
