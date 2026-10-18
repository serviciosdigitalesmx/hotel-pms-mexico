package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.dto.DocumentEvidenceRequest;
import com.hotelpms.frontdesk.serviceorder.repository.DocumentEvidenceRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Stores and retrieves tenant-scoped evidence attached to Fixi repair orders. */
@Service
@RequiredArgsConstructor
public class DocumentEvidenceService {
    private static final int MAX_STORAGE_REFERENCE_LENGTH = 14_000_000;
    private static final int DATA_URL_PREFIX_LENGTH = 5;
    private static final String INVALID_DATA_URL = "DOCUMENT_DATA_URL_INVALID";

    private final DocumentEvidenceRepository repository;
    private final ServiceOrderRepository serviceOrders;

    /**
     * Adds evidence after validating order ownership and the storage reference format.
     *
     * @param hotel tenant identifier
     * @param r validated evidence metadata
     * @return persisted evidence record
     * @implSpec Overrides must preserve tenant ownership and customer-to-order matching.
     */
    @Transactional
    public DocumentEvidence add(final UUID hotel, final DocumentEvidenceRequest r) {
        validateStorageReference(r.storageKey(), r.contentType());
        final var order =
                serviceOrders
                        .findByIdAndHotelId(r.serviceOrderId(), hotel)
                        .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
        if (!r.customerId().equals(order.getCustomerId())) {
            throw new IllegalArgumentException("DOCUMENT_CUSTOMER_MISMATCH");
        }
        return repository.save(
                DocumentEvidence.builder()
                        .hotelId(hotel)
                        .serviceOrderId(r.serviceOrderId())
                        .customerId(r.customerId())
                        .fileName(r.fileName().trim())
                        .contentType(r.contentType().trim())
                        .storageKey(r.storageKey().trim())
                        .accessLevel(r.accessLevel().trim().toUpperCase(Locale.ROOT))
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    private static void validateStorageReference(
            final String storageKey, final String contentType) {
        if (storageKey.length() > MAX_STORAGE_REFERENCE_LENGTH) {
            throw new IllegalArgumentException("DOCUMENT_REFERENCE_TOO_LARGE");
        }
        if (!storageKey.startsWith("data:")) {
            return;
        }
        final int comma = storageKey.indexOf(',');
        final int semicolon = storageKey.indexOf(';');
        if (comma < 0
                || semicolon < 0
                || semicolon > comma
                || !storageKey.substring(DATA_URL_PREFIX_LENGTH, semicolon)
                        .equalsIgnoreCase(contentType)
                || !";base64".equalsIgnoreCase(storageKey.substring(semicolon, comma))
                || !contentType.matches("image/(jpeg|png|webp)|application/pdf")) {
            throw new IllegalArgumentException(INVALID_DATA_URL);
        }
    }

    /**
     * Lists evidence records attached to a tenant-owned order.
     *
     * @param hotel tenant identifier
     * @param order order identifier
     * @return evidence records for that order
     * @implSpec Overrides must scope the lookup by both tenant and order.
     */
    @Transactional(readOnly = true)
    public List<DocumentEvidence> list(final UUID hotel, final UUID order) {
        return repository.findAllByHotelIdAndServiceOrderId(hotel, order);
    }

    /**
     * Reads binary content from a tenant-owned data-URL evidence record.
     *
     * @param hotel tenant identifier
     * @param id evidence identifier
     * @return decoded evidence bytes
     * @implSpec Overrides must verify tenant ownership before returning content.
     */
    @Transactional(readOnly = true)
    public byte[] content(final UUID hotel, final UUID id) {
        final DocumentEvidence evidence =
                repository
                        .findByIdAndHotelId(id, hotel)
                        .orElseThrow(() -> new NotFoundException("DOCUMENT_NOT_FOUND"));
        final String storageKey = evidence.getStorageKey();
        if (storageKey == null || !storageKey.startsWith("data:")) {
            throw new NotFoundException("DOCUMENT_CONTENT_NOT_FOUND");
        }
        final int comma = storageKey.indexOf(',');
        if (comma < 0 || !storageKey.substring(0, comma).endsWith(";base64")) {
            throw new IllegalArgumentException(INVALID_DATA_URL);
        }
        try {
            return Base64.getDecoder().decode(storageKey.substring(comma + 1));
        } catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException(INVALID_DATA_URL, exception);
        }
    }
}
