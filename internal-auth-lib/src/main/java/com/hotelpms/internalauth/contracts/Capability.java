package com.hotelpms.internalauth.contracts;

import java.util.Locale;
import java.util.Optional;

/**
 * Granular server-side capabilities.
 *
 * <p>Roles are templates that expand to a default set of capabilities; actual
 * authority is evaluated from the effective capability set (template plus
 * per-user grants/revocations). Adding a new capability here does not grant it
 * to any role until a role template is updated.</p>
 */
public enum Capability {
    BRANCHES_READ,
    BRANCHES_MANAGE,
    CAPABILITIES_READ,
    CAPABILITIES_MANAGE,

    CUSTOMERS_READ,
    CUSTOMERS_WRITE,
    DEVICES_READ,
    DEVICES_WRITE,
    DEVICE_TRANSFER,
    ORDERS_READ,
    ORDERS_CREATE,
    ORDERS_UPDATE,
    ORDERS_CANCEL,
    ORDERS_STATUS_CHANGE,
    QUOTES_CREATE,
    QUOTES_MODIFY,
    QUOTES_AUTHORIZE_RECORD,
    INVENTORY_READ,
    INVENTORY_ADJUST,
    INVENTORY_PURCHASE,
    CASH_OPEN,
    CASH_CLOSE,
    CASH_MOVE,
    PAYMENTS_RECEIVE,
    PAYMENTS_REFUND,
    FINANCE_READ,
    PERMISSIONS_MANAGE,
    WORKFLOWS_MANAGE,
    SETTINGS_MANAGE,
    SETTINGS_READ,

    RESERVATIONS_READ,
    RESERVATIONS_WRITE,
    ROOMS_READ,
    ROOMS_WRITE,
    ROOM_TYPES_WRITE,
    RATE_CALENDAR_WRITE,

    GUESTS_READ,
    GUESTS_WRITE,
    STAYS_READ,
    STAYS_WRITE,

    BILLING_READ,
    BILLING_WRITE,
    REPORTS_READ,

    USERS_MANAGE,
    HOTEL_SETTINGS_READ,
    HOTEL_SETTINGS_WRITE,

    FB_ORDERS_READ,
    FB_ORDERS_WRITE,

    HOUSEKEEPING_READ,
    HOUSEKEEPING_WRITE,

    PLATFORM_HOTELS_MANAGE;

    /**
     * Resolves a database/header capability code into the enum.
     *
     * @param code the capability name, case-insensitive
     * @return the matching capability, or empty if unknown
     */
    public static Optional<Capability> fromCode(final String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(code.trim().toUpperCase(Locale.ROOT).replace('.', '_')));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
