package com.hotelpms.frontdesk.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

class WhatsAppConversationServiceTest {
    private static final String PROVIDER_EVENT_ID = "event-1";

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
        final Conversation conversation =
                Conversation.builder()
                        .id(conversationId)
                        .hotelId(hotel)
                        .channelKey("5215550101@s.whatsapp.net")
                        .build();
        when(messages.existsByHotelIdAndProviderMessageId(hotel, PROVIDER_EVENT_ID))
                .thenReturn(false, true);
        when(conversations.findByHotelIdAndChannelKey(hotel, conversation.getChannelKey()))
                .thenReturn(Optional.of(conversation));

        assertTrue(
                service.ingest(
                        hotel,
                        PROVIDER_EVENT_ID,
                        "{\"remoteJid\":\"5215550101@s.whatsapp.net\",\"text\":\"Necesito"
                            + " ayuda\"}"));
        assertFalse(service.ingest(hotel, PROVIDER_EVENT_ID, "{}"));

        verify(messages).save(any(WhatsAppMessage.class));
        verify(conversations).findByHotelIdAndChannelKey(hotel, conversation.getChannelKey());
        verify(messages, never())
                .existsByHotelIdAndProviderMessageId(otherHotel, PROVIDER_EVENT_ID);
        assertEquals(hotel, conversation.getHotelId());
    }
}
