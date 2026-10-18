package com.hotelpms.frontdesk.workflow.repository;

import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** WorkflowDefinitionRepository domain type. */
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {
    /**
     * Executes the operation.
     *
     * @param id parameter.
     * @param hotelId parameter.
     * @return operation result.
     */
    Optional<WorkflowDefinition> findByIdAndHotelIdAndActiveTrue(UUID id, UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param verticalKey parameter.
     * @param hotelId parameter.
     * @return operation result.
     */
    Optional<WorkflowDefinition> findByVerticalKeyAndHotelIdAndActiveTrue(
            String verticalKey, UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @return operation result.
     */
    List<WorkflowDefinition> findAllByHotelIdAndActiveTrue(UUID hotelId);

    // Lock also serializes creation of the first instance, where no instance row exists yet.
    /**
     * Finds and locks an active workflow definition.
     *
     * @param id workflow identifier.
     * @param hotelId hotel identifier.
     * @return locked workflow definition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select w from WorkflowDefinition w where w.id=:id and w.hotelId=:hotelId and"
                + " w.active=true")
    Optional<WorkflowDefinition> findLockedByIdAndHotelId(
            @Param("id") UUID id, @Param("hotelId") UUID hotelId);
}
