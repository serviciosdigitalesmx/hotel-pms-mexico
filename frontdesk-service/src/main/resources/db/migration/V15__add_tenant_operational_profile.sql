ALTER TABLE hotel_settings
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN state VARCHAR(100),
    ADD COLUMN country VARCHAR(100),
    ADD COLUMN postal_code VARCHAR(5),
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
    ADD COLUMN locale VARCHAR(20) NOT NULL DEFAULT 'es-MX',
    ADD COLUMN timezone VARCHAR(50) NOT NULL DEFAULT 'America/Monterrey',
    ADD COLUMN public_slug VARCHAR(120);

CREATE UNIQUE INDEX uq_hotel_settings_public_slug
    ON hotel_settings (public_slug)
    WHERE public_slug IS NOT NULL;
