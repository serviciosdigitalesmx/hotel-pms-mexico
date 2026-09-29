package com.hotelpms.frontdesk.workflow;

import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.workflow.domain.*;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.*;
import com.hotelpms.frontdesk.workflow.repository.*;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.nio.file.*;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class WorkflowConcurrencyTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    private static AnnotationConfigApplicationContext context;

    @BeforeAll static void database() throws Exception {
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var statement = connection.createStatement()) {
            statement.execute(Files.readString(Path.of("src/main/resources/db/migration/V25__add_configurable_workflows.sql")));
        }
        context = new AnnotationConfigApplicationContext(DatabaseConfig.class);
    }

    @AfterAll static void close() { if (context != null) context.close(); }

    @Test void concurrentTransitionsApplyExactlyOnceAndPersistOneAudit() throws Exception {
        var service = context.getBean(WorkflowService.class);
        UUID tenant = UUID.randomUUID();
        UUID id = service.create(tenant, new CreateWorkflowRequest("repair", "Repair")).getId();
        service.addState(tenant, id, new AddStateRequest("received", "Received", SemanticPhase.INTAKE, true, false));
        service.addState(tenant, id, new AddStateRequest("diagnosing", "Diagnosing", SemanticPhase.DIAGNOSIS, false, false));
        service.addTransition(tenant, id, new AddTransitionRequest("received", "diagnosing", "diagnose", "workflow.transition"));
        UUID aggregate = UUID.randomUUID();
        var request = new TransitionRequest(aggregate, "received", "diagnose");
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Callable<Boolean> action = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
            try { service.transition(tenant, id, request, "technician"); return true; }
            catch (ConflictException expected) { return false; }
        };
        try (var pool = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = pool.submit(action), second = pool.submit(action);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            assertNotEquals(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        }
        assertEquals("diagnosing", context.getBean(WorkflowInstanceRepository.class)
                .findByHotelIdAndWorkflowIdAndAggregateId(tenant, id, aggregate).orElseThrow().getStateKey());
        assertEquals(1, context.getBean(WorkflowTransitionAuditRepository.class)
                .findAllByHotelIdOrderByCreatedAtDesc(tenant).size());
        assertTrue(context.getBean(WorkflowInstanceRepository.class)
                .findByHotelIdAndWorkflowIdAndAggregateId(UUID.randomUUID(), id, aggregate).isEmpty());
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = WorkflowInstanceRepository.class)
    static class DatabaseConfig {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.hotelpms.frontdesk.workflow.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean WorkflowService workflowService(WorkflowDefinitionRepository definitions, WorkflowStateRepository states,
                WorkflowTransitionRepository transitions, WorkflowTransitionAuditRepository audits,
                WorkflowInstanceRepository instances) {
            return new WorkflowService(definitions, states, transitions, audits, instances);
        }
    }
}
