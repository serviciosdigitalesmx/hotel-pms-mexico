package com.hotelpms.frontdesk.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.workflow.controller.WorkflowController;
import com.hotelpms.frontdesk.workflow.domain.SemanticPhase;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.CreateWorkflowRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

@SpringJUnitConfig(WorkflowPermissionTest.SecurityConfig.class)
class WorkflowPermissionTest {
    @Autowired private WorkflowController controller;
    @Autowired private WorkflowService service;
    private final UUID tenant = UUID.randomUUID();

    @BeforeEach
    void resetService() {
        reset(service);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(final String... permissions) {
        final var auth =
                new UsernamePasswordAuthenticationToken(
                        "user", null, AuthorityUtils.createAuthorityList(permissions));
        auth.setDetails(tenant.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void ownerLabelCanCreateWithoutExplicitPermission() {
        authenticate("ROLE_OWNER");
        final var request = new CreateWorkflowRequest("repair", "Repair");
        when(service.create(tenant, request))
                .thenReturn(new WorkflowDefinition(tenant, "repair", "Repair"));
        assertEquals(201, controller.create(request).getStatusCode().value());
        verify(service).create(tenant, request);
    }

    @Test
    void adminLabelCanCreateWithoutExplicitPermission() {
        authenticate("ROLE_ADMIN");
        final var request = new CreateWorkflowRequest("repair", "Repair");
        when(service.create(tenant, request))
                .thenReturn(new WorkflowDefinition(tenant, "repair", "Repair"));
        assertEquals(201, controller.create(request).getStatusCode().value());
        verify(service).create(tenant, request);
    }

    @Test
    void receptionistCannotCreateWorkflow() {
        authenticate("ROLE_RECEPTIONIST");
        final var request = new CreateWorkflowRequest("repair", "Repair");
        assertThrows(AccessDeniedException.class, () -> controller.create(request));
        verifyNoInteractions(service);
    }

    @Test
    void ownerCanTransitionWhenTransitionSpecificPermissionIsAbsent() {
        authenticate("ROLE_OWNER", "workflow.transition");
        final UUID id = UUID.randomUUID();
        final var workflow = new WorkflowDefinition(tenant, "repair", "Repair");
        final var from =
                new WorkflowState(
                        workflow, "received", "Received", SemanticPhase.INTAKE, true, false);
        final var to =
                new WorkflowState(workflow, "working", "Working", SemanticPhase.WORK, false, false);
        when(service.transitions(tenant, id))
                .thenReturn(
                        List.of(
                                new WorkflowTransition(
                                        workflow, from, to, "start", "orders.execute")));
        final var request = new TransitionRequest(UUID.randomUUID(), "received", "start");
        final var candidate = new WorkflowTransition(workflow, from, to, "start", "orders.execute");
        when(service.transition(eq(tenant), eq(id), eq(request), anyString()))
                .thenReturn(candidate);
        assertDoesNotThrow(() -> controller.transition(id, request));
        verify(service).transition(eq(tenant), eq(id), eq(request), anyString());
    }

    @Configuration
    @EnableMethodSecurity
    static class SecurityConfig {
        @Bean
        WorkflowService service() {
            return mock(WorkflowService.class);
        }

        @Bean
        WorkflowController controller(final WorkflowService service) {
            return new WorkflowController(service);
        }
    }
}
