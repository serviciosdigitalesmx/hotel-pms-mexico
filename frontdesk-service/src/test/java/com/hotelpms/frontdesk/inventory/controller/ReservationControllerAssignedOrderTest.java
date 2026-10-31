package com.hotelpms.frontdesk.inventory.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReservationRequest;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.serviceorder.security.FixiAssignedOrderAccess;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationControllerAssignedOrderTest {
  private static final UUID TENANT_ID = UUID.randomUUID();
  private static final UUID ORDER_ID = UUID.randomUUID();
  private static final UUID BRANCH_ID = UUID.randomUUID();
  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final String TECHNICIAN_USERNAME = "technician-a";

  @Mock private InventoryService service;
  @Mock private FixiAssignedOrderAccess orderAccess;

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void technicianCannotReserveStockForAnOrderAssignedToAnotherTechnician() {
    final ReservationController controller = controller();
    authenticateTechnician();
    when(orderAccess.canAccessOrder(TECHNICIAN_USERNAME, TENANT_ID, ORDER_ID, null))
        .thenReturn(false);

    assertThatThrownBy(
            () -> controller.reserve(new ReservationRequest(BRANCH_ID, PRODUCT_ID, ORDER_ID, 1)))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("SERVICE_ORDER_NOT_FOUND");

    verify(orderAccess).canAccessOrder(TECHNICIAN_USERNAME, TENANT_ID, ORDER_ID, null);
    verify(service, never()).reserve(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  void reservationAuthorizationUsesAuthenticatedIdentityAndAllowsItsAssignedOrder() {
    final ReservationController controller = controller();
    authenticateTechnician();
    when(orderAccess.canAccessOrder(TECHNICIAN_USERNAME, TENANT_ID, ORDER_ID, null))
        .thenReturn(true);

    controller.reserve(new ReservationRequest(BRANCH_ID, PRODUCT_ID, ORDER_ID, 1));

    verify(orderAccess).canAccessOrder(TECHNICIAN_USERNAME, TENANT_ID, ORDER_ID, null);
    verify(service).reserve(TENANT_ID, new ReservationRequest(BRANCH_ID, PRODUCT_ID, ORDER_ID, 1));
  }

  private ReservationController controller() {
    final ReservationController controller = new ReservationController(service, orderAccess);
    ReflectionTestUtils.setField(controller, "assignedOrderScopeEnabled", true);
    return controller;
  }

  private static void authenticateTechnician() {
    final var authentication = new UsernamePasswordAuthenticationToken(
        TECHNICIAN_USERNAME, null, java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_TECHNICIAN")));
    authentication.setDetails(TENANT_ID.toString());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
