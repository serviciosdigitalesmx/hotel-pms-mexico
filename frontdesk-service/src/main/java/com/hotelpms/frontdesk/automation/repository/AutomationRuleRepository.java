package com.hotelpms.frontdesk.automation.repository;

import com.hotelpms.frontdesk.automation.domain.AutomationRule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Tenant-scoped automation rules. */
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {
    /**
     * Finds enabled automation rules owned by one tenant.
     *
     * @param hotelId tenant identifier
     * @return enabled tenant-owned rules
     */
    List<AutomationRule> findAllByHotelIdAndEnabledTrue(UUID hotelId);
}
