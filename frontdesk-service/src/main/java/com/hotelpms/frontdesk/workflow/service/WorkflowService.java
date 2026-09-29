package com.hotelpms.frontdesk.workflow.service;
import com.hotelpms.frontdesk.exception.*; import com.hotelpms.frontdesk.workflow.domain.*; import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.*; import com.hotelpms.frontdesk.workflow.repository.*; import jakarta.transaction.Transactional; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import java.util.*;
@Service @RequiredArgsConstructor public class WorkflowService { private final WorkflowDefinitionRepository definitions; private final WorkflowStateRepository states; private final WorkflowTransitionRepository transitions; private final WorkflowTransitionAuditRepository audits; private final WorkflowInstanceRepository instances;
 @Transactional public WorkflowDefinition create(UUID hotel,CreateWorkflowRequest r){return definitions.save(new WorkflowDefinition(hotel,r.verticalKey(),r.name()));}
 @Transactional public WorkflowState addState(UUID hotel,UUID workflowId,AddStateRequest r){WorkflowDefinition w=definition(hotel,workflowId); return states.save(new WorkflowState(w,r.key(),r.label(),r.semanticPhase(),r.initialState(),r.terminalState()));}
 @Transactional public WorkflowTransition addTransition(UUID hotel,UUID workflowId,AddTransitionRequest r){
     if(r.requiredPermission()==null || r.requiredPermission().isBlank() || isRoleLabel(r.requiredPermission()))
         throw new BadRequestException("Workflow transitions require an explicit permission authority");
     WorkflowDefinition w=definition(hotel,workflowId); WorkflowState from=states.findByWorkflowIdAndKey(workflowId,r.fromStateKey()).orElseThrow(()->new NotFoundException("Workflow state not found")); WorkflowState to=states.findByWorkflowIdAndKey(workflowId,r.toStateKey()).orElseThrow(()->new NotFoundException("Workflow state not found")); return transitions.save(new WorkflowTransition(w,from,to,r.transitionKey(),r.requiredPermission()));}
 @Transactional public WorkflowTransition transition(UUID hotel,UUID workflowId,TransitionRequest r,String actor){
     definitions.findLockedByIdAndHotelId(workflowId,hotel)
             .orElseThrow(()->new NotFoundException("Workflow not found"));
     WorkflowTransition t=transitions.findByTenantAndWorkflowIdAndFromStateKeyAndKey(
             hotel,workflowId,r.fromStateKey(),r.transitionKey())
             .orElseThrow(()->new ConflictException("Transition is not allowed from current state"));
     WorkflowInstance instance=instances.findLockedByHotelIdAndWorkflowIdAndAggregateId(hotel,workflowId,r.aggregateId())
             .orElseGet(()->{
                 if(!t.getFromState().isInitialState())
                     throw new ConflictException("A new workflow instance must start at its initial state");
                 return new WorkflowInstance(hotel,workflowId,r.aggregateId(),r.fromStateKey());
             });
     if(!instance.getStateKey().equals(r.fromStateKey()) || t.getFromState().isTerminalState())
         throw new ConflictException("Workflow state changed or is terminal");
     instance.moveTo(t.getToState().getKey());
     instances.save(instance);
     audits.save(new WorkflowTransitionAudit(hotel,workflowId,r.aggregateId(),t.getFromState().getKey(),
             t.getToState().getKey(),t.getToState().getSemanticPhase(),actor));
     return t;
 }
 @Transactional public WorkflowDefinition definition(UUID hotel,UUID id){return definitions.findByIdAndHotelIdAndActiveTrue(id,hotel).orElseThrow(()->new NotFoundException("Workflow not found"));}
 private boolean isRoleLabel(String permission){String normalized=permission.trim().toUpperCase(Locale.ROOT);return normalized.startsWith("ROLE_")||normalized.equals("ADMIN")||normalized.equals("OWNER");}
 public List<WorkflowState> states(UUID hotel,UUID id){definition(hotel,id);return states.findAllByWorkflowId(id);} public List<WorkflowTransition> transitions(UUID hotel,UUID id){definition(hotel,id);return transitions.findAllByWorkflowId(id);} }
