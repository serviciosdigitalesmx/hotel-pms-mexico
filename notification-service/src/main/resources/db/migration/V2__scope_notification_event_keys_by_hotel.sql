ALTER TABLE notification_deliveries
    DROP CONSTRAINT notification_deliveries_event_key_key;

ALTER TABLE notification_deliveries
    ADD CONSTRAINT uq_notification_deliveries_hotel_event UNIQUE (hotel_id, event_key);
