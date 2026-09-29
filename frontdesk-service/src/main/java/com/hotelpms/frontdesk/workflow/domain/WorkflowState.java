package com.hotelpms.frontdesk.workflow.domain;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="workflow_states") public class WorkflowState {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="workflow_id",nullable=false) private WorkflowDefinition workflow;
 @Column(name="state_key",nullable=false) private String key; @Column(nullable=false) private String label; @Enumerated(EnumType.STRING) @Column(name="semantic_phase",nullable=false) private SemanticPhase semanticPhase; @Column(name="initial_state") private boolean initialState; @Column(name="terminal_state") private boolean terminalState;
 protected WorkflowState() {} public WorkflowState(WorkflowDefinition w,String key,String label,SemanticPhase phase,boolean initial,boolean terminal){this.workflow=w;this.key=key;this.label=label;this.semanticPhase=phase;this.initialState=initial;this.terminalState=terminal;}
 public UUID getId(){return id;} public WorkflowDefinition getWorkflow(){return workflow;} public String getKey(){return key;} public String getLabel(){return label;} public SemanticPhase getSemanticPhase(){return semanticPhase;} public boolean isInitialState(){return initialState;} public boolean isTerminalState(){return terminalState;}
}
