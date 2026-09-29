package com.hotelpms.frontdesk.workflow.repository;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition,UUID>{ List<WorkflowTransition> findAllByWorkflowId(UUID workflowId); Optional<WorkflowTransition> findByWorkflowIdAndFromStateKeyAndKey(UUID workflowId,String from,String key); }
