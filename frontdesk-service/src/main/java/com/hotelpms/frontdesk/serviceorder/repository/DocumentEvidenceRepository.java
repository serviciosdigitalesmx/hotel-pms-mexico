package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** DocumentEvidenceRepository domain type. */
public interface DocumentEvidenceRepository extends JpaRepository<DocumentEvidence, UUID> {
    /**
     * Executes the operation.
     *
     * @param id parameter.
     * @param hotelId parameter.
     * @return operation result.
     */
    Optional<DocumentEvidence> findByIdAndHotelId(UUID id, UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param serviceOrderId parameter.
     * @return operation result.
     */
    List<DocumentEvidence> findAllByHotelIdAndServiceOrderId(UUID hotelId, UUID serviceOrderId);
}
