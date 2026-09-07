package com.hotelpms.frontdesk.stays.service.impl;

import com.hotelpms.frontdesk.stays.domain.Stay;
import com.hotelpms.frontdesk.stays.repository.StayRepository;
import com.hotelpms.frontdesk.stays.service.AlloggiatiWebSenderService;
import com.hotelpms.frontdesk.stays.service.HotelSettingsService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class StayAlloggiatiCoordinatorTest {

    private final AlloggiatiWebSenderService sender = mock(AlloggiatiWebSenderService.class);
    private final HotelSettingsService settingsService = mock(HotelSettingsService.class);
    private final StayRepository stayRepository = mock(StayRepository.class);

    @Test
    void doesNotSendWhenRuntimeFlagIsDisabledEvenIfHotelWouldAutoSend() {
        final StayAlloggiatiCoordinator coordinator =
                new StayAlloggiatiCoordinator(sender, settingsService, stayRepository, false);
        final Stay stay = new Stay();
        stay.setId(UUID.randomUUID());
        stay.setHotelId(UUID.randomUUID());

        coordinator.sendAlloggiatiIfEnabled(stay);

        verifyNoInteractions(sender, settingsService, stayRepository);
    }

    @Test
    void canBeExplicitlyEnabledForAnIsolatedItalianRuntime() {
        final StayAlloggiatiCoordinator coordinator =
                new StayAlloggiatiCoordinator(sender, settingsService, stayRepository, true);

        assertThat(coordinator).isNotNull();
        verifyNoInteractions(sender);
    }
}
