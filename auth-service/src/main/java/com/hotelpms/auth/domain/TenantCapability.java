package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_capability")
@IdClass(TenantCapability.Key.class)
@Data
@NoArgsConstructor
public class TenantCapability {
    @Id @Column(name = "tenant_id") private UUID tenantId;
    @Id @Column(name = "capability_key", length = 120) private String capabilityKey;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "limit_value") private Long limitValue;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public record Key(UUID tenantId, String capabilityKey) implements Serializable { }
}
