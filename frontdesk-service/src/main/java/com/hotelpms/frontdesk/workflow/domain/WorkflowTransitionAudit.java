package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Type domain type. */
@Entity
@Table(name = "workflow_transition_audit")
public class WorkflowTransitionAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "workflow_id", nullable = false)
  private UUID workflowId;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "from_state_key", nullable = false)
  private String fromStateKey;

  @Column(name = "to_state_key", nullable = false)
  private String toStateKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "semantic_phase", nullable = false)
  private SemanticPhase semanticPhase;

  @Column(name = "actor_id", nullable = false)
  private String actorId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** Required by JPA. */
  protected WorkflowTransitionAudit() { /* Required by JPA. */ }
  /**
   * Executes the operation.
   *
   * @param h parameter.
   *
   * @param w parameter.
   *
   * @param a parameter.
   *
   * @param f parameter.
   *
   * @param t parameter.
   *
   * @param p parameter.
   *
   * @param actor parameter.
   *
   */

  public WorkflowTransitionAudit(
      final UUID h, final UUID w, final UUID a, final String f, final String t, final SemanticPhase p, final String actor) {
    hotelId = h;
    workflowId = w;
    aggregateId = a;
    fromStateKey = f;
    toStateKey = t;
    semanticPhase = p;
    actorId = actor;
    createdAt = Instant.now();
  }
}
