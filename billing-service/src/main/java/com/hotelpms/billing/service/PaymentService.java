package com.hotelpms.billing.service;

import com.hotelpms.billing.dto.PaymentRequest;
import com.hotelpms.billing.dto.PaymentResponse;
import com.hotelpms.billing.dto.RefundRequest;
import java.util.UUID;
import org.springframework.lang.NonNull;

/** Service interface for managing Payments. */
public interface PaymentService {

  /**
   * Adds a payment to a specific invoice.
   *
   * @param invoiceId the invoice UUID
   * @param request the payment request
   * @return the recorded payment response
   */
  PaymentResponse addPayment(@NonNull UUID invoiceId, @NonNull PaymentRequest request);

  /**
   * Adds a payment to a stay invoice.
   *
   * @param stayId stay identifier.
   * @param request payment request.
   * @return recorded payment response.
   */
  PaymentResponse addPaymentForStay(@NonNull UUID stayId, @NonNull PaymentRequest request);

  /**
   * Reverses part or all of a payment while preserving the original transaction.
   *
   * @param invoiceId invoice identifier.
   * @param paymentId payment identifier.
   * @param request refund request.
   * @return refund response.
   */
  PaymentResponse refund(
      @NonNull UUID invoiceId, @NonNull UUID paymentId, @NonNull RefundRequest request);

  /**
   * Refunds a payment associated with a stay.
   *
   * @param stayId stay identifier.
   * @param paymentId payment identifier.
   * @param request refund request.
   * @return refund response.
   */
  PaymentResponse refundForStay(
      @NonNull UUID stayId, @NonNull UUID paymentId, @NonNull RefundRequest request);
}
