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

/** Applies deposits without confusing order value with money collected. */
@Service
@RequiredArgsConstructor
public class DepositService {
  private final DepositRepository deposits;
  private final InvoiceRepository invoices;

  @Transactional
  public Deposit receive(final UUID hotelId, final UUID invoiceId, final DepositRequest request) {
    final Deposit existing = deposits.findByHotelIdAndReferenceKey(hotelId, request.referenceKey()).orElse(null);
    if (existing != null) {
      return existing;
    }
    final Invoice invoice = invoices.findById(invoiceId).filter(i -> hotelId.equals(i.getHotelId()))
        .orElseThrow(() -> new IllegalArgumentException("INVOICE_NOT_FOUND"));
    final BigDecimal paid = invoice.getPayments().stream().map(p -> p.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (paid.add(request.amount()).compareTo(invoice.getTotalAmount()) > 0) {
      throw new IllegalArgumentException("DEPOSIT_EXCEEDS_BALANCE");
    }
    final Deposit deposit = new Deposit();
    deposit.setHotelId(hotelId);
    deposit.setInvoiceId(invoiceId);
    deposit.setAmount(request.amount());
    deposit.setReferenceKey(request.referenceKey());
    return deposits.save(deposit);
  }
}
