package com.hotelpms.monolith.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.billing.service.InvoiceService;
import com.hotelpms.billing.service.PaymentService;
import com.hotelpms.fb.client.BillingClient;
import com.hotelpms.fb.client.InventoryClient;
import com.hotelpms.fb.client.StayClient;
import com.hotelpms.fb.client.dto.ChargeResponse;
import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.stays.service.StayService;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/** Direct adapters that keep POS, inventory, stays, and billing in the same Spring process. */
@Configuration(proxyBeanMethods = false)
public class FoodAndBeverageInProcessClients {

  @Bean
  StayClient monolithFbStayClient(final StayService stays, final ObjectMapper mapper) {
    return stayId -> convert(stays.getStayById(stayId, tenantId()),
        com.hotelpms.fb.client.dto.StayResponse.class, mapper);
  }

  @Bean
  InventoryClient monolithFbInventoryClient(
      final BranchValidationService branches, final InventoryService inventory,
      final ObjectMapper mapper) {
    return new InventoryClient() {
      @Override
      public java.util.Map<String, Object> validateBranch(final UUID branchId) {
        branches.requireActiveBranch(tenantId(), branchId);
        return java.util.Map.of("id", branchId, "hotelId", tenantId(), "active", true);
      }

      @Override
      public void consume(final UUID orderId, final ConsumptionRequest request) {
        final var lines = request.lines().stream()
            .map(line -> new InventoryService.PosConsumptionLine(line.productId(), line.quantity()))
            .toList();
        inventory.consumePosOrder(tenantId(), orderId, request.branchId(), lines);
      }
    };
  }

  @Bean
  BillingClient monolithFbBillingClient(
      final InvoiceService invoices, final PaymentService payments, final ObjectMapper mapper) {
    return new BillingClient() {
      @Override
      public ChargeResponse addCharge(final UUID stayId,
          final com.hotelpms.fb.client.dto.ChargeRequest request) {
        final var result = invoices.addCharge(stayId,
            convert(request, com.hotelpms.billing.dto.ChargeRequest.class, mapper));
        return new ChargeResponse(result.id());
      }

      @Override
      public ChargeResponse addPayment(final UUID stayId,
          final com.hotelpms.fb.client.dto.PaymentRequest request) {
        final var result = payments.addPaymentForStay(stayId,
            convert(request, com.hotelpms.billing.dto.PaymentRequest.class, mapper));
        return new ChargeResponse(result.id());
      }

      @Override
      public ChargeResponse refund(final UUID stayId, final UUID paymentId,
          final com.hotelpms.fb.client.dto.RefundRequest request) {
        final var result = payments.refundForStay(stayId, paymentId,
            convert(request, com.hotelpms.billing.dto.RefundRequest.class, mapper));
        return new ChargeResponse(result.id());
      }
    };
  }

  private static UUID tenantId() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getDetails() instanceof String tenant)) {
      throw new AccessDeniedException("TENANT_CONTEXT_REQUIRED");
    }
    return UUID.fromString(tenant);
  }

  private static <T> T convert(final Object source, final Class<T> target, final ObjectMapper mapper) {
    return mapper.convertValue(source, target);
  }
}
