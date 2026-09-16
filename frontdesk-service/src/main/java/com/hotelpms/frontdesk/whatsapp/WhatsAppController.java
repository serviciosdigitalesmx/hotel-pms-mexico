package com.hotelpms.frontdesk.whatsapp;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Owner-only WhatsApp pairing through the existing authenticated gateway and HMAC boundary. */
@RestController
@RequestMapping("/api/v1/stays/whatsapp")
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
public class WhatsAppController {
    private final EvolutionService evolution;

    /**
     * Builds the tenant-scoped controller.
     *
     * @param evolution private Evolution adapter
     */
    public WhatsAppController(final EvolutionService evolution) {
        this.evolution = evolution;
    }

    /**
     * Returns safe WhatsApp connection status for the current hotel.
     *
     * @return uncached connection status
     */
    @GetMapping
    public ResponseEntity<EvolutionService.Connection> status() {
        return response(evolution.status(hotelId()));
    }

    /**
     * Starts pairing and returns an uncached PNG QR for the current hotel.
     *
     * @return safe pairing result
     */
    @PostMapping("/connect")
    public ResponseEntity<EvolutionService.Connection> connect() {
        return response(evolution.connect(hotelId()));
    }

    private static UUID hotelId() {
        return UUID.fromString(String.valueOf(
                SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }

    private static ResponseEntity<EvolutionService.Connection> response(
            final EvolutionService.Connection connection) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(connection);
    }
}
