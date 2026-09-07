package com.hotelpms.frontdesk.stays.service.impl;

import com.hotelpms.frontdesk.exception.ExternalServiceException;
import com.hotelpms.frontdesk.stays.domain.Stay;
import com.hotelpms.frontdesk.stays.dto.HotelSettingsResponse;
import com.hotelpms.frontdesk.stays.repository.StayRepository;
import com.hotelpms.frontdesk.stays.service.AlloggiatiWebSenderService;
import com.hotelpms.frontdesk.stays.service.HotelSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Submits the check-in to Alloggiati Web (Polizia di Stato) when auto-send is
 * enabled for the hotel. Failure never blocks check-in — it's recorded on the
 * {@link Stay} for the Dashboard banner / per-stay badge and the manual retry
 * path ({@code POST /reports/alloggiati/submit}).
 */
@Component
@Slf4j
class StayAlloggiatiCoordinator {

    private final AlloggiatiWebSenderService alloggiatiWebSenderService;
    private final HotelSettingsService hotelSettingsService;
    private final StayRepository stayRepository;
    private final boolean alloggiatiEnabled;

    StayAlloggiatiCoordinator(
            final AlloggiatiWebSenderService alloggiatiWebSenderService,
            final HotelSettingsService hotelSettingsService,
            final StayRepository stayRepository,
            @Value("${hotel-pms.alloggiati.enabled:false}") final boolean alloggiatiEnabled) {
        this.alloggiatiWebSenderService = alloggiatiWebSenderService;
        this.hotelSettingsService = hotelSettingsService;
        this.stayRepository = stayRepository;
        this.alloggiatiEnabled = alloggiatiEnabled;
    }

    /**
     * Sends the day's Alloggiati Web report for the stay's check-in date if
     * auto-send is enabled in hotel settings; records success/failure on the stay.
     *
     * @param stay the just-checked-in stay
     */
    void sendAlloggiatiIfEnabled(final Stay stay) {
        if (!alloggiatiEnabled) {
            log.debug("Alloggiati runtime disabled — skipping Polizia di Stato send for stay {}",
                    stay.getId());
            return;
        }
        if (stay.getHotelId() == null) {
            return;
        }
        final HotelSettingsResponse settings = hotelSettingsService.getOrCreate(stay.getHotelId());
        if (!settings.alloggiatiAutoSend()) {
            return;
        }
        final LocalDate checkInDate = stay.getActualCheckInTime().toLocalDate();
        try {
            alloggiatiWebSenderService.submitReport(checkInDate, stay.getHotelId());
            stay.setAlloggiatiSent(true);
            stay.setAlloggiatiSendFailed(false);
            stay.setAlloggiatiFailureReason(null);
            stayRepository.save(stay);
            log.info("[STAY] ALLOGGIATI_SENT | stayId={} | date={}", stay.getId(), checkInDate);
        } catch (final ExternalServiceException ex) {
            log.error("[STAY] ALLOGGIATI_SEND_FAILED | stayId={} | date={} | reason={}",
                    stay.getId(), checkInDate, ex.getMessage());
            stay.setAlloggiatiSendFailed(true);
            stay.setAlloggiatiFailureReason(StayFailureReason.truncate(ex.getMessage()));
            stayRepository.save(stay);
        }
    }
}
