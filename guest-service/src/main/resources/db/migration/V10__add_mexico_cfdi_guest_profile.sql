ALTER TABLE guests
    ADD COLUMN rfc                VARCHAR(13)  NULL,
    ADD COLUMN fiscal_name        VARCHAR(200) NULL,
    ADD COLUMN fiscal_postal_code VARCHAR(5)   NULL,
    ADD COLUMN fiscal_regime      VARCHAR(3)   NULL,
    ADD COLUMN cfdi_use           VARCHAR(4)   NULL,
    ADD COLUMN billing_email      VARCHAR(150) NULL;

COMMENT ON COLUMN guests.rfc IS
'RFC del receptor para CFDI 4.0.';

COMMENT ON COLUMN guests.fiscal_name IS
'Nombre, denominación o razón social fiscal del receptor.';

COMMENT ON COLUMN guests.fiscal_postal_code IS
'Código postal del domicilio fiscal del receptor.';

COMMENT ON COLUMN guests.fiscal_regime IS
'Clave SAT c_RegimenFiscal del receptor.';

COMMENT ON COLUMN guests.cfdi_use IS
'Clave SAT c_UsoCFDI indicada por el receptor.';

COMMENT ON COLUMN guests.billing_email IS
'Correo opcional para entrega del CFDI.';
