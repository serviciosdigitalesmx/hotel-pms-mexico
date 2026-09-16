package com.hotelpms.frontdesk.whatsapp;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WhatsAppControllerTest {
    private AnnotationConfigApplicationContext context;
    private WhatsAppController controller;
    private EvolutionService service;

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        EvolutionService evolution() {
            return mock(EvolutionService.class);
        }

        @Bean
        WhatsAppController controller(final EvolutionService service) {
            return new WhatsAppController(service);
        }
    }

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        controller = context.getBean(WhatsAppController.class);
        service = context.getBean(EvolutionService.class);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void receptionistCannotReadOrGenerateQr() {
        authenticate("RECEPTIONIST", UUID.randomUUID());
        assertThrows(AccessDeniedException.class, controller::status);
        assertThrows(AccessDeniedException.class, controller::connect);
        verifyNoInteractions(service);
    }

    @Test
    void ownerUsesAuthenticatedTenantAndQrCannotBeCached() {
        final UUID hotel = UUID.randomUUID();
        authenticate("OWNER", hotel);
        when(service.connect(hotel)).thenReturn(new EvolutionService.Connection("connecting", null));
        assertEquals("no-store", controller.connect().getHeaders().getCacheControl());
        verify(service).connect(hotel);
    }

    private static void authenticate(final String role, final UUID hotel) {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "staff", null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authentication.setDetails(hotel.toString());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
