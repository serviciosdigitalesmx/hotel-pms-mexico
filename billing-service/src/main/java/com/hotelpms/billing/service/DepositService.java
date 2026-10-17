package com.hotelpms.billing.service;

import com.hotelpms.billing.domain.Deposit;
import com.hotelpms.billing.domain.Invoice;
import com.hotelpms.billing.dto.DepositRequest;
import com.hotelpms.billing.repository.DepositRepository;
import com.hotelpms.billing.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

/** Applies deposits without confusing order value with money collected. */
@Service
@RequiredArgsConstructor
public class DepositService {
  private final DepositRepository deposits;
  private final InvoiceRepository invoices;
  private final LedgerService ledger;

  /**
   * Records an idempotent deposit against an invoice without marking the invoice paid.
   *
   * @param hotelId tenant identifier
   * @param invoiceId invoice identifier
   * @param request validated deposit details
   * @return existing matching deposit or newly persisted deposit
   */
  @Transactional
  public Deposit receive(final UUID hotelId, final UUID invoiceId, final DepositRequest request) {
    final Deposit existing = deposits.findByHotelIdAndReferenceKey(hotelId, request.referenceKey()).orElse(null);
    if (existing != null) {
      if (!invoiceId.equals(existing.getInvoiceId())
          || existing.getAmount().compareTo(request.amount()) != 0) {
        throw new IllegalStateException("DEPOSIT_REFERENCE_CONFLICT");
      }
      return existing;
    }
    final Invoice invoice = invoices.findByIdAndHotelId(invoiceId, hotelId)
        .orElseThrow(() -> new IllegalArgumentException("INVOICE_NOT_FOUND"));
    final BigDecimal paid = invoice.getPayments().stream()
        .map(p -> p.getAmount().subtract(p.getReversedAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .add(deposits.findAllByHotelIdAndInvoiceIdAndStatusIn(hotelId, invoiceId,
            java.util.List.of(Deposit.Status.RECEIVED, Deposit.Status.APPLIED)).stream()
            .map(Deposit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    if (paid.add(request.amount()).compareTo(invoice.getTotalAmount()) > 0) {
      throw new IllegalArgumentException("DEPOSIT_EXCEEDS_BALANCE");
    }
    final Deposit deposit = new Deposit();
    deposit.setHotelId(hotelId);
    deposit.setInvoiceId(invoiceId);
    deposit.setAmount(request.amount());
    deposit.setReferenceKey(request.referenceKey());
    final Deposit saved;
    try {
      saved = deposits.saveAndFlush(deposit);
    } catch (final DataIntegrityViolationException conflict) {
      final Deposit replay = deposits.findByHotelIdAndReferenceKey(hotelId, request.referenceKey())
          .orElseThrow(() -> conflict);
      if (!invoiceId.equals(replay.getInvoiceId())
          || replay.getAmount().compareTo(request.amount()) != 0) {
        throw new IllegalStateException("DEPOSIT_REFERENCE_CONFLICT", conflict);
      }
      return replay;
    }
    ledger.record(hotelId, invoiceId,
        com.hotelpms.billing.domain.LedgerEntry.EntryType.PAYMENT,
        request.amount(), "deposit:" + saved.getId());
    return saved;
  }
}
