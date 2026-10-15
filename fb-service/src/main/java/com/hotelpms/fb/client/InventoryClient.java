package com.hotelpms.fb.client;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Authenticated client for applying idempotent POS stock consumption. */
@FeignClient(name = "frontdesk-service-inventory",
    url = "${APPLICATION_CONFIG_FRONTDESK_SERVICE_URL:http://frontdesk-service:8081}")
public interface InventoryClient {
  @org.springframework.web.bind.annotation.PostMapping("/api/v1/auth/branches/{branchId}/validate")
  Map<String, Object> validateBranch(@PathVariable("branchId") UUID branchId);

  @PostMapping("/api/v1/inventory/pos/orders/{orderId}/consume")
  void consume(@PathVariable("orderId") UUID orderId, @RequestBody ConsumptionRequest request);

  record ConsumptionRequest(UUID branchId, List<ConsumptionLine> lines) { }
  record ConsumptionLine(UUID productId, long quantity) { }
}
