package com.hotelpms.frontdesk.workflow.repository;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition,UUID>{
 Optional<WorkflowDefinition> findByIdAndHotelIdAndActiveTrue(UUID id,UUID hotelId);
 Optional<WorkflowDefinition> findByVerticalKeyAndHotelIdAndActiveTrue(String verticalKey,UUID hotelId);
 List<WorkflowDefinition> findAllByHotelIdAndActiveTrue(UUID hotelId);
 // Lock also serializes creation of the first instance, where no instance row exists yet.
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select w from WorkflowDefinition w where w.id=:id and w.hotelId=:hotelId and w.active=true")
 Optional<WorkflowDefinition> findLockedByIdAndHotelId(@Param("id") UUID id, @Param("hotelId") UUID hotelId);
}
