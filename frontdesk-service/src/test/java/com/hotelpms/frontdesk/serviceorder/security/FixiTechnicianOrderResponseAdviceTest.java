package com.hotelpms.frontdesk.serviceorder.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FixiTechnicianOrderResponseAdviceTest {
  @Test
  void redactsFinancialFieldsWithoutChangingOperationalOrderData() {
    final ServiceOrder order = ServiceOrder.builder()
        .summary("Revisión de equipo")
        .depositAmount(BigDecimal.TEN)
        .laborAmount(BigDecimal.TEN)
        .partsAmount(BigDecimal.TEN)
        .taxAmount(BigDecimal.TEN)
        .quoteTotal(BigDecimal.TEN)
        .paidAmount(BigDecimal.TEN)
        .paymentMethod("CASH")
        .paymentStatus("PAID")
        .paymentEvents(List.of(java.util.Map.of("type", "PAYMENT")))
        .build();

    final ServiceOrder safe = FixiTechnicianOrderResponseAdvice.redact(order);

    assertThat(safe.getSummary()).isEqualTo("Revisión de equipo");
    assertThat(safe.getDepositAmount()).isNull();
    assertThat(safe.getLaborAmount()).isNull();
    assertThat(safe.getPartsAmount()).isNull();
    assertThat(safe.getTaxAmount()).isNull();
    assertThat(safe.getQuoteTotal()).isNull();
    assertThat(safe.getPaidAmount()).isNull();
    assertThat(safe.getPaymentMethod()).isNull();
    assertThat(safe.getPaymentStatus()).isNull();
    assertThat(safe.getPaymentEvents()).isEmpty();
  }
}
