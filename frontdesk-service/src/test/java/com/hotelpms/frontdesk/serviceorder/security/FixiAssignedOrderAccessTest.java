package com.hotelpms.frontdesk.serviceorder.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class FixiAssignedOrderAccessTest {
  @Mock private JdbcTemplate jdbc;

  @Test
  void orderAccessQueryRequiresTenantMembershipAndTechnicianAssignment() {
    final String username = "tech-a";
    final UUID tenantId = UUID.randomUUID();
    final UUID orderId = UUID.randomUUID();
    when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class)))
        .thenReturn(List.of(false));
    final FixiAssignedOrderAccess access = new FixiAssignedOrderAccess(jdbc);

    assertThat(access.canAccessOrder(username, tenantId, orderId)).isFalse();

    final ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
    verify(jdbc).query(sql.capture(), any(RowMapper.class), any(Object[].class));
    assertThat(sql.getValue()).contains("o.hotel_id = ?", "user_branch_membership",
        "u.role <> 'TECHNICIAN' or o.assigned_user_id = u.id");
  }

  @Test
  void inventoryReservationReleasePathResolvesTheOrderId() {
    final FixiAssignedOrderWebConfiguration configuration =
        new FixiAssignedOrderWebConfiguration(new FixiAssignedOrderAccess(jdbc));
    final UUID orderId = UUID.randomUUID();
    final UUID productId = UUID.randomUUID();
    assertThat(configuration.orderIdFor(
        "/api/v1/inventory/reservations/" + orderId + "/" + productId + "/release",
        UUID.randomUUID()))
        .isEqualTo(orderId);
  }
}
