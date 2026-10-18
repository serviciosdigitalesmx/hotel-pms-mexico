package com.hotelpms.frontdesk.whatsapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant/channel scoped conversation lookup. */
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup OFF
    /**
     * Finds a conversation by tenant and provider channel.
     *
     * @param hotelId tenant identifier
     * @param channelKey provider channel
     * @return matching conversation
     */
    Optional<Conversation> findByHotelIdAndChannelKey(UUID hotelId, String channelKey);

    List<Conversation> findAllByHotelIdOrderByUpdatedAtDesc(UUID hotelId);
    // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup ON
}
