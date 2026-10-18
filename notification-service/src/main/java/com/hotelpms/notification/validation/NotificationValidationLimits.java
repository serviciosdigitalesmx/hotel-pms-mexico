package com.hotelpms.notification.validation;

/** Shared limits for notification payloads and persisted delivery fields. */
public final class NotificationValidationLimits {
  /** Maximum address length accepted by the email address validator. */
  public static final int MAX_EMAIL_ADDRESS_LENGTH = 254;
  /** Maximum subject length accepted for notification messages. */
  public static final int MAX_SUBJECT_LENGTH = 180;
  /** Maximum plain-text body length accepted for operational emails. */
  public static final int MAX_MESSAGE_LENGTH = 12_000;
  /** Maximum hotel display name length accepted in email requests. */
  public static final int MAX_HOTEL_NAME_LENGTH = 120;
  /** Maximum idempotency key length for an event. */
  public static final int MAX_EVENT_KEY_LENGTH = 180;
  /** Maximum event type length persisted by the delivery outbox. */
  public static final int MAX_EVENT_TYPE_LENGTH = 120;
  /** Maximum length for the delivery status column. */
  public static final int MAX_STATUS_LENGTH = 24;
  /** Maximum serialized event payload length accepted by the internal API. */
  public static final int MAX_EVENT_PAYLOAD_LENGTH = 1_000_000;
  /** Maximum error text length retained for a failed delivery. */
  public static final int MAX_DELIVERY_ERROR_LENGTH = 2_000;

  private NotificationValidationLimits() {
    // Utility class.
  }
}
