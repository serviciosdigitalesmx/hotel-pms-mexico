package com.hotelpms.frontdesk.automation.repository;

import com.hotelpms.frontdesk.automation.domain.AutomationRule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped automation rules. */
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {
  List<AutomationRule> findAllByHotelIdAndEnabledTrue(UUID hotelId);
}
