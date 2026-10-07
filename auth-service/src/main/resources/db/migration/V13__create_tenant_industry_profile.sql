CREATE TABLE tenant_industry_profile (
    tenant_id UUID PRIMARY KEY REFERENCES hotel_registry(id),
    industry_key VARCHAR(80) NOT NULL,
    enabled_modules_json TEXT NOT NULL DEFAULT '{}',
    dynamic_fields_json TEXT NOT NULL DEFAULT '{}',
    forms_labels_json TEXT NOT NULL DEFAULT '{}',
    catalogs_templates_json TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
