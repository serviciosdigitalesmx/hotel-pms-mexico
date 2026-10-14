package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import com.hotelpms.frontdesk.client.CapabilityClient;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.client.dto.CustomerDeviceResponse;
import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.repository.RepairTestRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderStatusHistoryRepository;
import com.hotelpms.frontdesk.serviceorder.domain.RepairTest;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory;
import com.hotelpms.frontdesk.serviceorder.dto.QuotationUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.RepairTestRequest;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionRepository;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class ServiceOrderService {
  private final ServiceOrderRepository repository;
  private final RepairTestRepository repairTests;
  private final ServiceOrderStatusHistoryRepository history;
  private final AuditEventService auditEvents;
  private final WorkflowService workflows;
  private final WorkflowTransitionRepository workflowTransitions;
  private final CapabilityClient capabilityClient;
  private final HotelSettingsRepository hotelSettingsRepository;
  private final GuestClient guestClient;
  private final BranchValidationService branchValidation;

  public record ConfiguredWorkflowStatus(String workflowId, String stateKey, String label) { }
  public record ConfiguredWorkflowTransition(String key, String fromStateKey, String toStateKey) { }
  public record ConfiguredWorkflowView(String id, String name, String currentState, String initialState,
      java.util.List<ConfiguredWorkflowTransition> transitions) { }

  @Transactional(readOnly = true)
  public ConfiguredWorkflowView configuredWorkflow(final UUID hotelId, final UUID orderId) {
    get(hotelId, orderId);
    WorkflowDefinition workflow = workflows.activeForVertical(hotelId, "REPAIR");
    if (workflow == null) return null;
    WorkflowState current = workflows.stateForAggregate(hotelId, workflow.getId(), orderId);
    String initialState = workflows.states(hotelId, workflow.getId()).stream()
        .filter(WorkflowState::isInitialState).map(WorkflowState::getKey).findFirst().orElse(null);
    return new ConfiguredWorkflowView(workflow.getId().toString(), workflow.getName(),
        current == null ? null : current.getKey(),
        initialState,
        workflowTransitions.findAllByWorkflowId(workflow.getId()).stream()
            .filter(t -> t.getFromState().isActive() && t.getToState().isActive())
            .map(t -> new ConfiguredWorkflowTransition(t.getKey(), t.getFromState().getKey(), t.getToState().getKey()))
            .toList());
  }

  @Transactional
  public ConfiguredWorkflowView advanceConfiguredWorkflow(final UUID hotelId, final UUID orderId,
      final String transitionKey, final String actor, final java.util.Collection<String> authorities) {
    ServiceOrder order = get(hotelId, orderId);
    WorkflowDefinition workflow = workflows.activeForVertical(hotelId, "REPAIR");
    if (workflow == null) throw new NotFoundException("No active REPAIR workflow configured");
    WorkflowState current = workflows.stateForAggregate(hotelId, workflow.getId(), orderId);
    String fromKey = current == null ? workflows.states(hotelId, workflow.getId()).stream()
        .filter(WorkflowState::isInitialState).map(WorkflowState::getKey).findFirst()
        .orElseThrow(() -> new IllegalStateException("REPAIR workflow has no initial state")) : current.getKey();
    WorkflowTransition transition = workflows.transitions(hotelId, workflow.getId()).stream()
        .filter(t -> t.getKey().equals(transitionKey) && t.getFromState().getKey().equals(fromKey)
            && t.getFromState().isActive() && t.getToState().isActive())
        .findFirst().orElseThrow(() -> new IllegalStateException("Transition is not allowed from current state"));
    boolean privileged = authorities.contains("ROLE_ADMIN") || authorities.contains("ROLE_OWNER");
    boolean granted = privileged || Boolean.TRUE.equals(capabilityClient.evaluate(
        transition.getRequiredPermission()).get("enabled"));
    if (!granted) {
      throw new org.springframework.security.access.AccessDeniedException("Missing workflow permission");
    }
    workflows.transition(hotelId, workflow.getId(), new TransitionRequest(orderId, fromKey, transitionKey), actor);
    WorkflowState destination = transition.getToState();
    ServiceOrderStatus nextStatus = switch (destination.getSemanticPhase()) {
      case INTAKE -> ServiceOrderStatus.INTAKE;
      case DIAGNOSIS -> ServiceOrderStatus.DIAGNOSIS;
      case QUOTE -> ServiceOrderStatus.QUOTED;
      case AUTHORIZATION -> ServiceOrderStatus.AUTHORIZED;
      case WORK -> ServiceOrderStatus.IN_PROGRESS;
      case READY -> ServiceOrderStatus.QA;
      case DELIVERED, CLOSED -> ServiceOrderStatus.COMPLETED;
      case WARRANTY -> order.getStatus();
      case CANCELLED -> ServiceOrderStatus.CANCELLED;
    };
    if (nextStatus == ServiceOrderStatus.COMPLETED && order.getStatus() != ServiceOrderStatus.QA) {
      throw new IllegalStateException("Service order must pass QA before configured delivery");
    }
    if (nextStatus == ServiceOrderStatus.COMPLETED
        && repairTests.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId, orderId)
            .stream().anyMatch(test -> !test.isPassed())) {
      throw new IllegalStateException("REPAIR_TESTS_FAILED");
    }
    ServiceOrderStatus previous = order.getStatus();
    order.setStatus(nextStatus);
    if (nextStatus == ServiceOrderStatus.COMPLETED && previous != ServiceOrderStatus.COMPLETED) {
      order.setDeliveredAt(LocalDateTime.now());
    }
    order.setUpdatedAt(LocalDateTime.now());
    repository.save(order);
    if (previous != nextStatus) record(order, previous, nextStatus, actor, "Flujo configurable: " + transitionKey);
    return configuredWorkflow(hotelId, orderId);
  }


  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param request parameter.
   *
   * @return operation result.
   */

  @Transactional
  public ServiceOrder create(final UUID hotelId, final ServiceOrderRequest request) {
    if (request.branchId() != null) branchValidation.requireActiveBranch(hotelId, request.branchId());
    branchValidation.requireTechnician(request.assignedUserId(), request.branchId());
    validateCustomerDevice(request.customerId(), request.deviceId());
    final LocalDateTime now = LocalDateTime.now();
    final ServiceOrder saved = repository.save(
        ServiceOrder.builder()
            .hotelId(hotelId)
            .branchId(request.branchId())
            .customerId(request.customerId())
            .deviceId(request.deviceId())
            .summary(request.summary().trim())
            .reportedFailure(request.reportedFailure().trim())
            .priority(request.priority() == null ? "NORMAL" : request.priority())
            .serviceName(request.serviceName() == null ? null : request.serviceName().trim())
            .depositAmount(request.depositAmount() == null ? java.math.BigDecimal.ZERO : request.depositAmount())
            .assignedUserId(request.assignedUserId())
            .intakeChecklist(request.intakeChecklist().trim())
            .promisedAt(request.promisedAt())
            .intakeEvidence(request.intakeEvidence() == null ? null : request.intakeEvidence().trim())
            .createdAt(now)
            .updatedAt(now)
            .build());
    record(saved, null, saved.getStatus(), "system", "Orden recibida");
    auditEvents.record(hotelId, null, AuditEvent.Category.OPERATIONAL, "SERVICE_ORDER_CREATED",
        saved.getId(), null, "{\"branchId\":\"" + saved.getBranchId() + "\"}");
    return saved;
  }
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param id parameter.
   *
   * @param request parameter.
   *
   * @return operation result.
   */

  @Transactional
  public ServiceOrder update(final UUID hotelId, final UUID id, final ServiceOrderUpdateRequest request) {
    final ServiceOrder order =
        repository
            .findByIdAndHotelId(id, hotelId)
            .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
    if (order.getStatus() == ServiceOrderStatus.COMPLETED
        || order.getStatus() == ServiceOrderStatus.CANCELLED) {
      throw new IllegalStateException("SERVICE_ORDER_CLOSED");
    }
    if (request.status() != order.getStatus()
        && !isManualStatusTransitionAllowed(order.getStatus(), request.status())) {
      throw new IllegalStateException("SERVICE_ORDER_TRANSITION_REQUIRES_DEDICATED_ACTION");
    }
    if (request.status() != ServiceOrderStatus.INTAKE && isBlank(order.getReportedFailure())) {
      throw new IllegalStateException("SERVICE_ORDER_REPORTED_FAILURE_REQUIRED");
    }
    if (request.status() != ServiceOrderStatus.INTAKE && isBlank(order.getIntakeChecklist())) {
      throw new IllegalStateException("SERVICE_ORDER_INTAKE_CHECKLIST_REQUIRED");
    }
    branchValidation.requireTechnician(request.assignedUserId(), order.getBranchId());
    final ServiceOrderStatus previous = order.getStatus();
    final String previousWorkLog = order.getWorkLog();
    order.setStatus(request.status());
    order.setDiagnosis(request.diagnosis());
    order.setWorkLog(request.workLog());
    order.setAssignedUserId(request.assignedUserId());
    if (request.priority() != null) order.setPriority(request.priority());
    if (request.promisedAt() != null) order.setPromisedAt(request.promisedAt());
    else if (Boolean.TRUE.equals(request.clearPromisedAt())) order.setPromisedAt(null);
    order.setUpdatedAt(LocalDateTime.now());
    final ServiceOrder saved = repository.save(order);
    if(previous != request.status()) record(saved, previous, request.status(), "operator", request.workLog());
    else if (!java.util.Objects.equals(previousWorkLog, request.workLog())
        && ((previousWorkLog != null && !previousWorkLog.isBlank())
            || (request.workLog() != null && !request.workLog().isBlank()))) {
      final String note = request.workLog() == null || request.workLog().isBlank()
          ? "Bitácora de reparación eliminada"
          : request.workLog();
      record(saved, previous, previous, "operator", note);
    }
    if (previous != request.status()) {
      auditEvents.record(hotelId, null, AuditEvent.Category.OPERATIONAL, "SERVICE_ORDER_STATUS_CHANGED",
          saved.getId(), null, "{\"from\":\"" + previous + "\",\"to\":\"" + request.status() + "\"}");
    }
    return saved;
  }

  private void validateCustomerDevice(final UUID customerId, final UUID deviceId) {
    if (deviceId == null) return;
    final CustomerDeviceResponse device = guestClient.getCustomerDevice(customerId, deviceId);
    if (!customerId.equals(device.customerId())) {
      throw new IllegalArgumentException("DEVICE_CUSTOMER_MISMATCH");
    }
  }

  private boolean isManualStatusTransitionAllowed(
      final ServiceOrderStatus current, final ServiceOrderStatus next) {
    return switch (current) {
      case INTAKE -> next == ServiceOrderStatus.REVIEW;
      case REVIEW, QUOTE_REJECTED -> next == ServiceOrderStatus.DIAGNOSIS;
      case AUTHORIZED -> next == ServiceOrderStatus.IN_PROGRESS;
      default -> false;
    };
  }
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param id parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public ServiceOrder get(final UUID hotelId, final UUID id) {
    return repository
        .findByIdAndHotelId(id, hotelId)
        .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
  }
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param customerId parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<ServiceOrder> list(final UUID hotelId, final UUID customerId) {
    return repository.findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(hotelId, customerId);
  }
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<ServiceOrder> listAll(final UUID hotelId) {
    return repository.findAllByHotelIdOrderByCreatedAtDesc(hotelId);
  }

  @Transactional(readOnly = true)
  public Page<ServiceOrder> listAll(final UUID hotelId, final Pageable pageable) {
    return repository.findAllByHotelId(hotelId, pageable);
  }
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param id parameter.
   *
   * @param actor parameter.
   *
   * @param request parameter.
   *
   * @return operation result.
   */

  @Transactional
  public ServiceOrder authorize(final UUID hotelId, final UUID id, final UUID actor, final AuthorizationRequest request) {
    final ServiceOrder order = get(hotelId, id);
    if (order.getStatus() != ServiceOrderStatus.QUOTED) {
      throw new IllegalStateException("SERVICE_ORDER_NOT_QUOTED");
    }
    order.setStatus(ServiceOrderStatus.AUTHORIZED);
    order.setAuthorizedBy(actor);
    order.setAuthorizationEvidence(request.evidence().trim());
    order.setAuthorizedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    final ServiceOrder saved=repository.save(order);record(saved,ServiceOrderStatus.QUOTED,ServiceOrderStatus.AUTHORIZED,actor.toString(),request.evidence());return saved;
  }
  @Transactional public ServiceOrder reject(UUID hotelId,UUID id,UUID actor,AuthorizationRequest request){var order=get(hotelId,id);if(order.getStatus()!=ServiceOrderStatus.QUOTED)throw new IllegalStateException("SERVICE_ORDER_NOT_QUOTED");order.setStatus(ServiceOrderStatus.QUOTE_REJECTED);order.setAuthorizationEvidence(request.evidence().trim());order.setAuthorizedBy(actor);order.setAuthorizedAt(LocalDateTime.now());order.setUpdatedAt(LocalDateTime.now());var saved=repository.save(order);record(saved,ServiceOrderStatus.QUOTED,ServiceOrderStatus.QUOTE_REJECTED,actor.toString(),request.evidence());return saved;}

  @Transactional
  public ServiceOrder quote(UUID hotelId,UUID id,QuotationUpdateRequest request){var o=get(hotelId,id);if(o.getStatus()!=ServiceOrderStatus.DIAGNOSIS&&o.getStatus()!=ServiceOrderStatus.REVIEW)throw new IllegalStateException("SERVICE_ORDER_NOT_DIAGNOSED");o.setProposedSolution(request.proposedSolution());o.setEstimatedMinutes(request.estimatedMinutes());o.setLaborAmount(request.laborAmount());o.setPartsAmount(request.partsAmount());var rate=hotelSettingsRepository.findById(hotelId).map(s->s.getDefaultTaxRate()).orElse(null);var tax=rate==null?request.taxAmount():request.laborAmount().add(request.partsAmount()).multiply(rate).divide(new java.math.BigDecimal("100"),2,java.math.RoundingMode.HALF_UP);o.setTaxAmount(tax);o.setQuoteTotal(request.laborAmount().add(request.partsAmount()).add(tax));var previous=o.getStatus();o.setStatus(ServiceOrderStatus.QUOTED);o.setUpdatedAt(LocalDateTime.now());var saved=repository.save(o);record(saved,previous,ServiceOrderStatus.QUOTED,"operator","Cotización generada");return saved;}

  @Transactional
  public RepairTest addTest(UUID hotelId,UUID id,RepairTestRequest r){var o=get(hotelId,id);if(o.getStatus()!=ServiceOrderStatus.IN_PROGRESS&&o.getStatus()!=ServiceOrderStatus.QA)throw new IllegalStateException("SERVICE_ORDER_NOT_TESTABLE");if(o.getStatus()==ServiceOrderStatus.IN_PROGRESS){o.setStatus(ServiceOrderStatus.QA);o.setUpdatedAt(LocalDateTime.now());repository.save(o);record(o,ServiceOrderStatus.IN_PROGRESS,ServiceOrderStatus.QA,"operator","Pruebas iniciadas");}return repairTests.save(RepairTest.builder().hotelId(hotelId).serviceOrderId(id).testName(r.testName().trim()).passed(r.passed()).notes(r.notes()).technicianId(r.technicianId()).createdAt(LocalDateTime.now()).build());}
  @Transactional(readOnly=true) public List<RepairTest> tests(UUID hotelId,UUID id){get(hotelId,id);return repairTests.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId,id);}
  @Transactional(readOnly=true) public List<ServiceOrderStatusHistory> history(UUID hotelId,UUID id){get(hotelId,id);return history.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId,id);}
  @Transactional public ServiceOrder deliver(UUID hotelId,UUID id,UUID actor){var o=get(hotelId,id);if(o.getStatus()!=ServiceOrderStatus.QA)throw new IllegalStateException("SERVICE_ORDER_NOT_READY_FOR_DELIVERY");boolean failed=repairTests.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId,id).stream().anyMatch(t->!t.isPassed());if(failed)throw new IllegalStateException("REPAIR_TESTS_FAILED");o.setStatus(ServiceOrderStatus.COMPLETED);o.setDeliveredAt(LocalDateTime.now());o.setDeliveredBy(actor);o.setUpdatedAt(LocalDateTime.now());var saved=repository.save(o);record(saved,ServiceOrderStatus.QA,ServiceOrderStatus.COMPLETED,actor.toString(),"Equipo entregado");return saved;}
  private void record(ServiceOrder o,ServiceOrderStatus from,ServiceOrderStatus to,String actor,String notes){history.save(ServiceOrderStatusHistory.builder().hotelId(o.getHotelId()).serviceOrderId(o.getId()).fromStatus(from).toStatus(to).actor(actor).notes(notes).createdAt(LocalDateTime.now()).build());}
  private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
