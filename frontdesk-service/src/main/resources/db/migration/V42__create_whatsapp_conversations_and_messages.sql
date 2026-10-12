CREATE TABLE whatsapp_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL,
    channel_key VARCHAR(160) NOT NULL, customer_id UUID, device_id UUID,
    service_order_id UUID, quotation_id UUID, payment_id UUID,
    ai_mode VARCHAR(20) NOT NULL DEFAULT 'HUMAN_ONLY', handoff BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL, CONSTRAINT uq_whatsapp_conversation_channel UNIQUE(hotel_id, channel_key)
);
CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL,
    conversation_id UUID NOT NULL, provider_message_id VARCHAR(180) NOT NULL,
    direction VARCHAR(10) NOT NULL, body VARCHAR(10000) NOT NULL, created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_whatsapp_message_provider UNIQUE(hotel_id, provider_message_id)
);
CREATE INDEX idx_whatsapp_messages_inbox ON whatsapp_messages(hotel_id, conversation_id, created_at);
