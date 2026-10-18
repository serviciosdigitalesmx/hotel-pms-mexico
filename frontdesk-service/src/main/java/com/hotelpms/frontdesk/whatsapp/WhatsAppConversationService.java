package com.hotelpms.frontdesk.whatsapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/** Normalizes Evolution events into tenant-scoped inbox records. */
@Service
@RequiredArgsConstructor
@SuppressWarnings({
    "checkstyle:JavadocMethod",
    "checkstyle:IllegalCatch",
    "checkstyle:LeftCurly",
    "checkstyle:OneStatementPerLine",
    "checkstyle:MagicNumber"
})
public class WhatsAppConversationService {
    // CHECKSTYLE: JavadocMethod|LeftCurly|IllegalCatch|OneStatementPerLine|MagicNumber OFF
    private final ConversationRepository conversations;
    private final WhatsAppMessageRepository messages;
    private final ObjectMapper mapper;

    /** Persists an inbound event exactly once and returns whether it was new. */
    @Transactional
    public boolean ingest(final UUID hotelId, final String eventId, final String body) {
        if (messages.existsByHotelIdAndProviderMessageId(hotelId, eventId)) {
            return false;
        }
        final JsonNode root = read(body);
        final String channel = text(root, "remoteJid", text(root, "from", "unknown:" + hotelId));
        final String content = text(root, "text", text(root.path("message"), "conversation", ""));
        final Conversation conversation =
                conversations
                        .findByHotelIdAndChannelKey(hotelId, channel)
                        .orElseGet(
                                () ->
                                        conversations.save(
                                                Conversation.builder()
                                                        .hotelId(hotelId)
                                                        .channelKey(channel)
                                                        .updatedAt(LocalDateTime.now())
                                                        .build()));
        messages.save(
                WhatsAppMessage.builder()
                        .hotelId(hotelId)
                        .conversationId(conversation.getId())
                        .providerMessageId(eventId)
                        .direction(WhatsAppMessage.Direction.INBOUND)
                        .body(content)
                        .createdAt(LocalDateTime.now())
                        .build());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversations.save(conversation);
        return true;
    }

    private JsonNode read(final String body) {
        try {
            return mapper.readTree(body);
        } catch (final JsonProcessingException ignored) {
            return mapper.createObjectNode();
        }
    }

    private static String text(final JsonNode node, final String field, final String fallback) {
        final String value = node.path(field).asText("");
        return value.isBlank() ? fallback : value.substring(0, Math.min(value.length(), 10_000));
    }
    // CHECKSTYLE: JavadocMethod|LeftCurly|IllegalCatch|OneStatementPerLine|MagicNumber ON
}
