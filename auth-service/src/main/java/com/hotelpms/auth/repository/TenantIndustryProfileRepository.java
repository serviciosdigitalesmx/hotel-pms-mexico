package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.TenantIndustryProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped persistence for industry configuration. */
public interface TenantIndustryProfileRepository
    extends JpaRepository<TenantIndustryProfile, UUID> { }
