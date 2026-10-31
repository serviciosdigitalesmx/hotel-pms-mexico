package com.hotelpms.frontdesk.serviceorder.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Resolves Fixi technician identity and assignment strictly from authenticated server context. */
@Component
@RequiredArgsConstructor
public final class FixiAssignedOrderAccess {
  private final JdbcTemplate jdbc;

  /**
   * Resolves the active technician's database identity from the authenticated username and tenant.
   *
   * @param username authenticated username
   * @param tenantId authenticated tenant identifier
   * @return the single active technician identifier
   * @throws AccessDeniedException if the identity is absent or ambiguous
   */
  public UUID technicianId(final String username, final UUID tenantId) {
    final List<UUID> ids = jdbc.query(
        "select id from user_account where username = ? and hotel_id = ? and active = true and role = 'TECHNICIAN'",
        (rs, row) -> rs.getObject("id", UUID.class), username, tenantId);
    if (ids.size() != 1) {
      throw new AccessDeniedException("Technician identity unavailable");
    }
    return ids.getFirst();
  }

  /**
   * Returns active branch memberships for the authenticated user in the tenant.
   *
   * @param username authenticated username
   * @param tenantId authenticated tenant identifier
   * @return active branch identifiers
   */
  public List<UUID> branchIds(final String username, final UUID tenantId) {
    return jdbc.query(
        "select m.branch_id from user_branch_membership m join user_account u on u.id = m.user_id "
            + "join tenant_branch b on b.id = m.branch_id and b.hotel_id = m.hotel_id and b.active = true "
            + "where u.username = ? and u.hotel_id = ? and u.active = true and m.hotel_id = u.hotel_id",
        (rs, row) -> rs.getObject("branch_id", UUID.class), username, tenantId);
  }

  /**
   * Checks whether the authenticated user may access a branch in the tenant.
   *
   * @param username authenticated username
   * @param tenantId authenticated tenant identifier
   * @param branchId requested branch identifier
   * @return whether the user belongs to the tenant and may access the branch
   */
  public boolean canAccessBranch(final String username, final UUID tenantId, final UUID branchId) {
    if (username == null || tenantId == null || branchId == null) {
      return false;
    }
    final List<Boolean> rows = jdbc.query(
        "select exists (select 1 from user_account u where u.username = ? and u.hotel_id = ? "
            + "and u.active = true and (u.role in ('OWNER','ADMIN') or exists (select 1 "
            + "from user_branch_membership m join tenant_branch b on b.id = m.branch_id "
            + "and b.hotel_id = m.hotel_id and b.active = true where m.user_id = u.id "
            + "and m.hotel_id = u.hotel_id and m.branch_id = ?))) as allowed",
        (rs, row) -> rs.getBoolean("allowed"), username, tenantId, branchId);
    return !rows.isEmpty() && rows.getFirst();
  }

  /**
   * Checks order access without revealing whether another tenant's or user's order exists.
   *
   * @param username authenticated username
   * @param tenantId authenticated tenant identifier
   * @param orderId requested order identifier
   * @return whether the authenticated user may access the order
   */
  public boolean canAccessOrder(
      final String username, final UUID tenantId, final UUID orderId) {
    return canAccessOrder(username, tenantId, orderId, null);
  }

  /**
   * Checks tenant, branch membership, selected branch context, and technician assignment.
   *
   * @param username authenticated username
   * @param tenantId authenticated tenant identifier
   * @param orderId requested order identifier
   * @param selectedBranchId optional server-validated branch context
   * @return whether the authenticated user may access the order
   */
  public boolean canAccessOrder(
      final String username, final UUID tenantId, final UUID orderId, final UUID selectedBranchId) {
    if (username == null || tenantId == null || orderId == null) {
      return false;
    }
    final String branchClause = selectedBranchId == null ? "" : " and o.branch_id = ?";
    final List<Object> arguments = new ArrayList<>(
        List.of(username, tenantId, orderId, tenantId));
    if (selectedBranchId != null) {
      arguments.add(selectedBranchId);
    }
    final List<Boolean> rows = jdbc.query(
        "select exists (select 1 from service_orders o join user_account u "
            + "on u.username = ? and u.hotel_id = ? and u.active = true "
            + "where o.id = ? and o.hotel_id = ? and "
            + "(u.role in ('OWNER','ADMIN') or exists (select 1 from user_branch_membership m "
            + "join tenant_branch b on b.id = m.branch_id and b.hotel_id = m.hotel_id and b.active = true "
            + "where m.user_id = u.id and m.hotel_id = u.hotel_id and m.branch_id = o.branch_id)) "
            + "and (u.role <> 'TECHNICIAN' or o.assigned_user_id = u.id)"
            + branchClause + ") as allowed",
        (rs, row) -> rs.getBoolean("allowed"), arguments.toArray());
    return !rows.isEmpty() && rows.getFirst();
  }

  /**
   * Resolves an evidence record's parent order within the tenant.
   *
   * @param tenantId authenticated tenant identifier
   * @param evidenceId evidence identifier
   * @return parent order identifier, or {@code null} when no tenant-owned evidence matches
   */
  public UUID evidenceOrderId(final UUID tenantId, final UUID evidenceId) {
    final List<UUID> ids = jdbc.query(
        "select service_order_id from document_evidence where id = ? and hotel_id = ?",
        (rs, row) -> rs.getObject("service_order_id", UUID.class), evidenceId, tenantId);
    return ids.isEmpty() ? null : ids.getFirst();
  }

  /**
   * Resolves a tenant-owned warranty claim's parent order for assignment checks.
   *
   * @param tenantId authenticated tenant identifier
   * @param claimId warranty claim identifier
   * @return parent order identifier, or {@code null} when no tenant-owned claim matches
   */
  public UUID warrantyOrderId(final UUID tenantId, final UUID claimId) {
    final List<UUID> ids = jdbc.query(
        "select service_order_id from warranty_claims where id = ? and hotel_id = ?",
        (rs, row) -> rs.getObject("service_order_id", UUID.class), claimId, tenantId);
    return ids.isEmpty() ? null : ids.getFirst();
  }
}
