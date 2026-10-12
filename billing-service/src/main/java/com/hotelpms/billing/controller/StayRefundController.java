package com.hotelpms.billing.controller;

import com.hotelpms.billing.dto.PaymentResponse;
import com.hotelpms.billing.dto.RefundRequest;
import com.hotelpms.billing.service.PaymentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-scoped payment reversal resolved through a stay invoice. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stays")
public class StayRefundController {
  private final PaymentService paymentService;

  /**
   * Refunds a payment associated with a stay.
   *
   * @param stayId stay identifier.
   * @param paymentId payment identifier.
   * @param request refund request.
   *
   * @return refund response.
   */
  @PostMapping("/{stayId}/payments/{paymentId}/refund")
  public ResponseEntity<PaymentResponse> refund(
      @PathVariable final UUID stayId,
      @PathVariable final UUID paymentId,
      @Valid @RequestBody final RefundRequest request) {
    return ResponseEntity.ok(paymentService.refundForStay(stayId, paymentId, request));
  }
}
