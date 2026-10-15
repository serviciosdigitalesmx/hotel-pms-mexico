package com.hotelpms.frontdesk.serviceorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.client.CapabilityClient;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.notifications.OperationalNotificationOutboxService;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderRequest;
import com.hotelpms.frontdesk.serviceorder.repository.RepairTestRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderStatusHistoryRepository;
import com.hotelpms.frontdesk.serviceorder.service.ServiceOrderService;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionRepository;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ServiceOrderOperationalTest {
  private static final UUID HOTEL_ID = UUID.randomUUID();
  private static final UUID CUSTOMER_ID = UUID.randomUUID();
  private static final UUID ORDER_ID = UUID.randomUUID();

  @Mock private ServiceOrderRepository repository;
  @Mock private RepairTestRepository repairTests;
  @Mock private ServiceOrderStatusHistoryRepository history;
  @Mock private AuditEventService auditEvents;
  @Mock private WorkflowService workflows;
  @Mock private WorkflowTransitionRepository workflowTransitions;
  @Mock private CapabilityClient capabilityClient;
  @Mock private HotelSettingsRepository hotelSettingsRepository;
  @Mock private GuestClient guestClient;
  @Mock private BranchValidationService branchValidation;
  @Mock private InventoryService inventoryService;
  @Mock private OperationalNotificationOutboxService notificationOutbox;

  private ServiceOrderService service;

  @BeforeEach
  void setUp() {
    service = new ServiceOrderService(repository, repairTests, history, auditEvents, workflows,
        workflowTransitions, capabilityClient, hotelSettingsRepository, guestClient,
        branchValidation, inventoryService, notificationOutbox);
  }

  @Test
  void createsAnOrderWithRequiredOperationalDetailsAndHistory() {
    when(repository.save(any(ServiceOrder.class))).thenAnswer(call -> call.getArgument(0));
    final ServiceOrderRequest request = new ServiceOrderRequest(CUSTOMER_ID, null, null,
        "No enciende", "Equipo no enciende", "Pantalla y carcasa revisadas",
        null, null, "NORMAL", "Diagnóstico", BigDecimal.ZERO, null);

    final ServiceOrder created = service.create(HOTEL_ID, request);

    assertEquals(CUSTOMER_ID, created.getCustomerId());
    assertEquals(ServiceOrderStatus.INTAKE, created.getStatus());
    assertEquals("Diagnóstico", created.getServiceName());
    assertNotNull(created.getCreatedAt());
    verify(history).save(any());
    verify(auditEvents).record(eq(HOTEL_ID), eq(null), any(),
        eq("SERVICE_ORDER_CREATED"), eq(created.getId()), eq(null), anyString());
  }

  @Test
  void deliveryWithDeferredPaymentQueuesBusinessAlertAndKeepsBalanceOpen() {
    when(repository.save(any(ServiceOrder.class))).thenAnswer(call -> call.getArgument(0));
    final ServiceOrder order = ServiceOrder.builder().id(ORDER_ID).hotelId(HOTEL_ID)
        .status(ServiceOrderStatus.QA).quoteTotal(new BigDecimal("100.00"))
        .depositAmount(new BigDecimal("30.00")).paidAmount(new BigDecimal("30.00")).build();
    when(repository.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID)).thenReturn(java.util.Optional.of(order));

    final ServiceOrder delivered = service.deliver(HOTEL_ID, ORDER_ID, UUID.randomUUID(), "PENDING");

    assertEquals(ServiceOrderStatus.COMPLETED, delivered.getStatus());
    assertEquals("PENDING", delivered.getPaymentStatus());
    assertEquals(new BigDecimal("30.00"), delivered.getPaidAmount());
    assertNotNull(delivered.getDeliveredAt());
    verify(inventoryService).consumeReservations(HOTEL_ID, ORDER_ID);
    verify(notificationOutbox).enqueue(eq(HOTEL_ID), eq("PAYMENT_DUE"), anyString(),
        anyString(), anyString());
  }

  @Test
  void retryingTheSameDeliveryReturnsTheCompletedOrderWithoutRepeatingSideEffects() {
    final ServiceOrder order = ServiceOrder.builder().id(ORDER_ID).hotelId(HOTEL_ID)
        .status(ServiceOrderStatus.COMPLETED).paymentStatus("PENDING").paymentMethod("PENDING")
        .quoteTotal(new BigDecimal("100.00")).paidAmount(new BigDecimal("30.00"))
        .deliveredAt(java.time.LocalDateTime.now()).build();
    when(repository.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID))
        .thenReturn(java.util.Optional.of(order));

    final ServiceOrder retry = service.deliver(HOTEL_ID, ORDER_ID, UUID.randomUUID(), "PENDING");

    assertEquals(order, retry);
    verify(inventoryService, never()).consumeReservations(HOTEL_ID, ORDER_ID);
    verify(notificationOutbox, never()).enqueue(any(), anyString(), anyString(), anyString(), anyString());
    verify(history, never()).save(any());
  }

  @Test
  void retryingDeliveryWithDifferentPaymentMethodIsRejected() {
    final ServiceOrder order = ServiceOrder.builder().id(ORDER_ID).hotelId(HOTEL_ID)
        .status(ServiceOrderStatus.COMPLETED).paymentStatus("PENDING").paymentMethod("PENDING")
        .deliveredAt(java.time.LocalDateTime.now()).build();
    when(repository.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID))
        .thenReturn(java.util.Optional.of(order));

    assertThrows(IllegalStateException.class,
        () -> service.deliver(HOTEL_ID, ORDER_ID, UUID.randomUUID(), "CASH"));

    verify(inventoryService, never()).consumeReservations(HOTEL_ID, ORDER_ID);
    verify(notificationOutbox, never()).enqueue(any(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void recordsTheRemainingAmountAndClosesPaymentStatus() {
    when(repository.save(any(ServiceOrder.class))).thenAnswer(call -> call.getArgument(0));
    final ServiceOrder order = ServiceOrder.builder().id(ORDER_ID).hotelId(HOTEL_ID)
        .status(ServiceOrderStatus.COMPLETED).paymentStatus("PENDING")
        .quoteTotal(new BigDecimal("100.00")).paidAmount(new BigDecimal("30.00")).build();
    when(repository.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID)).thenReturn(java.util.Optional.of(order));

    final ServiceOrder updated = service.recordPendingPayment(HOTEL_ID, ORDER_ID, UUID.randomUUID(),
        "CASH", new BigDecimal("70.00"));

    assertEquals("PAID", updated.getPaymentStatus());
    assertEquals(new BigDecimal("100.00"), updated.getPaidAmount());
    verify(history).save(any());
  }
}
