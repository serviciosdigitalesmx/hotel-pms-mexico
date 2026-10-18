package com.hotelpms.frontdesk.whatsapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant- and channel-scoped WhatsApp conversation access. */
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    /**
     * Finds a conversation by tenant and provider channel.
     *
     * @param hotelId tenant identifier
     * @param channelKey provider channel
     * @return matching conversation
     */
    Optional<Conversation> findByHotelIdAndChannelKey(UUID hotelId, String channelKey);

    /**
     * Lists conversations for a tenant from most recently updated to oldest.
     *
     * @param hotelId tenant identifier
     * @return tenant-owned conversations
     */
    List<Conversation> findAllByHotelIdOrderByUpdatedAtDesc(UUID hotelId);
}
