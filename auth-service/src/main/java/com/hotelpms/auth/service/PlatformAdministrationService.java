package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.PlatformAuditEvent;
import com.hotelpms.auth.domain.PlatformTenantSubscription;
import com.hotelpms.auth.domain.TenantPlan;
import com.hotelpms.auth.dto.AssignPlatformPlanRequest;
import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.dto.PlatformAuditSummary;
import com.hotelpms.auth.dto.PlatformDashboardSummary;
import com.hotelpms.auth.dto.PlatformPage;
import com.hotelpms.auth.dto.PlatformPlanSummary;
import com.hotelpms.auth.dto.PlatformSubscriptionSummary;
import com.hotelpms.auth.dto.PlatformTenantSummary;
import com.hotelpms.auth.dto.PlatformUserSummary;
import com.hotelpms.auth.dto.UpdatePlatformTenantStatusRequest;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.PlatformAuditEventRepository;
import com.hotelpms.auth.repository.PlatformTenantSubscriptionRepository;
import com.hotelpms.auth.repository.TenantPlanRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Platform-only operations with deliberately explicit cross-tenant SQL and audit writes. */
@Service
@RequiredArgsConstructor
public class PlatformAdministrationService {
  private static final String ACTIVE = "ACTIVE";
  private static final String SUSPENDED = "SUSPENDED";
  private static final int MAX_PAGE_SIZE = 100;

  private final JdbcTemplate jdbc;
  private final HotelRegistryRepository hotels;
  private final TenantPlanRepository plans;
  private final PlatformTenantSubscriptionRepository subscriptions;
  private final PlatformAuditEventRepository auditEvents;
  private final HotelOnboardingService onboarding;

  @Transactional(readOnly = true)
  public PlatformPage<PlatformTenantSummary> tenants(
      final String search, final String status, final int page, final int size) {
    final int safePage = Math.max(0, page);
    final int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
    final String query = normalized(search);
    final String state = normalized(status);
    final String where = " WHERE (CAST(? AS text) IS NULL OR lower(t.name) LIKE lower(?) OR lower(t.slug) LIKE lower(?))"
        + " AND (CAST(? AS text) IS NULL OR upper(t.status) = upper(?))";
    final Long count = jdbc.queryForObject(
        "SELECT count(*) FROM hotel_registry t" + where,
        Long.class, query, pattern(query), pattern(query), state, state);
    final List<PlatformTenantSummary> content = jdbc.query(
        """
            SELECT t.id, t.name, t.slug, t.status, t.created_at, t.onboarding_completed,
                   owner.username AS owner_username, owner.full_name AS owner_name,
                   owner.email AS owner_email, p.plan_key, COALESCE(p.active, false) AS plan_active
            FROM hotel_registry t
            LEFT JOIN LATERAL (
                SELECT u.username, u.full_name, u.email
                FROM user_account u
                WHERE u.hotel_id = t.id AND u.role = 'OWNER'
                ORDER BY u.active DESC, u.created_at ASC
                LIMIT 1
            ) owner ON TRUE
            LEFT JOIN tenant_plan p ON p.tenant_id = t.id
            """ + where + " ORDER BY t.created_at DESC, t.id LIMIT ? OFFSET ?",
        (rs, row) -> tenantSummary(rs),
        query, pattern(query), pattern(query), state, state, safeSize, (long) safePage * safeSize);
    return page(content, safePage, safeSize, count == null ? 0 : count);
  }

  @Transactional(readOnly = true)
  public PlatformTenantSummary tenant(final UUID tenantId) {
    return jdbc.query(
            """
                SELECT t.id, t.name, t.slug, t.status, t.created_at, t.onboarding_completed,
                       owner.username AS owner_username, owner.full_name AS owner_name,
                       owner.email AS owner_email, p.plan_key, COALESCE(p.active, false) AS plan_active
                FROM hotel_registry t
                LEFT JOIN LATERAL (
                    SELECT u.username, u.full_name, u.email
                    FROM user_account u
                    WHERE u.hotel_id = t.id AND u.role = 'OWNER'
                    ORDER BY u.active DESC, u.created_at ASC
                    LIMIT 1
                ) owner ON TRUE
                LEFT JOIN tenant_plan p ON p.tenant_id = t.id
                WHERE t.id = ?
                """,
            (rs, row) -> tenantSummary(rs), tenantId)
        .stream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND"));
  }

  @Transactional(readOnly = true)
  public PlatformDashboardSummary dashboard() {
    final long tenantsTotal = scalar("SELECT count(*) FROM hotel_registry");
    final long tenantsActive = scalar("SELECT count(*) FROM hotel_registry WHERE upper(status) = 'ACTIVE'");
    final long tenantsSuspended = scalar("SELECT count(*) FROM hotel_registry WHERE upper(status) = 'SUSPENDED'");
    final long usersTotal = scalar("SELECT count(*) FROM user_account");
    final long ownersTotal = scalar("SELECT count(*) FROM user_account WHERE role = 'OWNER'");
    final long ordersTotal = scalar("SELECT count(*) FROM service_orders");
    return new PlatformDashboardSummary(
        tenantsTotal, tenantsActive, tenantsSuspended, usersTotal, ownersTotal, ordersTotal);
  }

  @Transactional(readOnly = true)
  public PlatformPage<PlatformUserSummary> users(
      final String search, final String role, final Boolean active, final int page, final int size) {
    final int safePage = Math.max(0, page);
    final int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
    final String query = normalized(search);
    final String roleFilter = normalized(role);
    final String activeFilter = active == null ? null : active.toString();
    final String where = " WHERE (CAST(? AS text) IS NULL OR lower(u.username) LIKE lower(?)"
        + " OR lower(COALESCE(u.full_name, '')) LIKE lower(?) OR lower(u.email) LIKE lower(?))"
        + " AND (CAST(? AS text) IS NULL OR upper(u.role) = upper(?))"
        + " AND (CAST(? AS text) IS NULL OR u.active = CAST(? AS boolean))";
    final Long count = jdbc.queryForObject(
        "SELECT count(*) FROM user_account u" + where,
        Long.class, query, pattern(query), pattern(query), pattern(query),
        roleFilter, roleFilter, activeFilter, activeFilter);
    final List<PlatformUserSummary> content = jdbc.query(
        """
            SELECT u.id, u.username, u.full_name, u.email, u.role, u.active, u.hotel_id,
                   h.name AS tenant_name, u.created_at
            FROM user_account u
            LEFT JOIN hotel_registry h ON h.id = u.hotel_id
            """ + where + " ORDER BY u.created_at DESC, u.id LIMIT ? OFFSET ?",
        (rs, row) -> new PlatformUserSummary(
            rs.getObject("id", UUID.class), rs.getString("username"), rs.getString("full_name"),
            rs.getString("email"), rs.getString("role"), rs.getBoolean("active"),
            rs.getObject("hotel_id", UUID.class), rs.getString("tenant_name"),
            instant(rs.getTimestamp("created_at"))),
        query, pattern(query), pattern(query), pattern(query), roleFilter, roleFilter,
        activeFilter, activeFilter, safeSize, (long) safePage * safeSize);
    return page(content, safePage, safeSize, count == null ? 0 : count);
  }

  @Transactional(readOnly = true)
  public List<PlatformPlanSummary> plans() {
    return jdbc.query(
        "SELECT plan_key, count(*) AS assigned FROM tenant_plan GROUP BY plan_key ORDER BY plan_key",
        (rs, row) -> new PlatformPlanSummary(rs.getString("plan_key"), rs.getLong("assigned")));
  }

  @Transactional(readOnly = true)
  public PlatformPage<PlatformSubscriptionSummary> subscriptions(
      final UUID tenantId, final int page, final int size) {
    final int safePage = Math.max(0, page);
    final int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
    final String where = tenantId == null ? "" : " WHERE s.tenant_id = ?";
    final Long count = tenantId == null
        ? jdbc.queryForObject("SELECT count(*) FROM platform_tenant_subscription", Long.class)
        : jdbc.queryForObject("SELECT count(*) FROM platform_tenant_subscription s" + where, Long.class, tenantId);
    final String sql = """
        SELECT s.id, s.tenant_id, h.name AS tenant_name, s.plan_key, s.status,
               s.starts_at, s.ends_at, s.created_by
        FROM platform_tenant_subscription s
        JOIN hotel_registry h ON h.id = s.tenant_id
        """ + where + " ORDER BY s.starts_at DESC LIMIT ? OFFSET ?";
    final Object[] args = tenantId == null
        ? new Object[] {safeSize, (long) safePage * safeSize}
        : new Object[] {tenantId, safeSize, (long) safePage * safeSize};
    final List<PlatformSubscriptionSummary> content = jdbc.query(
        sql, (rs, row) -> new PlatformSubscriptionSummary(
            rs.getObject("id", UUID.class), rs.getObject("tenant_id", UUID.class),
            rs.getString("tenant_name"), rs.getString("plan_key"), rs.getString("status"),
            instant(rs.getTimestamp("starts_at")), instant(rs.getTimestamp("ends_at")),
            rs.getString("created_by")), args);
    return page(content, safePage, safeSize, count == null ? 0 : count);
  }

  @Transactional(readOnly = true)
  public PlatformPage<PlatformAuditSummary> audit(
      final UUID tenantId, final String action, final int page, final int size) {
    final int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
    final var events = auditEvents.search(
        tenantId, normalized(action),
        org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize));
    return page(events.getContent().stream().map(event -> new PlatformAuditSummary(
        event.getId(), event.getActorUsername(), event.getAction(), event.getTargetTenantId(),
        event.getOccurredAt(), event.getMetadataJson())).toList(),
        events.getNumber(), safeSize, events.getTotalElements());
  }

  @Transactional
  public PlatformTenantSummary createTenant(final CreateHotelRequest request, final String actor) {
    final var created = onboarding.create(request);
    hotels.flush();
    appendAudit(actor, "TENANT_CREATED", created.id(), "{\"slug\":\"" + json(request.slug()) + "\"}");
    return tenant(created.id());
  }

  @Transactional
  public PlatformTenantSummary updateStatus(
      final UUID tenantId, final UpdatePlatformTenantStatusRequest request, final String actor) {
    final String requested = request.status().trim().toUpperCase(java.util.Locale.ROOT);
    if (!ACTIVE.equals(requested) && !SUSPENDED.equals(requested)) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TENANT_STATUS");
    }
    final HotelRegistry tenant = hotels.findById(tenantId)
        .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND"));
    final String previous = tenant.getStatus();
    if (!requested.equalsIgnoreCase(previous)) {
      tenant.setStatus(requested);
      hotels.save(tenant);
      hotels.flush();
      appendAudit(actor, SUSPENDED.equals(requested) ? "TENANT_SUSPENDED" : "TENANT_REACTIVATED",
          tenantId, "{\"previousStatus\":\"" + json(previous) + "\",\"status\":\"" + requested + "\"}");
    }
    return tenant(tenantId);
  }

  @Transactional
  public PlatformTenantSummary assignPlan(
      final UUID tenantId, final AssignPlatformPlanRequest request, final String actor) {
    final HotelRegistry tenant = hotels.findById(tenantId)
        .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND"));
    final String planKey = request.planKey().trim();
    final TenantPlan current = plans.findById(tenantId).orElseGet(() -> {
      final TenantPlan created = new TenantPlan();
      created.setTenantId(tenantId);
      created.setCreatedAt(LocalDateTime.now());
      return created;
    });
    final String previous = current.getPlanKey();
    if (planKey.equals(previous) && current.isActive()) {
      return tenant(tenantId);
    }
    final Instant now = Instant.now();
    subscriptions.findFirstByTenantIdAndStatusOrderByStartsAtDesc(tenantId, "ACTIVE")
        .ifPresent(active -> {
          active.setStatus("ENDED");
          active.setEndsAt(now);
          subscriptions.save(active);
        });
    final PlatformTenantSubscription assignment = new PlatformTenantSubscription();
    assignment.setId(UUID.randomUUID());
    assignment.setTenantId(tenantId);
    assignment.setPlanKey(planKey);
    assignment.setStatus("ACTIVE");
    assignment.setStartsAt(now);
    assignment.setCreatedBy(actor);
    subscriptions.save(assignment);
    current.setPlanKey(planKey);
    current.setActive(true);
    final LocalDateTime updatedAt = LocalDateTime.now();
    current.setUpdatedAt(updatedAt);
    if (current.getCreatedAt() == null) {
      current.setCreatedAt(updatedAt);
    }
    plans.save(current);
    plans.flush();
    subscriptions.flush();
    appendAudit(actor, "TENANT_PLAN_ASSIGNED", tenantId,
        "{\"previousPlanKey\":\"" + json(previous) + "\",\"planKey\":\"" + json(planKey) + "\"}");
    return tenant(tenantId);
  }

  private void appendAudit(
      final String actor, final String action, final UUID tenantId, final String metadata) {
    final PlatformAuditEvent event = new PlatformAuditEvent();
    event.setId(UUID.randomUUID());
    event.setActorUsername(actor);
    event.setAction(action);
    event.setTargetTenantId(tenantId);
    event.setOccurredAt(Instant.now());
    event.setMetadataJson(metadata);
    auditEvents.save(event);
  }

  private long scalar(final String sql) {
    final Long value = jdbc.queryForObject(sql, Long.class);
    return value == null ? 0 : value;
  }

  private static PlatformTenantSummary tenantSummary(final ResultSet rs) throws SQLException {
    final Timestamp created = rs.getTimestamp("created_at");
    return new PlatformTenantSummary(
        rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("slug"),
        rs.getString("status"), instant(created), rs.getString("owner_username"),
        rs.getString("owner_name"), rs.getString("owner_email"), rs.getString("plan_key"),
        rs.getBoolean("plan_active"), rs.getBoolean("onboarding_completed"));
  }

  private static Instant instant(final Timestamp value) {
    return value == null ? null : value.toInstant();
  }

  private static String normalized(final String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private static String pattern(final String value) {
    return value == null ? null : "%" + value + "%";
  }

  private static String json(final String value) {
    if (value == null) {
      return "";
    }
    final StringBuilder escaped = new StringBuilder(value.length());
    for (final char character : value.toCharArray()) {
      if (character == '\\' || character == '"') {
        escaped.append('\\').append(character);
      } else if (character < 0x20) {
        escaped.append(String.format("\\u%04x", (int) character));
      } else {
        escaped.append(character);
      }
    }
    return escaped.toString();
  }

  private static <T> PlatformPage<T> page(
      final List<T> content, final int page, final int size, final long total) {
    return new PlatformPage<>(content, page, size, total,
        total == 0 ? 0 : (int) Math.ceil((double) total / size));
  }
}
