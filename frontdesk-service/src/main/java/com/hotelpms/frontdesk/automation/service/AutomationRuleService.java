package com.hotelpms.frontdesk.automation.service;

import com.hotelpms.frontdesk.automation.domain.AutomationRule;
import com.hotelpms.frontdesk.automation.repository.AutomationRuleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Persists and retrieves tenant automation definitions without executing unsafe mutations. */
@Service
@RequiredArgsConstructor
public class AutomationRuleService {
    private final AutomationRuleRepository repository;

    /**
     * Lists enabled automation rules owned by one tenant.
     *
     * @param hotelId tenant identifier
     * @return enabled tenant-owned rules
     * @implSpec Overrides must preserve tenant scoping and must not execute rules during lookup.
     */
    @Transactional(readOnly = true)
    public List<AutomationRule> enabled(final UUID hotelId) {
        return repository.findAllByHotelIdAndEnabledTrue(hotelId);
    }
}
