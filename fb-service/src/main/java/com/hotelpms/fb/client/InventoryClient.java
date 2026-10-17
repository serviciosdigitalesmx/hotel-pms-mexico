package com.hotelpms.fb.client;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Authenticated client for applying idempotent POS stock consumption. */
@FeignClient(name = "frontdesk-service-inventory",
    url = "${APPLICATION_CONFIG_FRONTDESK_SERVICE_URL:http://frontdesk-service:8081}")
public interface InventoryClient {
  /**
   * Validates branch access using the internal-authenticated inventory endpoint.
   *
   * @param branchId branch to validate for the current tenant
   * @return validation response returned by frontdesk
   */
  @PostMapping("/api/v1/auth/branches/{branchId}/validate")
  Map<String, Object> validateBranch(@PathVariable("branchId") UUID branchId);

  /**
   * Consumes linked inventory items for an idempotently identified POS order.
   *
   * @param orderId POS order identifier used for idempotency
   * @param request branch and aggregated product quantities to consume
   */
  @PostMapping("/api/v1/inventory/pos/orders/{orderId}/consume")
  void consume(@PathVariable("orderId") UUID orderId, @RequestBody ConsumptionRequest request);

  /**
   * Inventory consumption payload scoped to the order's branch.
   *
   * @param branchId branch where the order consumes stock
   * @param lines aggregated product quantities
   */
  record ConsumptionRequest(UUID branchId, List<ConsumptionLine> lines) {
    /**
     * Copies lines so callers cannot mutate this request after construction.
     *
     * @param branchId branch where the order consumes stock
     * @param lines aggregated product quantities
     */
    public ConsumptionRequest {
      lines = lines == null ? null : List.copyOf(lines);
    }
  }

  /**
   * One product and quantity in a POS consumption request.
   *
   * @param productId inventory product identifier
   * @param quantity positive quantity to consume
   */
  record ConsumptionLine(UUID productId, long quantity) { }
}
