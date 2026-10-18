package com.hotelpms.frontdesk.whatsapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Idempotent tenant-scoped message storage. */
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, UUID> {
    // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup OFF
    /**
     * Checks provider-message idempotency within a tenant.
     *
     * @param hotelId tenant identifier
     * @param providerMessageId provider message identifier
     * @return whether the message already exists
     */
    boolean existsByHotelIdAndProviderMessageId(UUID hotelId, String providerMessageId);

    /**
     * Lists a conversation chronologically within a tenant.
     *
     * @param hotelId tenant identifier
     * @param conversationId conversation identifier
     * @return ordered messages
     */
    List<WhatsAppMessage> findAllByHotelIdAndConversationIdOrderByCreatedAtAsc(
            UUID hotelId, UUID conversationId);
    // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup ON
}
