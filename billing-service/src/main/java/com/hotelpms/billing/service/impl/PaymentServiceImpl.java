package com.hotelpms.billing.service.impl;

import com.hotelpms.billing.domain.CashMovement;
import com.hotelpms.billing.domain.CashMovementType;
import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.domain.CashSessionStatus;
import com.hotelpms.billing.domain.Deposit;
import com.hotelpms.billing.domain.Invoice;
import com.hotelpms.billing.domain.InvoiceStatus;
import com.hotelpms.billing.domain.LedgerEntry;
import com.hotelpms.billing.domain.Payment;
import com.hotelpms.billing.domain.PaymentMethod;
import com.hotelpms.billing.dto.PaymentRequest;
import com.hotelpms.billing.dto.PaymentResponse;
import com.hotelpms.billing.dto.RefundRequest;
import com.hotelpms.billing.exception.BillingValidationException;
import com.hotelpms.billing.exception.InvoiceConflictException;
import com.hotelpms.billing.exception.NotFoundException;
import com.hotelpms.billing.mapper.PaymentMapper;
import com.hotelpms.billing.repository.CashSessionRepository;
import com.hotelpms.billing.repository.DepositRepository;
import com.hotelpms.billing.repository.InvoiceFiscalExportRepository;
import com.hotelpms.billing.repository.InvoiceRepository;
import com.hotelpms.billing.repository.PaymentRepository;
import com.hotelpms.billing.service.PaymentService;
import com.hotelpms.billing.service.LedgerService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

/** Implementation of the PaymentService interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private static final String INVOICE_NOT_FOUND = "INVOICE_NOT_FOUND";

  private final PaymentRepository paymentRepository;
  private final InvoiceRepository invoiceRepository;
  private final PaymentMapper paymentMapper;
  private final InvoiceFiscalExportRepository invoiceFiscalExportRepository;
  private final CashSessionRepository cashSessionRepository;
  private final DepositRepository depositRepository;
  private final LedgerService ledgerService;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public PaymentResponse addPayment(
      @NonNull final UUID invoiceId, @NonNull final PaymentRequest request) {
    final UUID hotelId = resolveHotelId();
    final Invoice invoice =
        invoiceRepository
            .findByIdAndHotelId(invoiceId, hotelId)
            .orElseThrow(() -> new NotFoundException(INVOICE_NOT_FOUND));

    if (request.transactionReference() != null && !request.transactionReference().isBlank()) {
      final var existing = paymentRepository.findByInvoiceIdAndTransactionReference(
          invoiceId, request.transactionReference());
      if (existing.isPresent()) {
        if (existing.get().getAmount().compareTo(request.amount().setScale(2, RoundingMode.HALF_UP)) != 0
            || existing.get().getPaymentMethod() != request.paymentMethod()) {
          throw new BillingValidationException("PAYMENT_REFERENCE_CONFLICT");
        }
        return paymentMapper.toResponse(existing.get());
      }
    }

    final BigDecimal paymentAmount = request.amount().setScale(2, RoundingMode.HALF_UP);

    if (invoice.getStatus() == InvoiceStatus.PAID) {
      log.warn(
          "[BILLING] PAYMENT_REJECTED | invoiceId={} | hotelId={} | reason=INVOICE_ALREADY_PAID",
          invoiceId,
          hotelId);
      throw new BillingValidationException("INVOICE_ALREADY_PAID");
    }

    if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
      log.warn(
          "[BILLING] PAYMENT_REJECTED | invoiceId={} | hotelId={} | reason=INVOICE_CANCELLED",
          invoiceId,
          hotelId);
      throw new BillingValidationException("INVOICE_CANCELLED");
    }

    // A payment changes the invoice's PAID/ISSUED status, a fiscally-relevant field
    // once a FatturaPA export exists — same guard as addCharge/updateDocumentType in
    // InvoiceServiceImpl, kept here as its own check because PaymentServiceImpl is a
    // separate class with its own Invoice lookup (T-BILL-06/round 2 bug #3).
    if (invoiceFiscalExportRepository.existsByInvoiceId(invoice.getId())) {
      log.warn(
          "[BILLING] PAYMENT_REJECTED | invoiceId={} | hotelId={} |"
              + " reason=INVOICE_LOCKED_AFTER_EXPORT",
          invoiceId,
          hotelId);
      throw new InvoiceConflictException("INVOICE_LOCKED_AFTER_EXPORT");
    }

    final BigDecimal currentTotalPaid =
        invoice.getPayments().stream()
            .map((@NonNull Payment p) -> p.getAmount().subtract(p.getReversedAmount()))
            .reduce(BigDecimal.ZERO, (@NonNull BigDecimal a, @NonNull BigDecimal b) -> a.add(b))
            .add(depositRepository.findAllByHotelIdAndInvoiceIdAndStatusIn(hotelId, invoiceId,
                java.util.List.of(Deposit.Status.RECEIVED, Deposit.Status.APPLIED)).stream()
                .map(Deposit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

    final BigDecimal balanceDue = invoice.getTotalAmount().subtract(currentTotalPaid);

    if (paymentAmount.compareTo(balanceDue) > 0) {
      log.warn(
          "[BILLING] PAYMENT_REJECTED | invoiceId={} | hotelId={} | reason=PAYMENT_EXCEEDS_BALANCE"
              + " | amount={} | balanceDue={}",
          invoiceId,
          hotelId,
          paymentAmount,
          balanceDue);
      throw new BillingValidationException("PAYMENT_EXCEEDS_BALANCE");
    }

    final CashSession cashSession =
        request.paymentMethod() == PaymentMethod.CASH
            ? cashSessionRepository
                .findFirstByHotelIdAndStatusOrderByOpenedAtDesc(hotelId, CashSessionStatus.OPEN)
                .orElseThrow(() -> new BillingValidationException("CASH_SESSION_NOT_OPEN"))
            : null;

    final Payment payment = paymentMapper.toEntity(request);
    payment.setAmount(paymentAmount);
    payment.setPaymentDate(LocalDateTime.now());

    invoice.addPayment(payment);
    final Payment savedPayment;
    try {
      savedPayment = paymentRepository.saveAndFlush(payment);
    } catch (final DataIntegrityViolationException conflict) {
      if (request.transactionReference() == null || request.transactionReference().isBlank()) throw conflict;
      final Payment replay = paymentRepository.findByInvoiceIdAndTransactionReference(
          invoiceId, request.transactionReference()).orElseThrow(() -> conflict);
      if (replay.getAmount().compareTo(paymentAmount) != 0
          || replay.getPaymentMethod() != request.paymentMethod()) {
        throw new BillingValidationException("PAYMENT_REFERENCE_CONFLICT", conflict);
      }
      return paymentMapper.toResponse(replay);
    }
    ledgerService.record(hotelId, invoiceId, LedgerEntry.EntryType.PAYMENT, paymentAmount,
        "payment:" + savedPayment.getId());

    if (cashSession != null) {
      cashSession
          .getMovements()
          .add(
              CashMovement.builder()
                  .session(cashSession)
                  .type(CashMovementType.CASH_IN)
                  .amount(paymentAmount)
                  .reference(request.transactionReference())
                  .createdBy(resolveActorId())
                  .createdAt(LocalDateTime.now())
                  .build());
      cashSessionRepository.save(cashSession);
    }

    log.info(
        "[BILLING] PAYMENT_ADDED | invoiceId={} | paymentId={} | amount={} | method={} |"
            + " hotelId={}",
        invoiceId,
        savedPayment.getId(),
        paymentAmount,
        request.paymentMethod(),
        hotelId);

    // Check if fully paid
    final BigDecimal newTotalPaid = currentTotalPaid.add(paymentAmount);
    if (newTotalPaid.compareTo(invoice.getTotalAmount()) == 0) {
      log.info(
          "[BILLING] INVOICE_PAID | invoiceId={} | totalAmount={} | hotelId={}",
          invoiceId,
          invoice.getTotalAmount(),
          hotelId);
      invoice.setStatus(InvoiceStatus.PAID);
    }

    invoiceRepository.save(invoice); // Save cascaded payment addition and potential status change

    return paymentMapper.toResponse(savedPayment);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public PaymentResponse addPaymentForStay(
      @NonNull final UUID stayId, @NonNull final PaymentRequest request) {
    final UUID hotelId = resolveHotelId();
    final Invoice invoice =
        invoiceRepository
            .findByStayIdAndHotelId(stayId, hotelId)
            .orElseThrow(() -> new NotFoundException(INVOICE_NOT_FOUND));
    return addPayment(invoice.getId(), request);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public PaymentResponse refund(
      @NonNull final UUID invoiceId,
      @NonNull final UUID paymentId,
      @NonNull final RefundRequest request) {
    final UUID hotelId = resolveHotelId();
    final Invoice invoice =
        invoiceRepository
            .findByIdAndHotelId(invoiceId, hotelId)
            .orElseThrow(() -> new NotFoundException(INVOICE_NOT_FOUND));
    final Payment payment =
        invoice.getPayments().stream()
            .filter(candidate -> paymentId.equals(candidate.getId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("PAYMENT_NOT_FOUND"));
    if (request.reference() != null && request.reference().equals(payment.getReversalReference())) {
      return paymentMapper.toResponse(payment);
    }
    final BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
    final BigDecimal available = payment.getAmount().subtract(payment.getReversedAmount());
    if (amount.compareTo(available) > 0) {
      throw new BillingValidationException("REFUND_EXCEEDS_PAYMENT");
    }
    payment.setReversedAmount(payment.getReversedAmount().add(amount));
    payment.setReversalReference(request.reference());
    paymentRepository.save(payment);
    ledgerService.record(hotelId, invoiceId, LedgerEntry.EntryType.REFUND, amount,
        "refund:" + paymentId + ":" + request.reference());
    final BigDecimal collected =
        invoice.getPayments().stream()
            .map(item -> item.getAmount().subtract(item.getReversedAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(depositRepository.findAllByHotelIdAndInvoiceIdAndStatusIn(hotelId, invoiceId,
                java.util.List.of(Deposit.Status.RECEIVED, Deposit.Status.APPLIED)).stream()
                .map(Deposit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    if (collected.compareTo(invoice.getTotalAmount()) < 0
        && invoice.getStatus() == InvoiceStatus.PAID) {
      invoice.setStatus(InvoiceStatus.ISSUED);
      invoiceRepository.save(invoice);
    }
    return paymentMapper.toResponse(payment);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public PaymentResponse refundForStay(
      @NonNull final UUID stayId,
      @NonNull final UUID paymentId,
      @NonNull final RefundRequest request) {
    final UUID hotelId = resolveHotelId();
    final Invoice invoice =
        invoiceRepository
            .findByStayIdAndHotelId(stayId, hotelId)
            .orElseThrow(() -> new NotFoundException(INVOICE_NOT_FOUND));
    return refund(invoice.getId(), paymentId, request);
  }

  /**
   * Extracts the hotel UUID from the current authentication context. The hotel ID is stored as
   * {@code details} by {@link com.hotelpms.internalauth.security.InternalAuthFilter} after reading
   * the {@code X-Auth-Hotel} header injected by the API Gateway.
   *
   * @return the hotel UUID of the authenticated caller
   * @throws IllegalStateException if the security context is missing or malformed
   */
  private UUID resolveHotelId() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getDetails() instanceof String hotelIdStr)) {
      throw new IllegalStateException("MISSING_HOTEL_CONTEXT");
    }
    return UUID.fromString(hotelIdStr);
  }

  private UUID resolveActorId() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return UUID.nameUUIDFromBytes(auth.getName().getBytes(StandardCharsets.UTF_8));
  }
}
