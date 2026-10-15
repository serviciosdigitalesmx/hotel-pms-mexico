package com.hotelpms.frontdesk.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Server-side capability evaluation using the signed tenant/user context. */
@FeignClient(
    contextId = "authCapabilityClient",
    name = "auth-service",
    url = "${APPLICATION_CONFIG_AUTH_SERVICE_URL:http://auth-service:8087}")
public interface CapabilityClient {
  @GetMapping("/api/v1/auth/capabilities/{capabilityKey}")
  Map<String, Object> evaluate(@PathVariable("capabilityKey") String capabilityKey);
}
