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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
  private static final String TENANT_NOT_FOUND = "TENANT_NOT_FOUND";
  private static final String ID_COLUMN = "id";
  private static final String PLAN_KEY_COLUMN = "plan_key";
  private static final String JSON_OBJECT_END = "\"}";
  private static final int MAX_PAGE_SIZE = 100;

  private final JdbcTemplate jdbc;
  private final HotelRegistryRepository hotels;
  private final TenantPlanRepository plans;
  private final PlatformTenantSubscriptionRepository subscriptions;
  private final PlatformAuditEventRepository auditEvents;
  private final HotelOnboardingService onboarding;

  /** Searches tenants using platform-level filters and bounded pagination.
   * @param search optional name or slug substring
   * @param status optional lifecycle-state filter
   * @param page zero-based page index
   * @param size requested page size
   * @return tenant projection page
   * @implSpec Preserve SQL filtering, page bounds, and platform-only data access.
   */
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

  /** Reads one tenant's current administrative projection.
   * @param tenantId tenant identifier
   * @return tenant projection
   * @throws NotFoundException when the tenant does not exist
   * @implSpec Keep this cross-tenant read behind the platform grant guard.
   */
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
        .orElseThrow(() -> new NotFoundException(TENANT_NOT_FOUND));
  }

  /** Calculates dashboard totals from persisted Fixi records.
   * @return current platform totals
   * @implSpec Keep counts database-backed; do not substitute presentation fixtures.
   */
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

  /** Searches the safe global account directory.
   * @param search optional username, name, or email substring
   * @param role optional tenant-role filter
   * @param active optional account active-state filter
   * @param page zero-based page index
   * @param size requested page size
   * @return safe user projection page
   * @implSpec Never select password hashes, cookies, refresh tokens, or API secrets.
   */
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
            rs.getObject(ID_COLUMN, UUID.class), rs.getString("username"), rs.getString("full_name"),
            rs.getString("email"), rs.getString("role"), rs.getBoolean("active"),
            rs.getObject("hotel_id", UUID.class), rs.getString("tenant_name"),
            instant(rs.getObject("created_at", LocalDateTime.class))),
        query, pattern(query), pattern(query), pattern(query), roleFilter, roleFilter,
        activeFilter, activeFilter, safeSize, (long) safePage * safeSize);
    return page(content, safePage, safeSize, count == null ? 0 : count);
  }

  /** Lists plan keys already assigned to tenants.
   * @return assigned plan keys and tenant counts
   * @implSpec Do not synthesize unpersisted catalog entries.
   */
  @Transactional(readOnly = true)
  public List<PlatformPlanSummary> plans() {
    return jdbc.query(
        "SELECT plan_key, count(*) AS assigned FROM tenant_plan GROUP BY plan_key ORDER BY plan_key",
        (rs, row) -> new PlatformPlanSummary(rs.getString(PLAN_KEY_COLUMN), rs.getLong("assigned")));
  }

  /** Lists plan-history rows, optionally scoped to one tenant.
   * @param tenantId optional tenant identifier
   * @param page zero-based page index
   * @param size requested page size
   * @return bounded plan-history page
   * @implSpec Preserve tenant filtering and bounded pagination.
   */
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
            rs.getObject(ID_COLUMN, UUID.class), rs.getObject("tenant_id", UUID.class),
            rs.getString("tenant_name"), rs.getString(PLAN_KEY_COLUMN), rs.getString("status"),
            instant(rs.getObject("starts_at", OffsetDateTime.class)),
            instant(rs.getObject("ends_at", OffsetDateTime.class)),
            rs.getString("created_by")), args);
    return page(content, safePage, safeSize, count == null ? 0 : count);
  }

  /** Lists audit events, optionally filtered by tenant and action.
   * @param tenantId optional affected tenant identifier
   * @param action optional action name
   * @param page zero-based page index
   * @param size requested page size
   * @return bounded audit page
   * @implSpec Preserve secret-free metadata and the platform-only access boundary.
   */
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

  /** Creates a tenant with its first owner and records the actor in the platform audit.
   * @param request validated tenant and owner details
   * @param actor authenticated platform administrator
   * @return created tenant projection
   * @implSpec Preserve atomic onboarding and audit-event persistence.
   */
  @Transactional
  public PlatformTenantSummary createTenant(final CreateHotelRequest request, final String actor) {
    final var created = onboarding.create(request);
    hotels.flush();
    appendAudit(actor, "TENANT_CREATED", created.id(), "{\"slug\":\"" + json(request.slug()) + JSON_OBJECT_END);
    return tenant(created.id());
  }

  /** Applies a validated tenant lifecycle transition and records its audit event.
   * @param tenantId tenant identifier
   * @param request requested lifecycle status
   * @param actor authenticated platform administrator
   * @return updated tenant projection
   * @implSpec Preserve data on suspension and write the audit event in the same transaction.
   */
  @Transactional
  public PlatformTenantSummary updateStatus(
      final UUID tenantId, final UpdatePlatformTenantStatusRequest request, final String actor) {
    final String requested = request.status().trim().toUpperCase(java.util.Locale.ROOT);
    if (!ACTIVE.equals(requested) && !SUSPENDED.equals(requested)) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TENANT_STATUS");
    }
    final HotelRegistry tenant = hotels.findById(tenantId)
        .orElseThrow(() -> new NotFoundException(TENANT_NOT_FOUND));
    final String previous = tenant.getStatus();
    if (!requested.equalsIgnoreCase(previous)) {
      tenant.setStatus(requested);
      hotels.save(tenant);
      hotels.flush();
      appendAudit(actor, SUSPENDED.equals(requested) ? "TENANT_SUSPENDED" : "TENANT_REACTIVATED",
          tenantId, "{\"previousStatus\":\"" + json(previous) + "\",\"status\":\"" + requested + JSON_OBJECT_END);
    }
    return tenant(tenantId);
  }

  /** Assigns a plan key and preserves the prior assignment in history.
   * @param tenantId tenant identifier
   * @param request requested plan key
   * @param actor authenticated platform administrator
   * @return updated tenant projection
   * @implSpec Keep history and audit updates transactional; this is not payment processing.
   */
  @Transactional
  public PlatformTenantSummary assignPlan(
      final UUID tenantId, final AssignPlatformPlanRequest request, final String actor) {
    hotels.findById(tenantId).orElseThrow(() -> new NotFoundException(TENANT_NOT_FOUND));
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
    subscriptions.findFirstByTenantIdAndStatusOrderByStartsAtDesc(tenantId, ACTIVE)
        .ifPresent(active -> {
          active.setStatus("ENDED");
          active.setEndsAt(now);
          subscriptions.save(active);
        });
    final PlatformTenantSubscription assignment = new PlatformTenantSubscription();
    assignment.setId(UUID.randomUUID());
    assignment.setTenantId(tenantId);
    assignment.setPlanKey(planKey);
    assignment.setStatus(ACTIVE);
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
        "{\"previousPlanKey\":\"" + json(previous) + "\",\"planKey\":\"" + json(planKey) + JSON_OBJECT_END);
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
    final LocalDateTime created = rs.getObject("created_at", LocalDateTime.class);
    return new PlatformTenantSummary(
        rs.getObject(ID_COLUMN, UUID.class), rs.getString("name"), rs.getString("slug"),
        rs.getString("status"), instant(created), rs.getString("owner_username"),
        rs.getString("owner_name"), rs.getString("owner_email"), rs.getString(PLAN_KEY_COLUMN),
        rs.getBoolean("plan_active"), rs.getBoolean("onboarding_completed"));
  }

  private static Instant instant(final LocalDateTime value) {
    return value == null ? null : value.atZone(ZoneId.systemDefault()).toInstant();
  }

  private static Instant instant(final OffsetDateTime value) {
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
