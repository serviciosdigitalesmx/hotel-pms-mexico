package com.hotelpms.frontdesk.serviceorder.domain;

/** ServiceOrderStatus domain type. */
public enum ServiceOrderStatus {
  INTAKE,
  REVIEW,
  DIAGNOSIS,
  QUOTED,
  QUOTE_REJECTED,
  AUTHORIZED,
  IN_PROGRESS,
  QA,
  COMPLETED,
  CANCELLED
}
