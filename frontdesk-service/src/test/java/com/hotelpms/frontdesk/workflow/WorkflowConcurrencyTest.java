package com.hotelpms.frontdesk.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.workflow.domain.SemanticPhase;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddStateRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddTransitionRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.CreateWorkflowRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.repository.WorkflowDefinitionRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowInstanceRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowStateRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionAuditRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionRepository;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;

import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

@Testcontainers(disabledWithoutDocker = true)
class WorkflowConcurrencyTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String RECEIVED_STATE = "received";
    private static final String DIAGNOSING_STATE = "diagnosing";
    private static final int WAIT_SECONDS = 10;
    private static final int RESULT_TIMEOUT_SECONDS = 30;

    private static AnnotationConfigApplicationContext context;

    @BeforeAll
    static void database() throws SQLException {
        try (var connection =
                        DriverManager.getConnection(
                                POSTGRES.getJdbcUrl(),
                                POSTGRES.getUsername(),
                                POSTGRES.getPassword())) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new FileSystemResource(
                            "src/main/resources/db/migration/V25__add_configurable_workflows.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new FileSystemResource(
                            "src/main/resources/db/migration/V26__enforce_workflow_instance_identity.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new FileSystemResource(
                            "src/main/resources/db/migration/V52__add_workflow_state_activation.sql"));
        }
        context = new AnnotationConfigApplicationContext(DatabaseConfig.class);
    }

    @AfterAll
    static void close() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void concurrentTransitionsApplyExactlyOnceAndPersistOneAudit()
            throws InterruptedException, ExecutionException, TimeoutException {
        final WorkflowService service = context.getBean(WorkflowService.class);
        final UUID tenant = UUID.randomUUID();
        final UUID id = service.create(tenant, new CreateWorkflowRequest("repair", "Repair")).getId();
        service.addState(
                tenant,
                id,
                new AddStateRequest(RECEIVED_STATE, "Received", SemanticPhase.INTAKE, true, false));
        service.addState(
                tenant,
                id,
                new AddStateRequest(
                        DIAGNOSING_STATE, "Diagnosing", SemanticPhase.DIAGNOSIS, false, false));
        service.addTransition(
                tenant,
                id,
                new AddTransitionRequest(
                        RECEIVED_STATE, DIAGNOSING_STATE, "diagnose", "workflow.transition"));
        final UUID aggregate = UUID.randomUUID();
        final TransitionRequest request =
                new TransitionRequest(aggregate, RECEIVED_STATE, "diagnose");
        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch start = new CountDownLatch(1);
        final Callable<Boolean> action =
                () -> {
                    ready.countDown();
                    if (!start.await(WAIT_SECONDS, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start timed out");
                    }
                    try {
                        service.transition(tenant, id, request, "technician");
                        return true;
                    } catch (final ConflictException expected) {
                        return false;
                    }
                };
        try (var pool = Executors.newFixedThreadPool(2)) {
            final Future<Boolean> first = pool.submit(action);
            final Future<Boolean> second = pool.submit(action);
            assertTrue(ready.await(WAIT_SECONDS, TimeUnit.SECONDS));
            start.countDown();
            assertNotEquals(
                    first.get(RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    second.get(RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }
        assertEquals(
                DIAGNOSING_STATE,
                context.getBean(WorkflowInstanceRepository.class)
                        .findByHotelIdAndWorkflowIdAndAggregateId(tenant, id, aggregate)
                        .orElseThrow()
                        .getStateKey());
        assertEquals(
                1,
                context.getBean(WorkflowTransitionAuditRepository.class)
                        .findAllByHotelIdOrderByCreatedAtDesc(tenant)
                        .size());
        assertTrue(
                context.getBean(WorkflowInstanceRepository.class)
                        .findByHotelIdAndWorkflowIdAndAggregateId(UUID.randomUUID(), id, aggregate)
                        .isEmpty());
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = WorkflowInstanceRepository.class)
    static class DatabaseConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
            final var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.hotelpms.frontdesk.workflow.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(final EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        WorkflowService workflowService(
                final WorkflowDefinitionRepository definitions,
                final WorkflowStateRepository states,
                final WorkflowTransitionRepository transitions,
                final WorkflowTransitionAuditRepository audits,
                final WorkflowInstanceRepository instances) {
            return new WorkflowService(definitions, states, transitions, audits, instances);
        }
    }
}
