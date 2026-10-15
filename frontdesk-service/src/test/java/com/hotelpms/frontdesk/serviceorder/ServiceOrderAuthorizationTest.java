package com.hotelpms.frontdesk.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.repository.RepairTestRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderStatusHistoryRepository;
import com.hotelpms.frontdesk.serviceorder.service.ServiceOrderService;
import java.util.UUID;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceOrderAuthorizationTest {
  @Mock private ServiceOrderRepository repository;
  @Mock private RepairTestRepository repairTests;
  @Mock private ServiceOrderStatusHistoryRepository history;

  @Test
  void reauthorizesAQuotedOrderAfterAQuoteRevision() {
    final UUID hotelId = UUID.randomUUID();
    final UUID orderId = UUID.randomUUID();
    final UUID firstActor = UUID.randomUUID();
    final UUID secondActor = UUID.randomUUID();
    final ServiceOrder order = ServiceOrder.builder()
        .id(orderId)
        .hotelId(hotelId)
        .status(ServiceOrderStatus.QUOTED)
        .build();
    when(repository.findByIdAndHotelId(orderId, hotelId)).thenReturn(Optional.of(order));
    when(repository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    final ServiceOrderService service = new ServiceOrderService(repository, repairTests, history,
        org.mockito.Mockito.mock(com.hotelpms.frontdesk.audit.service.AuditEventService.class),
        null, null, null, null, null, null, null);

    service.authorize(hotelId, orderId, firstActor, new AuthorizationRequest("first"));
    order.setStatus(ServiceOrderStatus.QUOTED);
    final ServiceOrder reauthorized = service.authorize(
        hotelId, orderId, secondActor, new AuthorizationRequest("revised quote"));

    assertThat(reauthorized.getStatus()).isEqualTo(ServiceOrderStatus.AUTHORIZED);
    assertThat(reauthorized.getAuthorizedBy()).isEqualTo(secondActor);
    assertThat(reauthorized.getAuthorizationEvidence()).isEqualTo("revised quote");
  }
}
