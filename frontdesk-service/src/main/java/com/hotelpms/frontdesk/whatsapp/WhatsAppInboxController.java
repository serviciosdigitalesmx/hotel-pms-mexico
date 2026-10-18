package com.hotelpms.frontdesk.whatsapp;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Staff inbox read model for tenant-scoped WhatsApp conversations. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/whatsapp/inbox")
@SuppressWarnings("checkstyle:JavadocMethod")
public class WhatsAppInboxController {
    private static final String FRONTDESK_ROLES =
            "hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')";
    // CHECKSTYLE: JavadocMethod OFF
    private final ConversationRepository conversations;
    private final WhatsAppMessageRepository messages;
    private final WhatsAppProvider evolution;

    /** Lists conversations owned by the active tenant. */
    @GetMapping("/conversations")
    @PreAuthorize(FRONTDESK_ROLES)
    public List<Conversation> conversations() {
        return conversations.findAllByHotelIdOrderByUpdatedAtDesc(hotel());
    }

    /** Returns one conversation for the active tenant. */
    @GetMapping("/conversations/{id}")
    @PreAuthorize(FRONTDESK_ROLES)
    public Conversation conversation(@PathVariable final UUID id) {
        return conversations
                .findById(id)
                .filter(value -> hotel().equals(value.getHotelId()))
                .orElseThrow(() -> new IllegalArgumentException("CONVERSATION_NOT_FOUND"));
    }

    /** Returns messages only after tenant ownership is checked. */
    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize(FRONTDESK_ROLES)
    public List<WhatsAppMessage> messages(@PathVariable final UUID id) {
        conversation(id);
        return messages.findAllByHotelIdAndConversationIdOrderByCreatedAtAsc(hotel(), id);
    }

    /** Sends a human reply through the tenant's Evolution instance and records it locally. */
    @PostMapping("/conversations/{id}/messages")
    @PreAuthorize(FRONTDESK_ROLES)
    public WhatsAppMessage reply(
            @PathVariable final UUID id,
            @org.springframework.web.bind.annotation.RequestParam final String text) {
        final Conversation target = conversation(id);
        if (!evolution.sendText(hotel(), target.getChannelKey(), text)) {
            throw new IllegalStateException("WHATSAPP_PROVIDER_UNAVAILABLE");
        }
        final WhatsAppMessage message =
                messages.save(
                        WhatsAppMessage.builder()
                                .hotelId(hotel())
                                .conversationId(id)
                                .providerMessageId("local-" + UUID.randomUUID())
                                .direction(WhatsAppMessage.Direction.OUTBOUND)
                                .body(text)
                                .createdAt(java.time.LocalDateTime.now())
                                .build());
        target.setHandoff(false);
        target.setAiMode(Conversation.AiMode.HUMAN_ONLY);
        conversations.save(target);
        return message;
    }

    /** Puts a conversation in human handoff and disables automatic responses. */
    @PostMapping("/conversations/{id}/handoff")
    @PreAuthorize(FRONTDESK_ROLES)
    public Conversation handoff(@PathVariable final UUID id) {
        final Conversation target = conversation(id);
        target.setHandoff(true);
        target.setAiMode(Conversation.AiMode.HUMAN_ONLY);
        return conversations.save(target);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
    // CHECKSTYLE: JavadocMethod ON
}
