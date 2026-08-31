package com.hotelpms.frontdesk.stays.repository;

import com.hotelpms.frontdesk.stays.domain.HotelSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelpms.internalauth.architecture.TenantScopeExempt;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for per-hotel operational settings.
 */
public interface HotelSettingsRepository extends JpaRepository<HotelSettings, UUID> {

    /**
     * Resolves the tenant from its globally unique public booking slug.
     *
     * <p>This query is deliberately cross-tenant because no tenant context
     * exists yet. Its purpose is to establish that context from the public
     * identifier.</p>
     *
     * @param publicSlug globally unique public hotel slug
     * @return matching hotel settings when the slug exists
     */
    @TenantScopeExempt(
            reason = "Public booking resolves tenant from globally unique public_slug before tenant context exists")
    Optional<HotelSettings> findByPublicSlug(String publicSlug);

}
