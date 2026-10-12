package com.hotelpms.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aggregated financial report for the Owner/Admin Dashboard.
 *
 * @param startDate the start of the reporting period
 * @param endDate the end of the reporting period
 * @param totalRevenue sum of all invoice amounts in the period
 * @param totalInvoices total number of invoices in the period
 * @param paidInvoices number of invoices with status PAID
 * @param totalCollected sum of persisted payments in the period
 * @param totalOutstanding invoice value not yet collected
 * @param invoices list of all invoices in the period
 */
public record OwnerFinancialReportDto(
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalRevenue,
    BigDecimal totalCollected,
    BigDecimal totalOutstanding,
    long totalInvoices,
    long paidInvoices,
    List<InvoiceResponse> invoices) {

  /** Backward-compatible constructor for callers that only provide invoice totals. */
  public OwnerFinancialReportDto(
      final LocalDate startDate,
      final LocalDate endDate,
      final BigDecimal totalRevenue,
      final long totalInvoices,
      final long paidInvoices,
      final List<InvoiceResponse> invoices) {
    this(
        startDate,
        endDate,
        totalRevenue,
        BigDecimal.ZERO,
        totalRevenue,
        totalInvoices,
        paidInvoices,
        invoices);
  }

  /**
   * Make a defensive copy of the invoices list to protect internal representation.
   *
   * @param startDate the start of the reporting period
   * @param endDate the end of the reporting period
   * @param totalRevenue sum of all invoice amounts in the period
   * @param totalInvoices total number of invoices in the period
   * @param paidInvoices number of invoices with status PAID
   * @param invoices list of all invoices in the period
   */
  public OwnerFinancialReportDto {
    invoices = List.copyOf(invoices);
  }
}
