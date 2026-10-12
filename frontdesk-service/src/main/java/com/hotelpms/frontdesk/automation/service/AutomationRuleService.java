package com.hotelpms.frontdesk.automation.service;

import com.hotelpms.frontdesk.automation.domain.AutomationRule;
import com.hotelpms.frontdesk.automation.repository.AutomationRuleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persists and retrieves tenant automation definitions without executing unsafe mutations. */
@Service
@RequiredArgsConstructor
public class AutomationRuleService {
  private final AutomationRuleRepository repository;
  @Transactional(readOnly = true)
  public List<AutomationRule> enabled(final UUID hotelId) { return repository.findAllByHotelIdAndEnabledTrue(hotelId); }
}
