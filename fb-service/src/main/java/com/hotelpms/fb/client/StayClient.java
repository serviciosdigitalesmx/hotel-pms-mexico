package com.hotelpms.fb.client;

import com.hotelpms.fb.client.dto.StayResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * OpenFeign client for communicating with the stays domain in frontdesk-service
 * (formerly stay-service, see ADR-001 in backup/DECISIONS.md).
 */
@FunctionalInterface
@FeignClient(
        name = "frontdesk-service-stays",
        url = "${APPLICATION_CONFIG_FRONTDESK_SERVICE_URL:http://frontdesk-service:8081}")
public interface StayClient {

    /** Logger retained in the fallback so downstream transport failures are observable. */
    Logger LOG = LoggerFactory.getLogger(StayClient.class);

    /**
     * Retrieves a stay by its ID.
     *
     * @param id the ID of the stay to retrieve
     * @return the stay details
     */
    @GetMapping("/api/v1/stays/{id}")
    @CircuitBreaker(name = "stayService", fallbackMethod = "getStayFallback")
    StayResponse getStayById(@PathVariable("id") UUID id);

    /**
     * Fallback method for getStayById in case the Stay service is unreachable.
     *
     * @param id        the ID of the stay
     * @param throwable the exception that caused the fallback
     * @return a default or empty StayResponse
     */
    default StayResponse getStayFallback(final UUID id, final Throwable throwable) {
        LOG.warn("[StayClient] getStayById fallback | stayId={} | cause={}: {}",
                id, throwable.getClass().getSimpleName(), throwable.getMessage());
        return new StayResponse(id, "UNKNOWN", null, null);
    }
}
