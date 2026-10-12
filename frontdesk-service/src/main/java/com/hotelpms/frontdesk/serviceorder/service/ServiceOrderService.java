package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.exception.NotFoundException;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class ServiceOrderService {
  private final ServiceOrderRepository repository;
  private final RepairTestRepository repairTests;
  private final ServiceOrderStatusHistoryRepository history;
  private final AuditEventService auditEvents;

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
    final LocalDateTime now = LocalDateTime.now();
    final ServiceOrder saved = repository.save(
        ServiceOrder.builder()
            .hotelId(hotelId)
            .branchId(request.branchId())
            .customerId(request.customerId())
            .deviceId(request.deviceId())
            .summary(request.summary().trim())
            .reportedFailure(request.reportedFailure().trim())
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
    if (request.status() != ServiceOrderStatus.INTAKE && isBlank(order.getReportedFailure())) {
      throw new IllegalStateException("SERVICE_ORDER_REPORTED_FAILURE_REQUIRED");
    }
    if (request.status() != ServiceOrderStatus.INTAKE && isBlank(order.getIntakeChecklist())) {
      throw new IllegalStateException("SERVICE_ORDER_INTAKE_CHECKLIST_REQUIRED");
    }
    final ServiceOrderStatus previous = order.getStatus();
    order.setStatus(request.status());
    order.setDiagnosis(request.diagnosis());
    order.setWorkLog(request.workLog());
    order.setAssignedUserId(request.assignedUserId());
    order.setUpdatedAt(LocalDateTime.now());
    final ServiceOrder saved = repository.save(order);
    if(previous != request.status()) record(saved, previous, request.status(), "operator", request.workLog());
    if (previous != request.status()) {
      auditEvents.record(hotelId, null, AuditEvent.Category.OPERATIONAL, "SERVICE_ORDER_STATUS_CHANGED",
          saved.getId(), null, "{\"from\":\"" + previous + "\",\"to\":\"" + request.status() + "\"}");
    }
    return saved;
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
  public ServiceOrder quote(UUID hotelId,UUID id,QuotationUpdateRequest request){var o=get(hotelId,id);if(o.getStatus()!=ServiceOrderStatus.DIAGNOSIS&&o.getStatus()!=ServiceOrderStatus.REVIEW)throw new IllegalStateException("SERVICE_ORDER_NOT_DIAGNOSED");o.setProposedSolution(request.proposedSolution());o.setEstimatedMinutes(request.estimatedMinutes());o.setLaborAmount(request.laborAmount());o.setPartsAmount(request.partsAmount());o.setTaxAmount(request.taxAmount());o.setQuoteTotal(request.laborAmount().add(request.partsAmount()).add(request.taxAmount()));var previous=o.getStatus();o.setStatus(ServiceOrderStatus.QUOTED);o.setUpdatedAt(LocalDateTime.now());var saved=repository.save(o);record(saved,previous,ServiceOrderStatus.QUOTED,"operator","Cotización generada");return saved;}

  @Transactional
  public RepairTest addTest(UUID hotelId,UUID id,RepairTestRequest r){var o=get(hotelId,id);if(o.getStatus()!=ServiceOrderStatus.IN_PROGRESS&&o.getStatus()!=ServiceOrderStatus.QA)throw new IllegalStateException("SERVICE_ORDER_NOT_TESTABLE");if(o.getStatus()==ServiceOrderStatus.IN_PROGRESS){o.setStatus(ServiceOrderStatus.QA);o.setUpdatedAt(LocalDateTime.now());repository.save(o);record(o,ServiceOrderStatus.IN_PROGRESS,ServiceOrderStatus.QA,"operator","Pruebas iniciadas");}return repairTests.save(RepairTest.builder().hotelId(hotelId).serviceOrderId(id).testName(r.testName().trim()).passed(r.passed()).notes(r.notes()).technicianId(r.technicianId()).createdAt(LocalDateTime.now()).build());}
  @Transactional(readOnly=true) public List<RepairTest> tests(UUID hotelId,UUID id){get(hotelId,id);return repairTests.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId,id);}
  @Transactional(readOnly=true) public List<ServiceOrderStatusHistory> history(UUID hotelId,UUID id){get(hotelId,id);return history.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId,id);}
  @Transactional public ServiceOrder deliver(UUID hotelId,UUID id,UUID actor){var o=get(hotelId,id);if(o.getStatus()!=ServiceOrderStatus.QA)throw new IllegalStateException("SERVICE_ORDER_NOT_READY_FOR_DELIVERY");boolean failed=repairTests.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId,id).stream().anyMatch(t->!t.isPassed());if(failed)throw new IllegalStateException("REPAIR_TESTS_FAILED");o.setStatus(ServiceOrderStatus.COMPLETED);o.setDeliveredAt(LocalDateTime.now());o.setDeliveredBy(actor);o.setUpdatedAt(LocalDateTime.now());var saved=repository.save(o);record(saved,ServiceOrderStatus.QA,ServiceOrderStatus.COMPLETED,actor.toString(),"Equipo entregado");return saved;}
  private void record(ServiceOrder o,ServiceOrderStatus from,ServiceOrderStatus to,String actor,String notes){history.save(ServiceOrderStatusHistory.builder().hotelId(o.getHotelId()).serviceOrderId(o.getId()).fromStatus(from).toStatus(to).actor(actor).notes(notes).createdAt(LocalDateTime.now()).build());}
  private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
