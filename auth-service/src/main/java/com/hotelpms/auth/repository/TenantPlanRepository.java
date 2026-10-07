package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.TenantPlan;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides persistence operations for tenant plans. */
public interface TenantPlanRepository extends JpaRepository<TenantPlan, UUID> { }
