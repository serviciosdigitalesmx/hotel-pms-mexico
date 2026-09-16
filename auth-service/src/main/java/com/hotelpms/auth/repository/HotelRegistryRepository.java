package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.internalauth.architecture.TenantScopeExempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Only the platform onboarding service may read this global registry. */
public interface HotelRegistryRepository extends JpaRepository<HotelRegistry, UUID> {
    /**
     * Checks slug uniqueness across the platform.
     *
     * @param slug candidate slug
     * @return whether a hotel already uses it
     */
    @TenantScopeExempt(reason = "Platform operator checks globally unique hotel slugs")
    boolean existsBySlug(String slug);
}
