package com.hotelpms.frontdesk.stays.controller;

import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal tenant-directory endpoint used only by the API Gateway.
 *
 * <p>The request still requires the normal internal HMAC authentication.
 * It does not trust a hotel UUID supplied by a public browser: the tenant
 * is resolved server-side from {@code HotelSettings.publicSlug}.</p>
 */
@RestController
@RequestMapping("/internal/public-hotels")
@RequiredArgsConstructor
public class PublicHotelResolverController {

    private static final int MAX_SLUG_LENGTH = 120;

    private final HotelSettingsRepository hotelSettingsRepository;

    /**
     * Resolves a public booking slug to its tenant UUID.
     *
     * @param slug public hotel slug
     * @return hotel UUID as plain text, or 404 when the slug is unknown
     */
    @GetMapping("/{slug}")
    public ResponseEntity<String> resolveHotel(
            @PathVariable final String slug) {

        if (slug == null
                || slug.isBlank()
                || slug.length() > MAX_SLUG_LENGTH
                || !slug.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,119}")) {
            return ResponseEntity.badRequest().build();
        }

        return hotelSettingsRepository
                .findByPublicSlug(slug)
                .map(settings ->
                        ResponseEntity.ok(
                                settings.getHotelId().toString()))
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}
