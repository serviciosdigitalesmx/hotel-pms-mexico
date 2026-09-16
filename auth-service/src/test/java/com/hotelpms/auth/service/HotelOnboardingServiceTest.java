package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.Role;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelOnboardingServiceTest {
    private static final String HOTEL_SLUG = "hotel-sb";
    private static final CreateHotelRequest REQUEST = new CreateHotelRequest(
            "Hotel SB", HOTEL_SLUG, "sb-owner", "owner@sb.example", "Initial123");

    @Mock private HotelRegistryRepository hotels;
    @Mock private UserAccountRepository users;
    @Mock private PasswordEncoder passwords;
    @InjectMocks private HotelOnboardingService onboarding;

    @Test
    void createsNewTenantWithOwnerForcedToChangePassword() {
        when(passwords.encode("Initial123")).thenReturn("encoded");
        final var result = onboarding.create(REQUEST);
        final var hotelCaptor = org.mockito.ArgumentCaptor.forClass(HotelRegistry.class);
        final var ownerCaptor = org.mockito.ArgumentCaptor.forClass(UserAccount.class);
        verify(hotels).save(hotelCaptor.capture());
        verify(users).save(ownerCaptor.capture());
        assertEquals(HOTEL_SLUG, result.slug());
        assertEquals(result.id(), ownerCaptor.getValue().getHotelId());
        assertNotEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), result.id());
        assertEquals(Role.OWNER, ownerCaptor.getValue().getRole());
        assertEquals("encoded", ownerCaptor.getValue().getPasswordHash());
        assertTrue(ownerCaptor.getValue().isMustChangePassword());
    }

    @Test
    void duplicateSlugCreatesNoOwner() {
        when(hotels.existsBySlug(HOTEL_SLUG)).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> onboarding.create(REQUEST));
        verify(users, never()).save(any());
    }
}
