package com.hotelpms.auth.domain;

/**
 * Role enum representing user authorities.
 *
 * <ul>
 *   <li>{@code ADMIN} — system administrator; full access to all operations.
 *   <li>{@code OWNER} — hotel owner; same access as ADMIN within their hotel.
 *   <li>{@code RECEPTIONIST}— front-desk operator; can manage reservations, guests, check-in/out,
 *       billing, and F&B. Cannot manage room types, delete rooms, or view financial reports.
 *   <li>{@code KITCHEN} — kitchen operator; can manage restaurant orders.
 *   <li>{@code HOUSEKEEPER} — housekeeping operator; can view rooms and update their cleaning
 *       status.
 *   <li>{@code GUEST} — reserved for a future guest-facing portal; currently no API access is
 *       granted to this role.
 * </ul>
 */
public enum Role {
  ADMIN,
  OWNER,
  MANAGER,
  TECHNICIAN,
  RECEPTIONIST,
  KITCHEN,
  HOUSEKEEPER,
  GUEST
}
