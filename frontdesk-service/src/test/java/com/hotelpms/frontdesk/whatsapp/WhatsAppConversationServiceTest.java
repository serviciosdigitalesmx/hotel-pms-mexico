package com.hotelpms.frontdesk.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class WhatsAppConversationServiceTest {
  private ConversationRepository conversations;
  private WhatsAppMessageRepository messages;
  private WhatsAppConversationService service;

  @BeforeEach
  void setUp() {
    conversations = mock(ConversationRepository.class);
    messages = mock(WhatsAppMessageRepository.class);
    service = new WhatsAppConversationService(conversations, messages, new ObjectMapper());
  }

  @Test
  void ingestIsTenantScopedAndIdempotent() {
    final UUID hotel = UUID.randomUUID();
    final UUID otherHotel = UUID.randomUUID();
    final UUID conversationId = UUID.randomUUID();
    final Conversation conversation = Conversation.builder().id(conversationId).hotelId(hotel)
        .channelKey("5215550101@s.whatsapp.net").build();
    when(messages.existsByHotelIdAndProviderMessageId(hotel, "event-1"))
        .thenReturn(false, true);
    when(conversations.findByHotelIdAndChannelKey(hotel, conversation.getChannelKey()))
        .thenReturn(Optional.of(conversation));

    assertTrue(service.ingest(hotel, "event-1",
        "{\"remoteJid\":\"5215550101@s.whatsapp.net\",\"text\":\"Necesito ayuda\"}"));
    assertFalse(service.ingest(hotel, "event-1", "{}"));

    verify(messages).save(any(WhatsAppMessage.class));
    verify(conversations).findByHotelIdAndChannelKey(hotel, conversation.getChannelKey());
    verify(messages, never()).existsByHotelIdAndProviderMessageId(otherHotel, "event-1");
    assertEquals(hotel, conversation.getHotelId());
  }
}
