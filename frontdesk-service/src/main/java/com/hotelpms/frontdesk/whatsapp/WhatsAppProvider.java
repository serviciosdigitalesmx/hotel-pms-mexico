package com.hotelpms.frontdesk.whatsapp;

import java.util.UUID;

/** Provider boundary for tenant-scoped WhatsApp delivery and connection state. */
public interface WhatsAppProvider {
  /**
   * Sends a text message.
   *
   * @param hotelId tenant identifier
   * @param remoteJid provider recipient
   * @param text message body
   * @return whether the provider accepted the message
   */
  boolean sendText(UUID hotelId, String remoteJid, String text);

  /**
   * Reads the provider connection state.
   *
   * @param hotelId tenant identifier
   * @return normalized connection state
   */
  EvolutionService.Connection status(UUID hotelId);
}
