package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.Role;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.dto.HotelSummary;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Atomically creates a tenant registry record and its first owner identity. */
@Service
@RequiredArgsConstructor
public class HotelOnboardingService {
    private final HotelRegistryRepository hotels;
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;

    /**
     * Lists all registered hotels for a platform-authorized caller.
     *
     * @return hotel summaries
     */
    @Transactional(readOnly = true)
    public List<HotelSummary> list() {
        return hotels.findAll().stream().map(this::summary).toList();
    }

    /**
     * Atomically inserts the hotel and its owner.
     *
     * @param request onboarding details
     * @return created hotel summary
     */
    @Transactional
    public HotelSummary create(final CreateHotelRequest request) {
        if (hotels.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("HOTEL_SLUG_EXISTS");
        }
        if (users.existsByUsernameIncludingInactive(request.ownerUsername())) {
            throw new DuplicateResourceException("USERNAME_ALREADY_EXISTS");
        }
        if (users.existsByEmailIncludingInactive(request.ownerEmail())) {
            throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS");
        }
        final HotelRegistry hotel = new HotelRegistry();
        hotel.setId(UUID.randomUUID());
        hotel.setName(request.name().trim());
        hotel.setSlug(request.slug());
        hotel.setCreatedAt(LocalDateTime.now());
        hotels.save(hotel);

        users.save(UserAccount.builder()
                .username(request.ownerUsername().trim())
                .email(request.ownerEmail().trim())
                .passwordHash(passwords.encode(request.initialPassword()))
                .role(Role.OWNER)
                .hotelId(hotel.getId())
                .active(true)
                .mustChangePassword(true)
                .build());
        return summary(hotel);
    }

    private HotelSummary summary(final HotelRegistry hotel) {
        return new HotelSummary(hotel.getId(), hotel.getName(), hotel.getSlug());
    }
}
