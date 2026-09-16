package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.service.HotelOnboardingService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PlatformHotelControllerTest {
    private AnnotationConfigApplicationContext context;
    private PlatformHotelController controller;
    private HotelOnboardingService onboarding;
    private static final UUID ROOT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final CreateHotelRequest REQUEST = new CreateHotelRequest(
            "Hotel SB", "hotel-sb", "sb-owner", "owner@sb.example", "Initial123");

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean HotelOnboardingService onboarding() { return mock(HotelOnboardingService.class); }
        @Bean PlatformHotelController controller(final HotelOnboardingService service) {
            return new PlatformHotelController(service);
        }
    }

    @BeforeEach
    void start() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        controller = context.getBean(PlatformHotelController.class);
        onboarding = context.getBean(HotelOnboardingService.class);
    }

    @AfterEach
    void stop() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void hotelAdminCannotCreateAnotherTenant() {
        authenticate("ADMIN");
        assertThrows(AccessDeniedException.class, () -> controller.create(UUID.randomUUID(), REQUEST));
        verifyNoInteractions(onboarding);
    }

    @Test
    void rootOwnerCannotCreateAnotherTenant() {
        authenticate("OWNER");
        assertThrows(AccessDeniedException.class, () -> controller.create(ROOT, REQUEST));
        verifyNoInteractions(onboarding);
    }

    private static void authenticate(final String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "operator", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
