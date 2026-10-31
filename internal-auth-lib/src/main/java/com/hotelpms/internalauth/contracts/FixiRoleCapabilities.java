package com.hotelpms.internalauth.contracts;

import java.util.EnumSet;
import java.util.Set;

/** Authoritative default capability templates for Fixi workshop employees. */
public final class FixiRoleCapabilities {
  private static final Set<Capability> FIXI_CAPABILITIES = EnumSet.of(
      Capability.BRANCHES_READ, Capability.BRANCHES_MANAGE,
      Capability.CAPABILITIES_READ, Capability.CAPABILITIES_MANAGE,
      Capability.CUSTOMERS_READ, Capability.CUSTOMERS_WRITE,
      Capability.DEVICES_READ, Capability.DEVICES_WRITE, Capability.DEVICE_TRANSFER,
      Capability.ORDERS_READ, Capability.ORDERS_CREATE, Capability.ORDERS_UPDATE,
      Capability.ORDERS_CANCEL, Capability.ORDERS_STATUS_CHANGE,
      Capability.QUOTES_CREATE, Capability.QUOTES_MODIFY, Capability.QUOTES_AUTHORIZE_RECORD,
      Capability.INVENTORY_READ, Capability.INVENTORY_ADJUST, Capability.INVENTORY_PURCHASE,
      Capability.CASH_OPEN, Capability.CASH_CLOSE, Capability.CASH_MOVE,
      Capability.PAYMENTS_RECEIVE, Capability.PAYMENTS_REFUND, Capability.FINANCE_READ,
      Capability.PERMISSIONS_MANAGE, Capability.WORKFLOWS_MANAGE,
      Capability.SETTINGS_MANAGE, Capability.SETTINGS_READ,
      Capability.BILLING_READ, Capability.BILLING_WRITE, Capability.REPORTS_READ,
      Capability.USERS_MANAGE);

  private static final Set<Capability> OWNER = EnumSet.copyOf(FIXI_CAPABILITIES);
  private static final Set<Capability> ADMIN = EnumSet.copyOf(FIXI_CAPABILITIES);
  private static final Set<Capability> MANAGER = EnumSet.of(
      Capability.BRANCHES_READ, Capability.CUSTOMERS_READ, Capability.CUSTOMERS_WRITE,
      Capability.DEVICES_READ, Capability.DEVICES_WRITE,
      Capability.ORDERS_READ, Capability.ORDERS_CREATE, Capability.ORDERS_UPDATE,
      Capability.ORDERS_CANCEL, Capability.ORDERS_STATUS_CHANGE,
      Capability.QUOTES_CREATE, Capability.QUOTES_MODIFY, Capability.QUOTES_AUTHORIZE_RECORD,
      Capability.INVENTORY_READ, Capability.INVENTORY_ADJUST, Capability.INVENTORY_PURCHASE,
      Capability.PAYMENTS_RECEIVE, Capability.SETTINGS_READ,
      Capability.REPORTS_READ, Capability.USERS_MANAGE,
      Capability.BILLING_READ, Capability.BILLING_WRITE);
  private static final Set<Capability> RECEPTIONIST = EnumSet.of(
      Capability.CUSTOMERS_READ, Capability.CUSTOMERS_WRITE,
      Capability.DEVICES_READ, Capability.DEVICES_WRITE,
      Capability.ORDERS_READ, Capability.ORDERS_CREATE, Capability.ORDERS_UPDATE,
      Capability.ORDERS_STATUS_CHANGE,
      Capability.QUOTES_CREATE, Capability.QUOTES_MODIFY, Capability.QUOTES_AUTHORIZE_RECORD,
      Capability.INVENTORY_READ, Capability.PAYMENTS_RECEIVE);
  private static final Set<Capability> TECHNICIAN = EnumSet.of(
      Capability.CUSTOMERS_READ, Capability.DEVICES_READ,
      Capability.ORDERS_READ, Capability.ORDERS_UPDATE, Capability.ORDERS_STATUS_CHANGE,
      Capability.INVENTORY_READ);

  static {
    ADMIN.remove(Capability.CAPABILITIES_MANAGE);
    ADMIN.remove(Capability.PERMISSIONS_MANAGE);
  }

  private FixiRoleCapabilities() {}

  /** Returns whether this enum role is a selectable Fixi workshop employee role. */
  public static boolean isEmployeeRole(final String role) {
    return role != null && switch (role) {
      case "MANAGER", "RECEPTIONIST", "TECHNICIAN" -> true;
      default -> false;
    };
  }

  /** Returns whether a role is recognized as a Fixi tenant role (not a platform grant). */
  public static boolean isWorkshopRole(final String role) {
    return role != null && switch (role) {
      case "OWNER", "ADMIN", "MANAGER", "RECEPTIONIST", "TECHNICIAN" -> true;
      default -> false;
    };
  }

  /** Returns the immutable default capability template for a Fixi tenant role. */
  public static Set<Capability> forRole(final String role) {
    final Set<Capability> result = switch (role == null ? "" : role) {
      case "OWNER" -> OWNER;
      case "ADMIN" -> ADMIN;
      case "MANAGER" -> MANAGER;
      case "RECEPTIONIST" -> RECEPTIONIST;
      case "TECHNICIAN" -> TECHNICIAN;
      default -> Set.of();
    };
    return Set.copyOf(result);
  }

  /** Platform and hotel-PMS capabilities are never workshop capabilities. */
  public static boolean isFixiCapability(final Capability capability) {
    return FIXI_CAPABILITIES.contains(capability);
  }
}
