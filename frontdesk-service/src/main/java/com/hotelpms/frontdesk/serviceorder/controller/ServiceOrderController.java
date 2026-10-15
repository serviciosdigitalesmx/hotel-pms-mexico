package com.hotelpms.frontdesk.serviceorder.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.QuotationUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderDeliveryRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderPaymentUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.RepairTestRequest;
import com.hotelpms.frontdesk.serviceorder.service.ServiceOrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/service-orders")
public class ServiceOrderController {
  private final ServiceOrderService service;

  @GetMapping("/{id}/configured-workflow")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public ServiceOrderService.ConfiguredWorkflowView configuredWorkflow(@PathVariable final UUID id) {
    return service.configuredWorkflow(hotel(), id);
  }

  @PostMapping("/{id}/configured-workflow/transitions/{transitionKey}")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public ServiceOrderService.ConfiguredWorkflowView advanceConfiguredWorkflow(
      @PathVariable final UUID id, @PathVariable final String transitionKey) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return service.advanceConfiguredWorkflow(hotel(), id, transitionKey,
        authentication.getName(), authentication.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority).toList());
  }

  /**
   * Creates a service order within the active tenant.
   *
   * @param r service order request
   * @return created service order response
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
  public ResponseEntity<ServiceOrder> create(@Valid @RequestBody final ServiceOrderRequest r) {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(service.create(hotel(), r, authentication.getName()));
  }

  /**
   * Gets one service order.
   *
   * @param id service order identifier
   * @return the service order
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
  public ServiceOrder get(@PathVariable final UUID id) {
    return service.get(hotel(), id);
  }

  /**
   * Lists all service orders for the active tenant.
   *
   * @return service orders
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
  public ResponseEntity<?> listAll(
      @RequestParam(required = false) final Integer page,
      @RequestParam(required = false) final Integer size) {
    if (page == null && size == null) return ResponseEntity.ok(service.listAll(hotel()));
    final int safePage = Math.max(0, page == null ? 0 : page);
    final int safeSize = Math.min(200, Math.max(1, size == null ? 50 : size));
    final Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    return ResponseEntity.ok(service.listAll(hotel(), pageable));
  }

  /**
   * Lists service orders for a customer.
   *
   * @param customerId customer identifier
   * @return matching service orders
   */
  @GetMapping("/customer/{customerId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
  public List<ServiceOrder> list(@PathVariable final UUID customerId) {
    return service.list(hotel(), customerId);
  }

  @GetMapping("/customer-counts")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<ServiceOrderService.CustomerOrderCount> customerOrderCounts(
      @RequestParam final List<UUID> customerIds) {
    return service.customerOrderCounts(hotel(), customerIds);
  }

  /**
   * Updates a service order.
   *
   * @param id service order identifier
   * @param r update request
   * @return the updated service order
   */
  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'TECHNICIAN')")
  public ServiceOrder update(
      @PathVariable final UUID id, @Valid @RequestBody final ServiceOrderUpdateRequest r) {
    return service.update(hotel(), id, r);
  }

  /**
   * Records customer authorization for a service order.
   *
   * @param id service order identifier
   * @param r authorization request
   * @return the authorized service order
   */
  @PostMapping("/{id}/authorize")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
  public ServiceOrder authorize(@PathVariable final UUID id, @Valid @RequestBody final AuthorizationRequest r) {
    return service.authorize(
        hotel(),
        id,
        UUID.nameUUIDFromBytes(
            SecurityContextHolder.getContext().getAuthentication().getName()
                .getBytes(StandardCharsets.UTF_8)),
        r);
  }
  @PostMapping("/{id}/reject") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
  public ServiceOrder reject(@PathVariable UUID id,@Valid @RequestBody AuthorizationRequest r){return service.reject(hotel(),id,UUID.nameUUIDFromBytes(SecurityContextHolder.getContext().getAuthentication().getName().getBytes(StandardCharsets.UTF_8)),r);}

  @PostMapping("/{id}/quotation") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
  public ServiceOrder quote(@PathVariable UUID id,@Valid @RequestBody QuotationUpdateRequest r){return service.quote(hotel(),id,r);}
  @PostMapping("/{id}/tests") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
  public com.hotelpms.frontdesk.serviceorder.domain.RepairTest addTest(@PathVariable UUID id,@Valid @RequestBody RepairTestRequest r){return service.addTest(hotel(),id,r);}
  @GetMapping("/{id}/tests") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public java.util.List<com.hotelpms.frontdesk.serviceorder.domain.RepairTest> tests(@PathVariable UUID id){return service.tests(hotel(),id);}
  @GetMapping("/{id}/timeline") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public java.util.List<com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory> timeline(@PathVariable UUID id){return service.history(hotel(),id);}
  @PostMapping("/{id}/notes")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public ResponseEntity<com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory> addInternalNote(
      @PathVariable final UUID id, @Valid @RequestBody final InternalNoteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.addInternalNote(hotel(), id,
        SecurityContextHolder.getContext().getAuthentication().getName(), request.note()));
  }

  public record InternalNoteRequest(
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 2000) String note) {}

  @PostMapping("/{id}/deliver") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
  public ServiceOrder deliver(@PathVariable UUID id,@Valid @RequestBody ServiceOrderDeliveryRequest request){return service.deliver(hotel(),id,UUID.nameUUIDFromBytes(SecurityContextHolder.getContext().getAuthentication().getName().getBytes(StandardCharsets.UTF_8)),request.paymentMethod());}

  @PatchMapping("/{id}/payment") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
  public ServiceOrder recordPendingPayment(@PathVariable UUID id,@Valid @RequestBody ServiceOrderPaymentUpdateRequest request){return service.recordPendingPayment(hotel(),id,UUID.nameUUIDFromBytes(SecurityContextHolder.getContext().getAuthentication().getName().getBytes(StandardCharsets.UTF_8)),request.paymentMethod(),request.amount(),request.requestId());}

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
