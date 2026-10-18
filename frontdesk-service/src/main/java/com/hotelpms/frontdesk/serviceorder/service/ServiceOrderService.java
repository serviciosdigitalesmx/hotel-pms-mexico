package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.client.CapabilityClient;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.client.dto.CustomerDeviceResponse;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.notifications.OperationalNotificationOutboxService;
import com.hotelpms.frontdesk.serviceorder.domain.RepairTest;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.dto.QuotationUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.RepairTestRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.repository.RepairTestRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderStatusHistoryRepository;
import com.hotelpms.frontdesk.stays.domain.HotelSettings;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import com.hotelpms.frontdesk.util.ImmutableCopies;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionRepository;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Coordinates tenant-scoped repair orders and their workflow, inventory, and payment effects.
 */
@Service
@RequiredArgsConstructor
public class ServiceOrderService {
    private static final String SERVICE_ORDER_NOT_FOUND = "SERVICE_ORDER_NOT_FOUND";
    private static final String OPERATOR_ACTOR = "operator";
    private static final String PAYMENT_PENDING = "PENDING";
    private static final String PAYMENT_PAID = "PAID";
    private static final String PAYMENT_METHOD_LABEL = " · método ";
    private static final String ORDER_LABEL = "La orden ";
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
    private final InventoryService inventoryService;
    private final OperationalNotificationOutboxService notificationOutbox;

    /**
     * Returns the active repair workflow for an order in the requested tenant.
     *
     * @param hotelId tenant identifier
     * @param orderId repair-order identifier
     * @return active workflow view, or {@code null} when none is configured
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public ConfiguredWorkflowView configuredWorkflow(final UUID hotelId, final UUID orderId) {
        get(hotelId, orderId);
        final WorkflowDefinition workflow = workflows.activeForVertical(hotelId, "REPAIR");
        if (workflow == null) {
            return null;
        }
        final WorkflowState current = workflows.stateForAggregate(hotelId, workflow.getId(), orderId);
        final String initialState =
                workflows.states(hotelId, workflow.getId()).stream()
                        .filter(WorkflowState::isInitialState)
                        .map(WorkflowState::getKey)
                        .findFirst()
                        .orElse(null);
        return new ConfiguredWorkflowView(
                workflow.getId().toString(),
                workflow.getName(),
                current == null ? null : current.getKey(),
                initialState,
                workflowTransitions.findAllByWorkflowId(workflow.getId()).stream()
                        .filter(t -> t.getFromState().isActive() && t.getToState().isActive())
                        .map(
                                t ->
                                        new ConfiguredWorkflowTransition(
                                                t.getKey(),
                                                t.getFromState().getKey(),
                                                t.getToState().getKey()))
                        .toList());
    }

    /**
     * Applies an authorized configured transition and synchronizes the order's operational state.
     *
     * @param hotelId tenant identifier
     * @param orderId repair-order identifier
     * @param transitionKey requested transition key
     * @param actor authenticated actor label
     * @param authorities actor authorities used for privileged transitions
     * @return refreshed workflow view
     * @implSpec Overrides must preserve authorization and workflow invariants.
     */
    @Transactional
    public ConfiguredWorkflowView advanceConfiguredWorkflow(
            final UUID hotelId,
            final UUID orderId,
            final String transitionKey,
            final String actor,
            final Collection<String> authorities) {
        final ServiceOrder order = get(hotelId, orderId);
        final WorkflowDefinition workflow = workflows.activeForVertical(hotelId, "REPAIR");
        if (workflow == null) {
            throw new NotFoundException("No active REPAIR workflow configured");
        }
        final WorkflowState current = workflows.stateForAggregate(hotelId, workflow.getId(), orderId);
        final String fromKey =
                current == null
                        ? workflows.states(hotelId, workflow.getId()).stream()
                                .filter(WorkflowState::isInitialState)
                                .map(WorkflowState::getKey)
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "REPAIR workflow has no initial state"))
                        : current.getKey();
        final WorkflowTransition transition =
                workflows.transitions(hotelId, workflow.getId()).stream()
                        .filter(
                                t ->
                                        t.getKey().equals(transitionKey)
                                                && t.getFromState().getKey().equals(fromKey)
                                                && t.getFromState().isActive()
                                                && t.getToState().isActive())
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Transition is not allowed from current state"));
        final boolean privileged =
                authorities.contains("ROLE_ADMIN") || authorities.contains("ROLE_OWNER");
        final boolean granted =
                privileged
                        || Boolean.TRUE.equals(
                                capabilityClient
                                        .evaluate(transition.getRequiredPermission())
                                        .get("enabled"));
        if (!granted) {
            throw new AccessDeniedException("Missing workflow permission");
        }
        workflows.transition(
                hotelId,
                workflow.getId(),
                new TransitionRequest(orderId, fromKey, transitionKey),
                actor);
        final WorkflowState destination = transition.getToState();
        final ServiceOrderStatus nextStatus =
                switch (destination.getSemanticPhase()) {
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
        if (nextStatus == ServiceOrderStatus.COMPLETED
                && order.getStatus() != ServiceOrderStatus.QA) {
            throw new IllegalStateException(
                    "Service order must pass QA before configured delivery");
        }
        if (nextStatus == ServiceOrderStatus.COMPLETED
                && hasFailedLatestRepairTest(hotelId, orderId)) {
            throw new IllegalStateException("REPAIR_TESTS_FAILED");
        }
        final ServiceOrderStatus previous = order.getStatus();
        order.setStatus(nextStatus);
        if (nextStatus == ServiceOrderStatus.COMPLETED
                && previous != ServiceOrderStatus.COMPLETED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());
        repository.save(order);
        if (nextStatus == ServiceOrderStatus.CANCELLED
                && previous != ServiceOrderStatus.CANCELLED) {
            inventoryService.releaseReservationsForOrder(hotelId, orderId);
        } else if (nextStatus == ServiceOrderStatus.COMPLETED
                && previous != ServiceOrderStatus.COMPLETED) {
            inventoryService.consumeReservations(hotelId, orderId);
        }
        if (previous != nextStatus) {
            record(order, previous, nextStatus, actor, "Flujo configurable: " + transitionKey);
        }
        return configuredWorkflow(hotelId, orderId);
    }

    /**
     * Creates a tenant-scoped order using the system actor for its initial history entry.
     *
     * @param hotelId tenant identifier
     * @param request order creation data
     * @return persisted order
     * @implSpec Overrides must preserve tenant scoping and order-creation side effects.
     */
    @Transactional
    public ServiceOrder create(final UUID hotelId, final ServiceOrderRequest request) {
        return create(hotelId, request, "system");
    }

    /**
     * Creates an order and records its deposit, assignment, audit, and history effects.
     *
     * @param hotelId tenant identifier
     * @param request order creation data
     * @param actor actor recorded in the order history
     * @return persisted order
     * @implSpec Overrides must preserve tenant scoping and order-creation side effects.
     */
    @Transactional
    public ServiceOrder create(
            final UUID hotelId, final ServiceOrderRequest request, final String actor) {
        final BigDecimal deposit =
                request.depositAmount() == null ? BigDecimal.ZERO : request.depositAmount();
        final String depositMethod =
                request.depositPaymentMethod() == null && deposit.compareTo(BigDecimal.ZERO) > 0
                        ? "OTHER"
                        : request.depositPaymentMethod();
        if (deposit.compareTo(BigDecimal.ZERO) > 0
                && !Set.of("CASH", "CHECK", "BANK_DEPOSIT", "PAYPAL", "CARD", "OTHER")
                        .contains(depositMethod)) {
            throw new IllegalArgumentException("DEPOSIT_PAYMENT_METHOD_REQUIRED");
        }
        if (request.branchId() != null) {
            branchValidation.requireActiveBranch(hotelId, request.branchId());
        }
        branchValidation.requireTechnician(request.assignedUserId(), request.branchId());
        validateCustomerDevice(request.customerId(), request.deviceId());
        final LocalDateTime now = LocalDateTime.now();
        ServiceOrder saved =
                repository.save(
                        ServiceOrder.builder()
                                .hotelId(hotelId)
                                .branchId(request.branchId())
                                .customerId(request.customerId())
                                .deviceId(request.deviceId())
                                .summary(request.summary().trim())
                                .reportedFailure(request.reportedFailure().trim())
                                .priority(
                                        request.priority() == null ? "NORMAL" : request.priority())
                                .serviceName(
                                        request.serviceName() == null
                                                ? null
                                                : request.serviceName().trim())
                                .depositAmount(deposit)
                                .paidAmount(deposit)
                                .paymentMethod(
                                        deposit.compareTo(BigDecimal.ZERO) > 0
                                                ? depositMethod
                                                : null)
                                .assignedUserId(request.assignedUserId())
                                .intakeChecklist(request.intakeChecklist().trim())
                                .promisedAt(request.promisedAt())
                                .intakeEvidence(
                                        request.intakeEvidence() == null
                                                ? null
                                                : request.intakeEvidence().trim())
                                .createdAt(now)
                                .updatedAt(now)
                                .build());
        if (deposit.compareTo(BigDecimal.ZERO) > 0) {
            appendPaymentEvent(saved, deposit, depositMethod, "DEPOSIT", now);
            saved = repository.save(saved);
        }
        final String historyActor = actor == null || actor.isBlank() ? "system" : actor;
        record(saved, null, saved.getStatus(), historyActor, "Orden recibida");
        if (deposit.compareTo(BigDecimal.ZERO) > 0) {
            record(
                    saved,
                    saved.getStatus(),
                    saved.getStatus(),
                    historyActor,
                    "Anticipo recibido "
                            + deposit.toPlainString()
                            + PAYMENT_METHOD_LABEL
                            + depositMethod);
        }
        auditEvents.record(
                hotelId,
                null,
                AuditEvent.Category.OPERATIONAL,
                "SERVICE_ORDER_CREATED",
                saved.getId(),
                null,
                "{\"branchId\":\"" + saved.getBranchId() + "\"}");
        if (saved.getAssignedUserId() != null) {
            notificationOutbox.enqueue(
                    hotelId,
                    "ORDER_ASSIGNED",
                    "Orden de servicio asignada",
                    "Se asignó la orden "
                            + saved.getId()
                            + ". Consulta el tablero de Fixi para ver a la persona responsable.",
                    "ORDER_ASSIGNED:" + saved.getId() + ":" + UUID.randomUUID());
        }
        return saved;
    }

    /**
     * Updates an open order without bypassing dedicated workflow transitions.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param request mutable order fields
     * @return updated order
     * @implSpec Overrides must preserve tenant scoping and transition restrictions.
     */
    @Transactional
    public ServiceOrder update(
            final UUID hotelId, final UUID id, final ServiceOrderUpdateRequest request) {
        final ServiceOrder order =
                repository
                        .findByIdAndHotelId(id, hotelId)
                        .orElseThrow(() -> new NotFoundException(SERVICE_ORDER_NOT_FOUND));
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
        final UUID previousAssignedUserId = order.getAssignedUserId();
        order.setStatus(request.status());
        order.setDiagnosis(request.diagnosis());
        order.setWorkLog(request.workLog());
        order.setAssignedUserId(request.assignedUserId());
        if (request.priority() != null) {
            order.setPriority(request.priority());
        }
        if (request.promisedAt() != null) {
            order.setPromisedAt(request.promisedAt());
        } else if (Boolean.TRUE.equals(request.clearPromisedAt())) {
            order.setPromisedAt(null);
        }
        order.setUpdatedAt(LocalDateTime.now());
        final ServiceOrder saved = repository.save(order);
        if (!Objects.equals(previousAssignedUserId, request.assignedUserId())
                && request.assignedUserId() != null) {
            notificationOutbox.enqueue(
                    hotelId,
                    "ORDER_ASSIGNED",
                    "Orden de servicio reasignada",
                    ORDER_LABEL
                            + saved.getId()
                            + " cambió de responsable. Consulta el tablero de Fixi para ver la"
                            + " asignación actual.",
                    "ORDER_ASSIGNED:" + saved.getId() + ":" + UUID.randomUUID());
        }
        if (previous != request.status()) {
            record(saved, previous, request.status(), OPERATOR_ACTOR, request.workLog());
        } else if (!Objects.equals(previousWorkLog, request.workLog())
                && (previousWorkLog != null && !previousWorkLog.isBlank()
                        || request.workLog() != null && !request.workLog().isBlank())) {
            final String note =
                    request.workLog() == null || request.workLog().isBlank()
                            ? "Bitácora de reparación eliminada"
                            : request.workLog();
            record(saved, previous, previous, OPERATOR_ACTOR, note);
        }
        if (previous != request.status()) {
            auditEvents.record(
                    hotelId,
                    null,
                    AuditEvent.Category.OPERATIONAL,
                    "SERVICE_ORDER_STATUS_CHANGED",
                    saved.getId(),
                    null,
                    "{\"from\":\"" + previous + "\",\"to\":\"" + request.status() + "\"}");
        }
        return saved;
    }

    private void validateCustomerDevice(final UUID customerId, final UUID deviceId) {
        if (deviceId == null) {
            return;
        }
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
     * Loads an order by identifier while enforcing tenant ownership in the repository query.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @return matching order
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public ServiceOrder get(final UUID hotelId, final UUID id) {
        return repository
                .findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> new NotFoundException(SERVICE_ORDER_NOT_FOUND));
    }

    /**
     * Lists a customer's orders within the requested tenant, newest first.
     *
     * @param hotelId tenant identifier
     * @param customerId customer identifier
     * @return matching orders
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<ServiceOrder> list(final UUID hotelId, final UUID customerId) {
        return repository.findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(hotelId, customerId);
    }

    /**
     * Lists all tenant orders, newest first.
     *
     * @param hotelId tenant identifier
     * @return tenant orders
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<ServiceOrder> listAll(final UUID hotelId) {
        return repository.findAllByHotelIdOrderByCreatedAtDesc(hotelId);
    }

    /**
     * Returns a tenant-scoped page of repair orders.
     *
     * @param hotelId tenant identifier
     * @param pageable page request
     * @return requested order page
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public Page<ServiceOrder> listAll(final UUID hotelId, final Pageable pageable) {
        return repository.findAllByHotelId(hotelId, pageable);
    }

    /**
     * Counts tenant-scoped orders for each distinct customer identifier supplied.
     *
     * @param hotelId tenant identifier
     * @param customerIds customer identifiers to count
     * @return counts for matching customers
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrderCount> customerOrderCounts(
            final UUID hotelId, final List<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }
        return repository
                .countOrdersForCustomers(hotelId, customerIds.stream().distinct().toList())
                .stream()
                .map(row -> new CustomerOrderCount((UUID) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    /**
     * Records customer authorization of a quote and appends the corresponding status history.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor authorizing customer identifier
     * @param request authorization evidence
     * @return authorized order
     * @implSpec Overrides must preserve quote-state and authorization invariants.
     */
    @Transactional
    public ServiceOrder authorize(
            final UUID hotelId,
            final UUID id,
            final UUID actor,
            final AuthorizationRequest request) {
        final ServiceOrder order = get(hotelId, id);
        if (order.getStatus() != ServiceOrderStatus.QUOTED) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_QUOTED");
        }
        order.setStatus(ServiceOrderStatus.AUTHORIZED);
        order.setAuthorizedBy(actor);
        order.setAuthorizationEvidence(request.evidence().trim());
        order.setAuthorizedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        final ServiceOrder saved = repository.save(order);
        record(
                saved,
                ServiceOrderStatus.QUOTED,
                ServiceOrderStatus.AUTHORIZED,
                actor.toString(),
                request.evidence());
        return saved;
    }

    /**
     * Records a customer's rejection of a quote without changing its financial details.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor rejecting customer identifier
     * @param request rejection evidence
     * @return rejected order
     * @implSpec Overrides must preserve quote-state and rejection invariants.
     */
    @Transactional
    public ServiceOrder reject(
            final UUID hotelId,
            final UUID id,
            final UUID actor,
            final AuthorizationRequest request) {
        final var order = get(hotelId, id);
        if (order.getStatus() != ServiceOrderStatus.QUOTED) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_QUOTED");
        }
        order.setStatus(ServiceOrderStatus.QUOTE_REJECTED);
        order.setAuthorizationEvidence(request.evidence().trim());
        order.setAuthorizedBy(actor);
        order.setAuthorizedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        final var saved = repository.save(order);
        record(
                saved,
                ServiceOrderStatus.QUOTED,
                ServiceOrderStatus.QUOTE_REJECTED,
                actor.toString(),
                request.evidence());
        return saved;
    }

    /**
     * Calculates and persists an order quote, applying the tenant's configured tax rate.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param request quotation details
     * @return quoted order
     * @implSpec Overrides must preserve quote-state and tax calculation behavior.
     */
    @Transactional
    public ServiceOrder quote(
            final UUID hotelId, final UUID id, final QuotationUpdateRequest request) {
        final ServiceOrder order = get(hotelId, id);
        if (order.getStatus() != ServiceOrderStatus.DIAGNOSIS
                && order.getStatus() != ServiceOrderStatus.REVIEW) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_DIAGNOSED");
        }
        order.setProposedSolution(request.proposedSolution());
        order.setEstimatedMinutes(request.estimatedMinutes());
        order.setLaborAmount(request.laborAmount());
        order.setPartsAmount(request.partsAmount());
        final var rate =
                hotelSettingsRepository
                        .findById(hotelId)
                        .map(HotelSettings::getDefaultTaxRate)
                        .orElse(null);
        final var tax =
                rate == null
                        ? request.taxAmount()
                        : request.laborAmount()
                                .add(request.partsAmount())
                                .multiply(rate)
                                .divide(
                                        new BigDecimal("100"),
                                        2,
                                        RoundingMode.HALF_UP);
        order.setTaxAmount(tax);
        order.setQuoteTotal(request.laborAmount().add(request.partsAmount()).add(tax));
        final BigDecimal received =
                order.getPaidAmount() == null
                        ? zero(order.getDepositAmount())
                        : order.getPaidAmount();
        order.setPaymentStatus(
                received.compareTo(order.getQuoteTotal()) >= 0 ? PAYMENT_PAID : PAYMENT_PENDING);
        final var previous = order.getStatus();
        order.setStatus(ServiceOrderStatus.QUOTED);
        order.setUpdatedAt(LocalDateTime.now());
        final ServiceOrder saved = repository.save(order);
        record(saved, previous, ServiceOrderStatus.QUOTED, OPERATOR_ACTOR, "Cotización generada");
        return saved;
    }

    /**
     * Adds a repair test and moves an in-progress order into quality assurance on first test.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param request test details
     * @return persisted repair test
     * @implSpec Overrides must preserve quality-assurance transition behavior.
     */
    @Transactional
    public RepairTest addTest(final UUID hotelId, final UUID id, final RepairTestRequest request) {
        final ServiceOrder order = get(hotelId, id);
        if (order.getStatus() != ServiceOrderStatus.IN_PROGRESS
                && order.getStatus() != ServiceOrderStatus.QA) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_TESTABLE");
        }
        if (order.getStatus() == ServiceOrderStatus.IN_PROGRESS) {
            order.setStatus(ServiceOrderStatus.QA);
            order.setUpdatedAt(LocalDateTime.now());
            repository.save(order);
            record(
                    order,
                    ServiceOrderStatus.IN_PROGRESS,
                    ServiceOrderStatus.QA,
                OPERATOR_ACTOR,
                    "Pruebas iniciadas");
            notificationOutbox.enqueue(
                    hotelId,
                    "ORDER_READY",
                    "Orden lista para control de calidad",
                    ORDER_LABEL + order.getId() + " está lista para control de calidad.",
                    "ORDER_READY:" + order.getId() + ":" + UUID.randomUUID());
        }
        return repairTests.save(
                RepairTest.builder()
                        .hotelId(hotelId)
                        .serviceOrderId(id)
                        .testName(request.testName().trim())
                        .passed(request.passed())
                        .notes(request.notes())
                        .technicianId(request.technicianId())
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    /**
     * Lists repair tests after verifying that the order belongs to the tenant.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @return chronological repair tests
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<RepairTest> tests(final UUID hotelId, final UUID id) {
        get(hotelId, id);
        return repairTests.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId, id);
    }

    /**
     * Lists the chronological status and activity history for a tenant-owned order.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @return chronological history entries
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<ServiceOrderStatusHistory> history(final UUID hotelId, final UUID id) {
        get(hotelId, id);
        return history.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId, id);
    }

    /**
     * Appends an internal note without changing the order's current status.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor note author
     * @param note note contents
     * @return persisted history entry
     * @implSpec Overrides must preserve tenant ownership and immutable audit history.
     */
    @Transactional
    public ServiceOrderStatusHistory addInternalNote(
            final UUID hotelId, final UUID id, final String actor, final String note) {
        final ServiceOrder order = get(hotelId, id);
        final String normalized = note == null ? "" : note.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("SERVICE_ORDER_NOTE_REQUIRED");
        }
        return history.save(
                ServiceOrderStatusHistory.builder()
                        .hotelId(hotelId)
                        .serviceOrderId(id)
                        .fromStatus(order.getStatus())
                        .toStatus(order.getStatus())
                        .actor(actor)
                        .notes(normalized)
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    /**
     * Delivers an order after quality checks, recording payment or a valid outstanding balance.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor delivering user identifier
     * @param paymentMethod payment method or {@code PENDING}
     * @return delivered order
     * @implSpec Overrides must preserve quality, inventory, and payment invariants.
     */
    @Transactional
    public ServiceOrder deliver(
            final UUID hotelId, final UUID id, final UUID actor, final String paymentMethod) {
        final ServiceOrder order =
                repository
                        .findForUpdateByIdAndHotelId(id, hotelId)
                        .orElseThrow(() -> new NotFoundException(SERVICE_ORDER_NOT_FOUND));
        if (order.getStatus() == ServiceOrderStatus.COMPLETED && order.getDeliveredAt() != null) {
            if (paymentMethod != null && paymentMethod.equals(order.getPaymentMethod())) {
                return order;
            }
            throw new IllegalStateException("SERVICE_ORDER_ALREADY_DELIVERED");
        }
        if (order.getStatus() != ServiceOrderStatus.QA) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_READY_FOR_DELIVERY");
        }
        if (hasFailedLatestRepairTest(hotelId, id)) {
            throw new IllegalStateException("REPAIR_TESTS_FAILED");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("PAYMENT_METHOD_REQUIRED");
        }
        final BigDecimal received =
                order.getPaidAmount() == null
                        ? zero(order.getDepositAmount())
                        : order.getPaidAmount();
        final BigDecimal outstanding =
                order.getQuoteTotal() == null
                        ? BigDecimal.ZERO
                        : order.getQuoteTotal().subtract(received);
        if (PAYMENT_PENDING.equals(paymentMethod)
                && outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("SERVICE_ORDER_HAS_NO_OUTSTANDING_BALANCE");
        }
        inventoryService.consumeReservations(hotelId, id);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(
                PAYMENT_PENDING.equals(paymentMethod) ? PAYMENT_PENDING : PAYMENT_PAID);
        final BigDecimal receivedOnDelivery =
                !PAYMENT_PENDING.equals(paymentMethod)
                        ? outstanding.max(BigDecimal.ZERO)
                        : BigDecimal.ZERO;
        if (!PAYMENT_PENDING.equals(paymentMethod) && order.getQuoteTotal() != null) {
            order.setPaidAmount(received.max(order.getQuoteTotal()));
        } else {
            order.setPaidAmount(received);
        }
        order.setStatus(ServiceOrderStatus.COMPLETED);
        order.setDeliveredAt(LocalDateTime.now());
        order.setDeliveredBy(actor);
        order.setUpdatedAt(LocalDateTime.now());
        if (receivedOnDelivery.compareTo(BigDecimal.ZERO) > 0) {
            appendPaymentEvent(
                    order, receivedOnDelivery, paymentMethod, "DELIVERY", order.getDeliveredAt());
        }
        final ServiceOrder saved = repository.save(order);
        record(
                saved,
                ServiceOrderStatus.QA,
                ServiceOrderStatus.COMPLETED,
                actor.toString(),
                "Equipo entregado · pago "
                        + order.getPaymentStatus()
                        + " · recibido "
                        + receivedOnDelivery.toPlainString()
                        + PAYMENT_METHOD_LABEL
                        + paymentMethod);
        if (PAYMENT_PENDING.equals(paymentMethod)
                && outstanding.compareTo(BigDecimal.ZERO) > 0) {
            notificationOutbox.enqueue(
                    hotelId,
                    "PAYMENT_DUE",
                    "Saldo pendiente de una orden entregada",
                    ORDER_LABEL
                            + saved.getId()
                            + " se entregó con un saldo pendiente de "
                            + outstanding.toPlainString()
                            + ". Registra el pago cuando se reciba.",
                    "PAYMENT_DUE:" + saved.getId() + ":" + UUID.randomUUID());
        }
        return saved;
    }

    private boolean hasFailedLatestRepairTest(final UUID hotelId, final UUID orderId) {
        final var latestByTest = new LinkedHashMap<String, RepairTest>();
        repairTests
                .findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotelId, orderId)
                .forEach(
                        test ->
                                latestByTest.put(
                                        test.getTestName().trim().toLowerCase(Locale.ROOT), test));
        return latestByTest.values().stream().anyMatch(test -> !test.isPassed());
    }

    /**
     * Records the remaining balance using the full outstanding amount.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor payment recorder identifier
     * @param paymentMethod received payment method
     * @return updated order
     * @implSpec Overrides must preserve tenant ownership and payment invariants.
     */
    @Transactional
    public ServiceOrder recordPendingPayment(
            final UUID hotelId, final UUID id, final UUID actor, final String paymentMethod) {
        return recordPendingPayment(hotelId, id, actor, paymentMethod, null);
    }

    /**
     * Records a requested amount against the outstanding balance.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor payment recorder identifier
     * @param paymentMethod received payment method
     * @param requestedAmount amount received, or {@code null} for the full balance
     * @return updated order
     * @implSpec Overrides must preserve tenant ownership and payment invariants.
     */
    @Transactional
    public ServiceOrder recordPendingPayment(
            final UUID hotelId,
            final UUID id,
            final UUID actor,
            final String paymentMethod,
            final BigDecimal requestedAmount) {
        return recordPendingPayment(hotelId, id, actor, paymentMethod, requestedAmount, null);
    }

    /**
     * Records an idempotent balance payment and updates the order's cumulative paid amount.
     *
     * @param hotelId tenant identifier
     * @param id repair-order identifier
     * @param actor payment recorder identifier
     * @param paymentMethod received payment method
     * @param requestedAmount amount received, or {@code null} for the full balance
     * @param requestId optional idempotency key
     * @return updated order
     * @implSpec Overrides must preserve tenant ownership, payment invariants, and idempotency.
     */
    @Transactional
    public ServiceOrder recordPendingPayment(
            final UUID hotelId,
            final UUID id,
            final UUID actor,
            final String paymentMethod,
            final BigDecimal requestedAmount,
            final UUID requestId) {
        final ServiceOrder order =
                repository
                        .findForUpdateByIdAndHotelId(id, hotelId)
                        .orElseThrow(() -> new NotFoundException(SERVICE_ORDER_NOT_FOUND));
        final String requestFingerprint =
                paymentMethod
                        + "|"
                        + (requestedAmount == null
                                ? "AUTO"
                                : requestedAmount.stripTrailingZeros().toPlainString());
        if (requestId != null && order.getPaymentEvents() != null) {
            final var previous =
                    order.getPaymentEvents().stream()
                            .filter(event -> requestId.toString().equals(event.get("requestId")))
                            .findFirst();
            if (previous.isPresent()) {
                if (requestFingerprint.equals(previous.get().get("requestFingerprint"))) {
                    return order;
                }
                throw new IllegalArgumentException("SERVICE_ORDER_PAYMENT_REQUEST_ID_REUSED");
            }
        }
        if (order.getStatus() != ServiceOrderStatus.COMPLETED) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_DELIVERED");
        }
        if (!PAYMENT_PENDING.equals(order.getPaymentStatus())) {
            throw new IllegalStateException("SERVICE_ORDER_PAYMENT_NOT_PENDING");
        }
        if (paymentMethod == null
                || paymentMethod.isBlank()
                || PAYMENT_PENDING.equals(paymentMethod)) {
            throw new IllegalArgumentException("VALID_PAYMENT_METHOD_REQUIRED");
        }
        if (order.getQuoteTotal() == null
                || order.getQuoteTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("SERVICE_ORDER_HAS_NO_PAYABLE_TOTAL");
        }
        final BigDecimal previousPaid =
                order.getPaidAmount() == null
                        ? zero(order.getDepositAmount())
                        : order.getPaidAmount();
        final BigDecimal outstanding =
                order.getQuoteTotal().subtract(previousPaid).max(BigDecimal.ZERO);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("SERVICE_ORDER_HAS_NO_OUTSTANDING_BALANCE");
        }
        final BigDecimal received = requestedAmount == null ? outstanding : requestedAmount;
        if (received.compareTo(BigDecimal.ZERO) <= 0 || received.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException(
                    "PAYMENT_AMOUNT_MUST_BE_POSITIVE_AND_NOT_EXCEED_BALANCE");
        }
        final BigDecimal cumulativePaid = previousPaid.add(received);
        order.setPaidAmount(cumulativePaid);
        appendPaymentEvent(
                order,
                received,
                paymentMethod,
                "BALANCE",
                LocalDateTime.now(),
                requestId,
                requestFingerprint);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(
                cumulativePaid.compareTo(order.getQuoteTotal()) >= 0
                        ? PAYMENT_PAID
                        : PAYMENT_PENDING);
        order.setUpdatedAt(LocalDateTime.now());
        final ServiceOrder saved = repository.save(order);
        record(
                saved,
                ServiceOrderStatus.COMPLETED,
                ServiceOrderStatus.COMPLETED,
                actor.toString(),
                "Pago recibido "
                        + received.toPlainString()
                        + PAYMENT_METHOD_LABEL
                        + paymentMethod
                        + " · acumulado "
                        + cumulativePaid.toPlainString()
                        + " de "
                        + order.getQuoteTotal().toPlainString());
        return saved;
    }

    private BigDecimal zero(final BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void appendPaymentEvent(
            final ServiceOrder order,
            final BigDecimal amount,
            final String paymentMethod,
            final String kind,
            final LocalDateTime recordedAt) {
        appendPaymentEvent(order, amount, paymentMethod, kind, recordedAt, null, null);
    }

    private void appendPaymentEvent(
            final ServiceOrder order,
            final BigDecimal amount,
            final String paymentMethod,
            final String kind,
            final LocalDateTime recordedAt,
            final UUID requestId,
            final String requestFingerprint) {
        final var events =
                order.getPaymentEvents() == null
                        ? new ArrayList<Map<String, String>>()
                        : new ArrayList<>(order.getPaymentEvents());
        final var event = new HashMap<String, String>();
        event.put("amount", amount.toPlainString());
        event.put("method", paymentMethod);
        event.put("kind", kind);
        event.put("recordedAt", recordedAt.toString());
        if (requestId != null) {
            event.put("requestId", requestId.toString());
            event.put("requestFingerprint", requestFingerprint);
        }
        events.add(event);
        order.setPaymentEvents(events);
    }

    private void record(
            final ServiceOrder o,
            final ServiceOrderStatus from,
            final ServiceOrderStatus to,
            final String actor,
            final String notes) {
        history.save(
                ServiceOrderStatusHistory.builder()
                        .hotelId(o.getHotelId())
                        .serviceOrderId(o.getId())
                        .fromStatus(from)
                        .toStatus(to)
                        .actor(actor)
                        .notes(notes)
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    /**
     * A configured workflow state exposed to repair-order clients.
     *
     * @param workflowId workflow identifier
     * @param stateKey stable state key
     * @param label human-readable state label
     */
    public record ConfiguredWorkflowStatus(String workflowId, String stateKey, String label) { }

    /**
     * A permitted transition between two configured workflow states.
     *
     * @param key stable transition key
     * @param fromStateKey source state key
     * @param toStateKey destination state key
     */
    public record ConfiguredWorkflowTransition(
            String key, String fromStateKey, String toStateKey) { }

    /**
     * Number of repair orders currently associated with a customer.
     *
     * @param customerId customer identifier
     * @param count number of orders
     */
    public record CustomerOrderCount(UUID customerId, long count) { }

    /**
     * Active configured workflow and its currently available transitions.
     *
     * @param id workflow identifier
     * @param name workflow display name
     * @param currentState current aggregate state key
     * @param initialState configured initial state key
     * @param transitions active transitions
     */
    public record ConfiguredWorkflowView(
            String id,
            String name,
            String currentState,
            String initialState,
            List<ConfiguredWorkflowTransition> transitions) {
        public ConfiguredWorkflowView {
            transitions = ImmutableCopies.list(transitions);
        }
    }
}
