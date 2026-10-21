package com.hotelpms.monolith.security;

import java.util.UUID;

/** Path validation for external-provider webhooks protected by their own signatures. */
final class MonolithWebhookPath {

  private static final String WHATSAPP_WEBHOOK_PREFIX = "/internal/whatsapp/webhook/";

  private MonolithWebhookPath() {}

  static boolean isWhatsAppWebhook(final String path) {
    if (path == null || !path.startsWith(WHATSAPP_WEBHOOK_PREFIX)) {
      return false;
    }
    final String tenant = path.substring(WHATSAPP_WEBHOOK_PREFIX.length());
    try {
      return UUID.fromString(tenant).toString().equalsIgnoreCase(tenant);
    } catch (final IllegalArgumentException exception) {
      return false;
    }
  }
}
