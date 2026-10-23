package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.dto.PlatformTenantSummary;
import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import com.hotelpms.auth.service.HotelOnboardingService;
import com.hotelpms.auth.service.PlatformAdministrationService;
import com.hotelpms.auth.service.PlatformAdminGrantGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlatformHotelControllerTest {
    private static final CreateHotelRequest REQUEST = new CreateHotelRequest(
            "Hotel SB", "hotel-sb", "sb-owner", "owner@sb.example", "Initial123");
    private static final PlatformTenantSummary CREATED = new PlatformTenantSummary(
            UUID.fromString("00000000-0000-0000-0000-000000000123"), "Hotel SB", "hotel-sb",
            "ACTIVE", Instant.parse("2026-01-01T00:00:00Z"), "sb-owner", null,
            "owner@sb.example", null, false, false);

    private AnnotationConfigApplicationContext context;
    private PlatformHotelController controller;
    private HotelOnboardingService onboarding;
    private PlatformAdministrationService platform;
    private PlatformAdminGrantRepository grants;

    @BeforeEach
    void start() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        controller = context.getBean(PlatformHotelController.class);
        onboarding = context.getBean(HotelOnboardingService.class);
        platform = context.getBean(PlatformAdministrationService.class);
        grants = context.getBean(PlatformAdminGrantRepository.class);
    }

    @AfterEach
    void stop() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void activePlatformGrantCanCreateTenant() {
        when(grants.existsByUser_UsernameAndActiveTrue("operator")).thenReturn(true);
        authenticate("ADMIN");
        when(platform.createTenant(REQUEST, "operator")).thenReturn(CREATED);

        final var response = controller.create(REQUEST, authentication("ADMIN"));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(CREATED.id(), response.getBody().id());
        verify(platform).createTenant(REQUEST, "operator");
    }

    @Test
    void tenantRolesAndNormalUsersCannotCreateTenantWithoutPlatformGrant() {
        when(grants.existsByUser_UsernameAndActiveTrue("operator")).thenReturn(false);
        for (final String role : List.of("OWNER", "ADMIN", "MANAGER", "TECHNICIAN", "USER")) {
            authenticate(role);
            assertThrows(AccessDeniedException.class,
                    () -> controller.create(REQUEST, authentication(role)), "role=" + role);
        }
        verifyNoInteractions(onboarding);
        verifyNoInteractions(platform);
    }

    private static void authenticate(final String role) {
        SecurityContextHolder.getContext().setAuthentication(authentication(role));
    }

    private static UsernamePasswordAuthenticationToken authentication(final String role) {
        return new UsernamePasswordAuthenticationToken(
                "operator", null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        HotelOnboardingService onboarding() {
            return mock(HotelOnboardingService.class);
        }

        @Bean
        PlatformAdministrationService platform() {
            return mock(PlatformAdministrationService.class);
        }

        @Bean
        PlatformAdminGrantRepository grants() {
            return mock(PlatformAdminGrantRepository.class);
        }

        @Bean
        PlatformAdminGrantGuard platformAdminGrantGuard(final PlatformAdminGrantRepository grants) {
            return new PlatformAdminGrantGuard(grants);
        }

        @Bean
        PlatformHotelController controller(
                final HotelOnboardingService service, final PlatformAdministrationService platform) {
            return new PlatformHotelController(service, platform);
        }
    }
}
