package com.hotelpms.billing.controller;

import com.hotelpms.billing.dto.PaymentRequest;
import com.hotelpms.billing.dto.PaymentResponse;
import com.hotelpms.billing.service.PaymentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Payment endpoint resolved through a tenant-scoped stay invoice. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stays")
public class StayPaymentController {
  private final PaymentService paymentService;

  /**
   * Adds a payment to the invoice associated with a stay.
   *
   * @param stayId stay identifier.
   * @param request payment request.
   *
   * @return created payment response.
   */
  @PostMapping("/{stayId}/payments")
  public ResponseEntity<PaymentResponse> addPayment(
      @PathVariable final UUID stayId, @Valid @RequestBody final PaymentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(paymentService.addPaymentForStay(stayId, request));
  }
}
