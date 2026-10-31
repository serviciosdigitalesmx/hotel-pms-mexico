package com.hotelpms.frontdesk.serviceorder.security;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Removes financial fields from Fixi order responses for technician principals. */
@ControllerAdvice
@ConditionalOnProperty(name = "fixi.authorization.assigned-orders.enabled", havingValue = "true")
public class FixiTechnicianOrderResponseAdvice implements ResponseBodyAdvice<Object> {
  @Override
  public boolean supports(
      final MethodParameter returnType,
      final Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      final Object body,
      final MethodParameter returnType,
      final MediaType selectedContentType,
      final Class<? extends HttpMessageConverter<?>> selectedConverterType,
      final ServerHttpRequest request,
      final ServerHttpResponse response) {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (body == null || authentication == null || authentication.getAuthorities().stream()
        .noneMatch(authority -> "ROLE_TECHNICIAN".equals(authority.getAuthority()))) {
      return body;
    }
    if (body instanceof ServiceOrder order) {
      return redact(order);
    }
    if (body instanceof Page<?> page && page.getContent().stream().anyMatch(ServiceOrder.class::isInstance)) {
      return page.map(item -> item instanceof ServiceOrder order ? redact(order) : item);
    }
    if (body instanceof Collection<?> collection
        && collection.stream().anyMatch(ServiceOrder.class::isInstance)) {
      return collection.stream().map(item -> item instanceof ServiceOrder order ? redact(order) : item).toList();
    }
    return body;
  }

  static ServiceOrder redact(final ServiceOrder source) {
    final ServiceOrder safe = new ServiceOrder();
    org.springframework.beans.BeanUtils.copyProperties(source, safe);
    safe.setDepositAmount(null);
    safe.setLaborAmount(null);
    safe.setPartsAmount(null);
    safe.setTaxAmount(null);
    safe.setQuoteTotal(null);
    safe.setPaidAmount(null);
    safe.setPaymentMethod(null);
    safe.setPaymentStatus(null);
    safe.setPaymentEvents(List.of());
    return safe;
  }
}
