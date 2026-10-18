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

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class DocumentEvidenceService {
    private final DocumentEvidenceRepository repository;
    private final ServiceOrderRepository serviceOrders;

    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @param r parameter.
     * @return operation result.
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
        if (storageKey.length() > 14_000_000) {
            throw new IllegalArgumentException("DOCUMENT_REFERENCE_TOO_LARGE");
        }
        if (!storageKey.startsWith("data:")) return;
        final int comma = storageKey.indexOf(',');
        final int semicolon = storageKey.indexOf(';');
        if (comma < 0
                || semicolon < 0
                || semicolon > comma
                || !storageKey.substring(5, semicolon).equalsIgnoreCase(contentType)
                || !storageKey.substring(semicolon, comma).equalsIgnoreCase(";base64")
                || !contentType.matches("image/(jpeg|png|webp)|application/pdf")) {
            throw new IllegalArgumentException("DOCUMENT_DATA_URL_INVALID");
        }
    }

    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @param order parameter.
     * @return operation result.
     */
    @Transactional(readOnly = true)
    public List<DocumentEvidence> list(final UUID hotel, final UUID order) {
        return repository.findAllByHotelIdAndServiceOrderId(hotel, order);
    }

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
            throw new IllegalArgumentException("DOCUMENT_DATA_URL_INVALID");
        }
        try {
            return Base64.getDecoder().decode(storageKey.substring(comma + 1));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("DOCUMENT_DATA_URL_INVALID", exception);
        }
    }
}
