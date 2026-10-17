package com.hotelpms.frontdesk.client;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthFeignClientConfigurationTest {

    @Test
    void authClientsUseDistinctNamedContextsForTheSameService() {
        final Set<String> contextIds = Set.of(
                CapabilityClient.class.getAnnotation(FeignClient.class).contextId(),
                UserAssignmentClient.class.getAnnotation(FeignClient.class).contextId(),
                BranchClient.class.getAnnotation(FeignClient.class).contextId());

        assertEquals(3, contextIds.size());
        assertFalse(contextIds.contains(""));
    }
}
