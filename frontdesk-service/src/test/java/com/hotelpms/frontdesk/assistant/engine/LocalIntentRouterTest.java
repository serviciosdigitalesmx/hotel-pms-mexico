package com.hotelpms.frontdesk.assistant.engine;

import com.hotelpms.frontdesk.assistant.AssistantService;
import com.hotelpms.frontdesk.assistant.RetryableAiProviderException;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatRequest;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatResponse;
import com.hotelpms.frontdesk.assistant.dto.AssistantMessage;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.client.dto.GuestCreateRequest;
import com.hotelpms.frontdesk.client.dto.GuestResponse;
import com.hotelpms.frontdesk.client.dto.GuestSearchPageResponse;
import com.hotelpms.frontdesk.reservations.service.ReservationService;
import com.hotelpms.frontdesk.rooms.domain.RoomStatus;
import com.hotelpms.frontdesk.rooms.dto.RoomResponse;
import com.hotelpms.frontdesk.rooms.dto.RoomTypeResponse;
import com.hotelpms.frontdesk.stays.dto.StayResponse;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import com.hotelpms.frontdesk.stays.service.StayService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalIntentRouterTest {

    private static final UUID HOTEL_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID GUEST_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ROOM_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Set<String> ROLES = Set.of("RECEPTIONIST");
    private static final String USER_ID = "recepcion";
    private static final String ROBERTO_QUERY = "roberto";
    private static final String ROBERTO = "Roberto";
    private static final String GOMEZ = "Gómez";
    private static final String ROOM_101 = "101";
    private static final String YES = "sí";
    private static final String PEREZ = "Pérez";
    private static final String SLOT_GUEST_ID = "guestId";
    private static final String SLOT_CHECK_IN = "checkInDate";
    private static final String SLOT_CHECK_OUT = "checkOutDate";
    private static final String COMPLETE_CHECK_IN =
            "Check-in para Roberto, 2 adultos, sencilla, entra hoy y sale mañana";
    private static final int HTTP_UNAVAILABLE = 503;

    @Mock private ConversationSessionStore sessionStore;
    @Mock private GuestClient guestClient;
    @Mock private ReservationService reservationService;
    @Mock private StayService stayService;
    @Mock private AssistantService assistantService;
    @Mock private HotelSettingsRepository hotelSettingsRepository;

    private AtomicReference<ConversationSession> session;
    private LocalIntentRouter router;

    @BeforeEach
    void setUp() {
        session = new AtomicReference<>(new ConversationSession());
        lenient().when(sessionStore.withLock(any(), anyString(), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            final Supplier<AssistantChatResponse> operation = invocation.getArgument(2);
            return operation.get();
        });
        lenient().when(sessionStore.load(HOTEL_ID, USER_ID)).thenAnswer(ignored -> session.get());
        lenient().when(hotelSettingsRepository.findById(HOTEL_ID)).thenReturn(Optional.empty());
        lenient().doThrow(new RetryableAiProviderException("AI provider unavailable"))
                .when(assistantService).chat(any(), any(), any());
        lenient().doAnswer(ignored -> {
            session.set(new ConversationSession());
            return null;
        }).when(sessionStore).clear(HOTEL_ID, USER_ID);
        router = new LocalIntentRouter(
                sessionStore,
                new DeterministicParser(),
                guestClient,
                reservationService,
                stayService,
                assistantService,
                hotelSettingsRepository);
    }

    @Test
    void preservesKnownSlotsAndAsksOnlyForDates() {
        final AssistantChatResponse result = call("Check-in para Roberto, 2 adultos, sencilla");

        assertThat(result.answer()).isEqualTo("¿Cuál es la fecha de entrada?");
        assertThat(session.get().getSlots()).containsEntry(DeterministicParser.SLOT_GUEST_QUERY, ROBERTO_QUERY)
                .containsEntry(DeterministicParser.SLOT_OCCUPANT_COUNT, "2")
                .containsEntry(DeterministicParser.SLOT_ROOM_TYPE, "sencilla");
        verify(assistantService).chat(any(), any(), any());
    }

    @Test
    void doesNotSelectFirstGuestWhenThereAreMultipleMatches() {
        when(guestClient.searchGuestsStrict(ROBERTO_QUERY, 10)).thenReturn(new GuestSearchPageResponse(List.of(
                guest(GUEST_ID, ROBERTO, GOMEZ),
                guest(UUID.fromString("20000000-0000-0000-0000-000000000002"), ROBERTO, "Díaz"))));

        final AssistantChatResponse result = call(
                COMPLETE_CHECK_IN);

        assertThat(result.answer()).contains("1. Roberto Gómez", "2. Roberto Díaz");
        assertThat(session.get().has(SLOT_GUEST_ID)).isFalse();
        assertThat(session.get().getStep()).isEqualTo(ConversationStep.WAITING_GUEST_SELECTION);
    }

    @Test
    void asksForRoomSelectionWhenSeveralRealRoomsMatch() {
        when(guestClient.searchGuestsStrict(ROBERTO_QUERY, 10))
                .thenReturn(new GuestSearchPageResponse(List.of(guest(GUEST_ID, ROBERTO, GOMEZ))));
        when(reservationService.getAvailableRooms(any(), any())).thenReturn(List.of(
                room(ROOM_ID, ROOM_101),
                room(UUID.fromString("30000000-0000-0000-0000-000000000002"), "102"),
                room(UUID.fromString("30000000-0000-0000-0000-000000000003"), "103")));

        final AssistantChatResponse result = call(
                COMPLETE_CHECK_IN);

        assertThat(result.answer()).contains("1. Habitación 101", "2. Habitación 102", "3. Habitación 103");
        assertThat(session.get().has("roomId")).isFalse();
        assertThat(session.get().getStep()).isEqualTo(ConversationStep.WAITING_ROOM_SELECTION);
    }

    @Test
    void preservesContextWhenRequestedRoomTypeIsUnavailable() {
        when(guestClient.searchGuestsStrict(ROBERTO_QUERY, 10))
                .thenReturn(new GuestSearchPageResponse(List.of(guest(GUEST_ID, ROBERTO, GOMEZ))));
        when(reservationService.getAvailableRooms(any(), any())).thenReturn(List.of());

        final AssistantChatResponse result = call(
                COMPLETE_CHECK_IN);

        assertThat(result.answer()).contains("No hay habitaciones de ese tipo");
        assertThat(session.get().has(SLOT_GUEST_ID)).isTrue();
        assertThat(session.get().has(SLOT_CHECK_IN)).isTrue();
        assertThat(session.get().has(SLOT_CHECK_OUT)).isTrue();
        assertThat(session.get().getStep()).isEqualTo(ConversationStep.WAITING_ROOM_ALTERNATIVE);
    }

    @Test
    void unknownMessageUsesGroqFallbackUnchanged() {
        final AssistantChatResponse fallback = new AssistantChatResponse("respuesta groq", List.of());
        lenient().doReturn(fallback).when(assistantService).chat(any(), any(), any());

        assertThat(call("resume los ingresos del mes")).isSameAs(fallback);
        verify(guestClient, never()).searchGuestsStrict(anyString(), any(Integer.class));
    }

    @Test
    void createsMissingGuestOnlyAfterConfirmationAndResumesCheckIn() {
        when(guestClient.searchGuestsStrict(ROBERTO_QUERY, 10))
                .thenReturn(new GuestSearchPageResponse(List.of()));
        when(guestClient.createGuest(any(GuestCreateRequest.class)))
                .thenReturn(guest(GUEST_ID, ROBERTO, PEREZ));
        when(reservationService.getAvailableRooms(any(), any())).thenReturn(List.of(room(ROOM_ID, ROOM_101)));

        assertThat(call(COMPLETE_CHECK_IN).answer())
                .contains("¿Deseas crearlo?");
        assertThat(call(YES).answer()).contains("apellidos");
        assertThat(call(PEREZ).answer()).contains("correo electrónico");
        assertThat(call("roberto@hotel.test").answer()).contains("PROPUESTA DE NUEVO HUÉSPED");
        assertThat(call(YES).answer()).contains("PROPUESTA DE CHECK-IN");

        verify(guestClient).createGuest(new GuestCreateRequest(ROBERTO, PEREZ, "roberto@hotel.test"));
        assertThat(session.get().get(SLOT_GUEST_ID)).isEqualTo(GUEST_ID.toString());
    }

    @Test
    void isolatedDateAsksForItsMeaning() {
        final AssistantChatResponse result = call("qué habitaciones hay disponibles el 2026-08-20");

        assertThat(result.answer()).contains("¿corresponde a la entrada o a la salida?");
        verify(reservationService, never()).getAvailableRooms(any(), any());
    }

    @Test
    void guestServiceFailureIsNotReportedAsNoMatches() {
        final FeignException unavailable = org.mockito.Mockito.mock(FeignException.class);
        when(unavailable.status()).thenReturn(HTTP_UNAVAILABLE);
        when(guestClient.searchGuestsStrict(ROBERTO_QUERY, 10)).thenThrow(unavailable);

        final AssistantChatResponse result = call(
                COMPLETE_CHECK_IN);

        assertThat(result.answer()).contains("no está disponible temporalmente")
                .doesNotContain("No encontré ningún huésped");
        assertThat(session.get().get(DeterministicParser.SLOT_GUEST_QUERY)).isEqualTo(ROBERTO_QUERY);
    }

    @Test
    void cancelClearsAnActiveFlow() {
        call("Check-in para Roberto, 2 adultos, sencilla");

        assertThat(call("cancelar").answer()).isEqualTo("Operación cancelada.");
        assertThat(session.get().getIntent()).isEqualTo(LocalIntent.IDLE);
        verify(stayService, never()).checkIn(any());
    }

    @Test
    void repeatedConfirmationDoesNotDuplicateCheckIn() {
        when(guestClient.searchGuestsStrict(ROBERTO_QUERY, 10))
                .thenReturn(new GuestSearchPageResponse(List.of(guest(GUEST_ID, ROBERTO, GOMEZ))));
        when(reservationService.getAvailableRooms(any(), any())).thenReturn(List.of(room(ROOM_ID, ROOM_101)));
        final StayResponse stay = org.mockito.Mockito.mock(StayResponse.class);
        when(stay.roomNumber()).thenReturn(ROOM_101);
        when(stayService.checkIn(any())).thenReturn(stay);

        assertThat(call(COMPLETE_CHECK_IN).answer())
                .contains("PROPUESTA DE CHECK-IN");
        assertThat(call(YES).answer()).contains("Check-in completado");
        assertThat(call(YES).answer()).contains("No hay una operación local pendiente");

        verify(stayService).checkIn(any());
        verify(assistantService, times(3)).chat(any(), any(), any());
    }

    private AssistantChatResponse call(final String text) {
        final AssistantMessage message = new AssistantMessage("user", text, null, null, List.of());
        return router.processRequest(HOTEL_ID, USER_ID, ROLES, new AssistantChatRequest(List.of(message)));
    }

    private static GuestResponse guest(final UUID id, final String firstName, final String lastName) {
        return new GuestResponse(id, firstName, lastName, firstName.toLowerCase() + "@hotel.test");
    }

    private static RoomResponse room(final UUID id, final String number) {
        final RoomTypeResponse type = new RoomTypeResponse(
                UUID.fromString("50000000-0000-0000-0000-000000000001"),
                "Sencilla", "", 2, new BigDecimal("900.00"), true, null, null);
        return new RoomResponse(id, HOTEL_ID, number, type, RoomStatus.CLEAN, true, null, null,
                new BigDecimal("900.00"));
    }
}
