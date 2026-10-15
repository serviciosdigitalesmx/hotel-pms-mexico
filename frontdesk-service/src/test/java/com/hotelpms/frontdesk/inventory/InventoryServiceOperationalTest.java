package com.hotelpms.frontdesk.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.domain.PosInventoryConsumption;
import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReservationRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.StockCountRequest;
import com.hotelpms.frontdesk.inventory.repository.InventoryBalanceRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
import com.hotelpms.frontdesk.inventory.repository.PosInventoryConsumptionRepository;
import com.hotelpms.frontdesk.inventory.repository.StockReservationRepository;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceOperationalTest {
  private static final UUID HOTEL_ID = UUID.randomUUID();
  private static final UUID BRANCH_ID = UUID.randomUUID();
  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final UUID ORDER_ID = UUID.randomUUID();
  private static final long STOCK_BEFORE_RELEASE = 3;
  private static final long STOCK_AFTER_RELEASE = 5;
  private static final long STOCK_BEFORE_POS_REPLAY = 5;

  @Mock private InventoryProductRepository products;
  @Mock private InventoryBalanceRepository balances;
  @Mock private AuditEventService auditEvents;
  @Mock private StockReservationRepository reservations;
  @Mock private SupplierRepository suppliers;
  @Mock private BranchValidationService branchValidation;
  @Mock private ServiceOrderRepository serviceOrders;
  @Mock private PosInventoryConsumptionRepository posConsumptions;

  private InventoryService service;

  @BeforeEach
  void setUp() {
    service = new InventoryService(products, balances, auditEvents, reservations, suppliers,
        branchValidation, serviceOrders, posConsumptions);
  }

  @Test
  void movementUpdatesOnlyTenantProductBalanceAndRecordsAudit() {
    final InventoryProduct product = InventoryProduct.builder()
        .id(PRODUCT_ID).hotelId(HOTEL_ID).build();
    final InventoryBalance balance = InventoryBalance.builder()
        .hotelId(HOTEL_ID).branchId(BRANCH_ID).productId(PRODUCT_ID).quantity(4).build();
    when(products.findForUpdateByIdAndHotelId(PRODUCT_ID, HOTEL_ID))
        .thenReturn(Optional.of(product));
    when(balances.findByHotelIdAndBranchIdAndProductId(HOTEL_ID, BRANCH_ID, PRODUCT_ID))
        .thenReturn(Optional.of(balance));
    when(balances.save(any(InventoryBalance.class))).thenReturn(balance);

    final InventoryBalance updated = service.move(HOTEL_ID,
        new MovementRequest(BRANCH_ID, PRODUCT_ID, -2, "Uso de reparación"));

    assertSame(balance, updated);
    assertEquals(2, updated.getQuantity());
    verify(branchValidation).requireActiveBranch(HOTEL_ID, BRANCH_ID);
    verify(auditEvents).record(eq(HOTEL_ID), eq(null), eq(AuditEvent.Category.INVENTORY),
        eq("INVENTORY_MOVEMENT_RECORDED"), eq(balance.getId()), eq(null), anyString());
  }

  @Test
  void stockCountRejectsAStaleExpectedQuantityWithoutChangingBalance() {
    final InventoryProduct product = InventoryProduct.builder()
        .id(PRODUCT_ID).hotelId(HOTEL_ID).build();
    final InventoryBalance balance = InventoryBalance.builder()
        .hotelId(HOTEL_ID).branchId(BRANCH_ID).productId(PRODUCT_ID).quantity(4).build();
    when(products.findForUpdateByIdAndHotelId(PRODUCT_ID, HOTEL_ID))
        .thenReturn(Optional.of(product));
    when(balances.findByHotelIdAndBranchIdAndProductId(HOTEL_ID, BRANCH_ID, PRODUCT_ID))
        .thenReturn(Optional.of(balance));

    assertThrows(ConflictException.class, () -> service.count(HOTEL_ID,
        new StockCountRequest(BRANCH_ID, PRODUCT_ID, 1, "Conteo físico", 3L)));

    assertEquals(4, balance.getQuantity());
    verify(balances, never()).save(any(InventoryBalance.class));
    verify(auditEvents, never()).record(any(UUID.class), any(UUID.class),
        any(AuditEvent.Category.class), anyString(), any(UUID.class), anyString(), anyString());
  }

  @Test
  void repeatedMatchingReservationDoesNotDeductStockTwice() {
    final ServiceOrder order = ServiceOrder.builder()
        .branchId(BRANCH_ID).status(ServiceOrderStatus.INTAKE).build();
    final StockReservation existing = StockReservation.builder()
        .hotelId(HOTEL_ID).branchId(BRANCH_ID).productId(PRODUCT_ID)
        .serviceOrderId(ORDER_ID).quantity(2).status("RESERVED").build();
    when(serviceOrders.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID))
        .thenReturn(Optional.of(order));
    when(products.findForUpdateByIdAndHotelId(PRODUCT_ID, HOTEL_ID))
        .thenReturn(Optional.of(InventoryProduct.builder().id(PRODUCT_ID).hotelId(HOTEL_ID).build()));
    when(reservations.findForUpdateByHotelIdAndServiceOrderIdAndProductId(
        HOTEL_ID, ORDER_ID, PRODUCT_ID)).thenReturn(Optional.of(existing));

    final StockReservation result = service.reserve(HOTEL_ID,
        new ReservationRequest(BRANCH_ID, PRODUCT_ID, ORDER_ID, 2));

    assertSame(existing, result);
    verify(balances, never()).save(any(InventoryBalance.class));
    verify(reservations, never()).save(any(StockReservation.class));
  }

  @Test
  void reservationRejectsAnOrderBelongingToAnotherBranch() {
    final ServiceOrder order = ServiceOrder.builder()
        .branchId(UUID.randomUUID()).status(ServiceOrderStatus.INTAKE).build();
    when(serviceOrders.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID))
        .thenReturn(Optional.of(order));

    assertThrows(IllegalArgumentException.class, () -> service.reserve(HOTEL_ID,
        new ReservationRequest(BRANCH_ID, PRODUCT_ID, ORDER_ID, 1)));

    verify(products, never()).findForUpdateByIdAndHotelId(any(UUID.class), any(UUID.class));
    verify(balances, never()).save(any(InventoryBalance.class));
    verify(reservations, never()).save(any(StockReservation.class));
  }

  @Test
  void releasingReservationRestoresStockAndMarksItReleased() {
    final InventoryBalance balance = InventoryBalance.builder()
        .hotelId(HOTEL_ID).branchId(BRANCH_ID).productId(PRODUCT_ID)
        .quantity(STOCK_BEFORE_RELEASE).build();
    final StockReservation reservation = StockReservation.builder()
        .hotelId(HOTEL_ID).branchId(BRANCH_ID).productId(PRODUCT_ID)
        .serviceOrderId(ORDER_ID).quantity(2).status("RESERVED").build();
    when(serviceOrders.findForUpdateByIdAndHotelId(ORDER_ID, HOTEL_ID))
        .thenReturn(Optional.of(ServiceOrder.builder().status(ServiceOrderStatus.INTAKE).build()));
    when(reservations.findForUpdateByHotelIdAndServiceOrderIdAndProductId(
        HOTEL_ID, ORDER_ID, PRODUCT_ID)).thenReturn(Optional.of(reservation));
    when(products.findForUpdateByIdAndHotelId(PRODUCT_ID, HOTEL_ID))
        .thenReturn(Optional.of(InventoryProduct.builder().id(PRODUCT_ID).hotelId(HOTEL_ID).build()));
    when(balances.findByHotelIdAndBranchIdAndProductId(HOTEL_ID, BRANCH_ID, PRODUCT_ID))
        .thenReturn(Optional.of(balance));
    when(reservations.save(reservation)).thenReturn(reservation);

    final StockReservation released = service.release(HOTEL_ID, ORDER_ID, PRODUCT_ID);

    assertEquals("RELEASED", released.getStatus());
    assertEquals(STOCK_AFTER_RELEASE, balance.getQuantity());
    verify(balances).save(balance);
    verify(auditEvents).record(eq(HOTEL_ID), eq(null), eq(AuditEvent.Category.INVENTORY),
        eq("SERVICE_ORDER_STOCK_RELEASED"), eq(reservation.getId()), eq(null), anyString());
  }

  @Test
  void replayingPosConsumptionDoesNotDeductStockAgain() {
    final InventoryProduct product = InventoryProduct.builder()
        .id(PRODUCT_ID).hotelId(HOTEL_ID).build();
    final InventoryBalance balance = InventoryBalance.builder()
        .hotelId(HOTEL_ID).branchId(BRANCH_ID).productId(PRODUCT_ID)
        .quantity(STOCK_BEFORE_POS_REPLAY).build();
    final PosInventoryConsumption prior = new PosInventoryConsumption();
    prior.setHotelId(HOTEL_ID);
    prior.setOrderId(ORDER_ID);
    prior.setProductId(PRODUCT_ID);
    prior.setBranchId(BRANCH_ID);
    prior.setQuantity(2);
    when(products.findByIdAndHotelId(PRODUCT_ID, HOTEL_ID)).thenReturn(Optional.of(product));
    when(posConsumptions.findByHotelIdAndOrderIdAndProductId(HOTEL_ID, ORDER_ID, PRODUCT_ID))
        .thenReturn(Optional.empty(), Optional.of(prior));
    when(products.findForUpdateByIdAndHotelId(PRODUCT_ID, HOTEL_ID))
        .thenReturn(Optional.of(product));
    when(balances.findByHotelIdAndBranchIdAndProductId(HOTEL_ID, BRANCH_ID, PRODUCT_ID))
        .thenReturn(Optional.of(balance));
    when(balances.save(any(InventoryBalance.class))).thenReturn(balance);

    final var lines = java.util.List.of(new InventoryService.PosConsumptionLine(PRODUCT_ID, 2));
    service.consumePosOrder(HOTEL_ID, ORDER_ID, BRANCH_ID, lines);
    service.consumePosOrder(HOTEL_ID, ORDER_ID, BRANCH_ID, lines);

    assertEquals(3, balance.getQuantity());
    verify(posConsumptions).saveAndFlush(any(PosInventoryConsumption.class));
    verify(balances).save(balance);
  }
}
