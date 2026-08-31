package com.hotelpms.frontdesk.assistant.engine;

import com.hotelpms.frontdesk.assistant.AssistantService;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatRequest;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatResponse;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.client.dto.GuestCreateRequest;
import com.hotelpms.frontdesk.client.dto.GuestResponse;
import com.hotelpms.frontdesk.client.dto.GuestSearchPageResponse;
import com.hotelpms.frontdesk.exception.BadRequestException;
import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.exception.ExternalServiceException;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.reservations.service.ReservationService;
import com.hotelpms.frontdesk.rooms.dto.RoomResponse;
import com.hotelpms.frontdesk.stays.domain.StayStatus;
import com.hotelpms.frontdesk.stays.dto.StayRequest;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import com.hotelpms.frontdesk.stays.service.StayService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Routes supported operational conversations to the real PMS without Groq. */
@Component
@Slf4j
public final class LocalIntentRouter {

    private static final int MAX_GUEST_RESULTS = 10;
    private static final String SLOT_GUEST_QUERY = DeterministicParser.SLOT_GUEST_QUERY;
    private static final String SLOT_OCCUPANT_COUNT = DeterministicParser.SLOT_OCCUPANT_COUNT;
    private static final String SLOT_ROOM_TYPE = DeterministicParser.SLOT_ROOM_TYPE;
    private static final String SLOT_CHECK_IN = DeterministicParser.SLOT_CHECK_IN;
    private static final String SLOT_CHECK_OUT = DeterministicParser.SLOT_CHECK_OUT;
    private static final String SLOT_GUEST_ID = "guestId";
    private static final String SLOT_ROOM_ID = "roomId";
    private static final String SLOT_GUEST_FULL_NAME = "guestFullName";
    private static final String SLOT_GUEST_EMAIL = "guestEmail";
    private static final String SLOT_ROOM_NUMBER = "roomNumber";
    private static final String SLOT_ROOM_TYPE_NAME = "roomTypeName";
    private static final String SLOT_ROOM_MAX_OCCUPANCY = "roomMaxOccupancy";
    private static final String SLOT_PENDING_SINGLE_DATE = "pendingSingleDate";
    private static final String SLOT_FIRST_NAME = "newGuestFirstName";
    private static final String SLOT_LAST_NAME = "newGuestLastName";
    private static final String SLOT_EMAIL = "newGuestEmail";
    private static final String SLOT_RETURN_INTENT = "returnIntent";
    private static final String SLOT_SELECTION_PURPOSE = "selectionPurpose";

    private static final String SLOT_BATCH_COUNT = DeterministicParser.SLOT_BATCH_COUNT;
    private static final String SLOT_BATCH_RAW_LIST = "batchGuestList";

    private static final String SLOT_BATCH_ORIGIN_TYPE = "batchOriginType";
    private static final String SLOT_BATCH_ORGANIZATION_NAME = "batchOrganizationName";
    private static final String SLOT_BATCH_BILLING_MODE = "batchBillingMode";

    private static final int MAX_BATCH_CHECK_INS = 10;

    private static final String LINE_BREAK = "\n";

    private final ConversationSessionStore sessionStore;
    private final DeterministicParser parser;
    private final GuestClient guestClient;
    private final ReservationService reservationService;
    private final StayService stayService;
    private final AssistantService assistantService;
    private final HotelSettingsRepository hotelSettingsRepository;
    private final ResilientIntentFallbackHandler fallbackHandler;

    /** Constructor retained for existing direct unit-test wiring. */
    public LocalIntentRouter(
            final ConversationSessionStore sessionStore,
            final DeterministicParser parser,
            final GuestClient guestClient,
            final ReservationService reservationService,
            final StayService stayService,
            final AssistantService assistantService,
            final HotelSettingsRepository hotelSettingsRepository) {
        this(sessionStore, parser, guestClient, reservationService, stayService, assistantService,
                hotelSettingsRepository, new ResilientIntentFallbackHandler());
    }

    @Autowired
    public LocalIntentRouter(
            final ConversationSessionStore sessionStore,
            final DeterministicParser parser,
            final GuestClient guestClient,
            final ReservationService reservationService,
            final StayService stayService,
            final AssistantService assistantService,
            final HotelSettingsRepository hotelSettingsRepository,
            final ResilientIntentFallbackHandler fallbackHandler) {
        this.sessionStore = sessionStore;
        this.parser = parser;
        this.guestClient = guestClient;
        this.reservationService = reservationService;
        this.stayService = stayService;
        this.assistantService = assistantService;
        this.hotelSettingsRepository = hotelSettingsRepository;
        this.fallbackHandler = fallbackHandler;
    }

    /**
     * Processes one authenticated operator turn.
     *
     * @param hotelId authenticated tenant
     * @param userId authenticated operator identity
     * @param roles authenticated roles
     * @param request complete provider-neutral conversation
     * @return local PMS response or unchanged Groq fallback response
     */
    public AssistantChatResponse processRequest(
            final UUID hotelId,
            final String userId,
            final Set<String> roles,
            final AssistantChatRequest request) {

        /*
         * AI-FIRST ROUTING
         *
         * Qwen/Ollama es el interprete principal.
         * El router determinista queda solamente como fallback tecnico
         * si el proveedor local no puede responder.
         */
        if (aiFirstEnabled()) {
            return fallbackHandler.resolve(
                    () -> assistantService.chat(hotelId, roles, request),
                    () -> processDeterministically(hotelId, userId, roles, request));
        }

        return processDeterministically(hotelId, userId, roles, request);
    }

    private AssistantChatResponse processDeterministically(
            final UUID hotelId,
            final String userId,
            final Set<String> roles,
            final AssistantChatRequest request) {
        return sessionStore.withLock(hotelId, userId,
                () -> processLocked(hotelId, userId, roles, request));
    }

    private AssistantChatResponse processLocked(
            final UUID hotelId,
            final String userId,
            final Set<String> roles,
            final AssistantChatRequest request) {
        final String input = latestUserMessage(request);
        final LocalDate today = LocalDate.now(resolveHotelZone(hotelId));
        final DeterministicParser.ParsedCommand command = parser.parse(input, today);
        final ConversationSession session = sessionStore.load(hotelId, userId);

        if (command.intent() == LocalIntent.CANCEL) {
            sessionStore.clear(hotelId, userId);
            return response("Operación cancelada.");
        }

        if (session.getIntent() == LocalIntent.IDLE) {
            if (command.intent() == LocalIntent.UNKNOWN) {
                return assistantService.chat(hotelId, roles, request);
            }
            if (command.intent() == LocalIntent.CONFIRM || command.intent() == LocalIntent.DECLINE) {
                return response("No hay una operación local pendiente de confirmación.");
            }
            session.setIntent(command.intent());
        }

        absorbEntities(session, command.entities());
        try {
            final AssistantChatResponse result = route(hotelId, session, command, input, today);
            if (session.getIntent() == LocalIntent.IDLE) {
                sessionStore.clear(hotelId, userId);
            } else {
                sessionStore.save(hotelId, userId, session);
            }
            return result;
        } catch (final FeignException ex) {
            log.warn("Local assistant dependency unavailable | hotelId={} | status={}", hotelId, ex.status());
            sessionStore.save(hotelId, userId, session);
            return response("El servicio de huéspedes no está disponible temporalmente. "
                    + "Conservé el progreso para que puedas reintentar.");
        } catch (final BadRequestException | ConflictException | NotFoundException ex) {
            log.warn("Local assistant business validation failed | hotelId={} | type={}",
                    hotelId, ex.getClass().getSimpleName());
            sessionStore.save(hotelId, userId, session);
            return response("El PMS rechazó la operación por una validación de negocio. "
                    + "No se realizó ningún cambio.");
        } catch (final ExternalServiceException ex) {
            log.warn("Local assistant dependency failed | hotelId={} | type={}",
                    hotelId, ex.getClass().getSimpleName());
            sessionStore.save(hotelId, userId, session);
            return response("Un servicio interno no está disponible temporalmente. "
                    + "Conservé el progreso para que puedas reintentar.");
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            log.error("Unexpected local assistant failure | hotelId={}", hotelId, ex);
            sessionStore.save(hotelId, userId, session);
            return response("No pude completar la operación. No se realizó ningún cambio y conservé el progreso.");
        }
    }

    private AssistantChatResponse route(
            final UUID hotelId,
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final String input,
            final LocalDate today) {
        return switch (session.getIntent()) {
            case FIND_GUEST -> handleFindGuest(session, command, input);
            case CREATE_GUEST -> handleCreateGuest(hotelId, session, command, input, today);
            case ROOM_AVAILABILITY -> handleAvailability(session, input);
            case PREPARE_CHECK_IN, CHECK_IN -> handlePrepareCheckIn(hotelId, session, command, input, today);
            case BATCH_CHECK_IN -> handleBatchCheckIn(hotelId, session, command, input, today);
            default -> {
                session.setIntent(LocalIntent.IDLE);
                yield response("No hay un flujo local activo.");
            }
        };
    }

    private AssistantChatResponse handleFindGuest(
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final String input) {
        if (session.getStep() == ConversationStep.WAITING_GUEST_SELECTION) {
            return selectGuest(session, input, false, null, LocalDate.now());
        }
        if (session.getStep() == ConversationStep.WAITING_CREATE_GUEST_DECISION) {
            return handleCreateDecision(session, command, null, LocalDate.now());
        }
        if (!session.has(SLOT_GUEST_QUERY)) {
            if (session.getStep() == ConversationStep.WAITING_GUEST_NAME && !input.isBlank()) {
                session.put(SLOT_GUEST_QUERY, input.trim());
            } else {
                session.setStep(ConversationStep.WAITING_GUEST_NAME);
                return response("¿Qué nombre o correo del huésped debo buscar?");
            }
        }
        return resolveGuest(session, false, null, LocalDate.now());
    }

    private AssistantChatResponse handleAvailability(final ConversationSession session, final String input) {
        resolvePendingDateMeaning(session, input);
        final AssistantChatResponse dateQuestion = requireDates(session);
        if (dateQuestion != null) {
            return dateQuestion;
        }
        final LocalDate checkIn = LocalDate.parse(session.get(SLOT_CHECK_IN));
        final LocalDate checkOut = LocalDate.parse(session.get(SLOT_CHECK_OUT));
        if (!checkOut.isAfter(checkIn)) {
            session.remove(SLOT_CHECK_OUT);
            session.setStep(ConversationStep.WAITING_CHECKOUT_DATE);
            return response("La salida debe ser posterior a la entrada. ¿Cuál es la fecha de salida?");
        }
        final List<RoomResponse> rooms = filterByRequestedType(
                reservationService.getAvailableRooms(checkIn, checkOut), session.get(SLOT_ROOM_TYPE));
        session.setIntent(LocalIntent.IDLE);
        if (rooms.isEmpty()) {
            return response("No hay habitaciones disponibles para esas fechas"
                    + requestedTypeSuffix(session) + ".");
        }
        final StringBuilder answer = new StringBuilder("Habitaciones disponibles (" + rooms.size() + "):\n");
        rooms.forEach(room -> answer.append(formatRoom(room)).append('\n'));
        return response(answer.toString().trim());
    }

    private AssistantChatResponse handlePrepareCheckIn(
            final UUID hotelId,
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final String input,
            final LocalDate today) {
        resolvePendingDateMeaning(session, input);
        final AssistantChatResponse dateQuestion = requireDates(session);
        if (dateQuestion != null) {
            return dateQuestion;
        }
        if (!LocalDate.parse(session.get(SLOT_CHECK_OUT)).isAfter(LocalDate.parse(session.get(SLOT_CHECK_IN)))) {
            session.remove(SLOT_CHECK_OUT);
            session.setStep(ConversationStep.WAITING_CHECKOUT_DATE);
            return response("La salida debe ser posterior a la entrada. ¿Cuál es la fecha de salida?");
        }

        if (!session.has(SLOT_GUEST_ID)) {
            if (session.getStep() == ConversationStep.WAITING_GUEST_SELECTION) {
                return selectGuest(session, input, true, hotelId, today);
            }
            if (session.getStep() == ConversationStep.WAITING_CREATE_GUEST_DECISION) {
                return handleCreateDecision(session, command, hotelId, today);
            }
            if (!session.has(SLOT_GUEST_QUERY)) {
                if (session.getStep() == ConversationStep.WAITING_GUEST_NAME && !input.isBlank()) {
                    session.put(SLOT_GUEST_QUERY, input.trim());
                } else {
                    session.setStep(ConversationStep.WAITING_GUEST_NAME);
                    return response("¿A nombre de quién busco al huésped?");
                }
            }
            return resolveGuest(session, true, hotelId, today);
        }

        if (!session.has(SLOT_ROOM_TYPE)) {
            if (session.getStep() == ConversationStep.WAITING_ROOM_TYPE && !input.isBlank()) {
                session.put(SLOT_ROOM_TYPE, DeterministicParser.normalize(input));
            } else {
                session.setStep(ConversationStep.WAITING_ROOM_TYPE);
                return response("¿Qué tipo de habitación necesitas?");
            }
        }
        if (!session.has(SLOT_ROOM_ID)) {
            if (session.getStep() == ConversationStep.WAITING_ROOM_SELECTION) {
                final AssistantChatResponse selection = selectRoom(session, input);
                if (selection != null) {
                    return selection;
                }
            }
            if (!session.has(SLOT_ROOM_ID)) {
                final AssistantChatResponse resolution = resolveRoom(session);
                if (resolution != null) {
                    return resolution;
                }
            }
        }

        if (!session.has(SLOT_OCCUPANT_COUNT)) {
            if (session.getStep() == ConversationStep.WAITING_OCCUPANT_COUNT) {
                final String normalized = DeterministicParser.normalize(input);
                if (!normalized.matches("\\d+")) {
                    return response("Indica el número total de personas con un número entero.");
                }
                session.put(SLOT_OCCUPANT_COUNT, normalized);
            } else {
                session.setStep(ConversationStep.WAITING_OCCUPANT_COUNT);
                return response("¿Cuántas personas se hospedarán?");
            }
        }

        final int occupants = Integer.parseInt(session.get(SLOT_OCCUPANT_COUNT));
        final int capacity = Integer.parseInt(session.get(SLOT_ROOM_MAX_OCCUPANCY));
        if (occupants < 1 || occupants > capacity) {
            session.remove(SLOT_OCCUPANT_COUNT);
            session.setStep(ConversationStep.WAITING_OCCUPANT_COUNT);
            return response("La habitación admite como máximo " + capacity
                    + " personas. ¿Cuántas personas se hospedarán?");
        }

        if (session.getStep() == ConversationStep.WAITING_CHECKIN_CONFIRMATION) {
            return confirmCheckIn(hotelId, session, command, today);
        }
        session.setStep(ConversationStep.WAITING_CHECKIN_CONFIRMATION);
        return response("[PROPUESTA DE CHECK-IN]" + LINE_BREAK
                + "Huésped: " + session.get(SLOT_GUEST_FULL_NAME) + LINE_BREAK
                + "Habitación: " + session.get(SLOT_ROOM_NUMBER) + LINE_BREAK
                + "Tipo: " + session.get(SLOT_ROOM_TYPE_NAME) + LINE_BREAK
                + "Entrada: " + session.get(SLOT_CHECK_IN) + LINE_BREAK
                + "Salida: " + session.get(SLOT_CHECK_OUT) + LINE_BREAK
                + "Huéspedes: " + occupants + LINE_BREAK + LINE_BREAK + "¿Confirmas?");
    }

    private AssistantChatResponse confirmCheckIn(
            final UUID hotelId,
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final LocalDate today) {
        if (command.intent() == LocalIntent.DECLINE) {
            session.setIntent(LocalIntent.IDLE);
            return response("Check-in cancelado. No se realizó ningún cambio.");
        }
        if (command.intent() != LocalIntent.CONFIRM) {
            return response("Responde “sí” para confirmar o “no” para cancelar.");
        }
        final LocalDate checkIn = LocalDate.parse(session.get(SLOT_CHECK_IN));
        if (!today.equals(checkIn)) {
            session.remove(SLOT_CHECK_IN);
            session.setStep(ConversationStep.WAITING_CHECKIN_DATE);
            return response("El check-in registra una entrada en este momento. "
                    + "La fecha de entrada debe ser hoy. Indica la fecha correcta o cancela.");
        }
        final StayRequest request = new StayRequest(
                hotelId,
                null,
                UUID.fromString(session.get(SLOT_GUEST_ID)),
                UUID.fromString(session.get(SLOT_ROOM_ID)),
                StayStatus.CHECKED_IN,
                LocalDate.parse(session.get(SLOT_CHECK_OUT)),
                null,
                null,
                Integer.valueOf(session.get(SLOT_OCCUPANT_COUNT)),
                List.of());
        final var stay = stayService.checkIn(request);
        session.setIntent(LocalIntent.IDLE);
        return response("Check-in completado. Habitación " + stay.roomNumber() + ".");
    }

    /**
     * Group/batch check-in.
     *
     * The operator performs one conversational operation and confirms once.
     * Internally every StayService.checkIn call remains an independent real PMS
     * check-in, so one failure cannot be reported as a false global success.
     */
    private AssistantChatResponse handleBatchCheckIn(
            final UUID hotelId,
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final String input,
            final LocalDate today) {

        if (session.getStep() == ConversationStep.WAITING_BATCH_CONFIRMATION) {
            return confirmBatchCheckIn(hotelId, session, command, today);
        }

        resolvePendingDateMeaning(session, input);

        final AssistantChatResponse dateQuestion = requireDates(session);

        if (dateQuestion != null) {
            return dateQuestion;
        }

        final LocalDate checkIn = LocalDate.parse(session.get(SLOT_CHECK_IN));
        final LocalDate checkOut = LocalDate.parse(session.get(SLOT_CHECK_OUT));

        if (!checkOut.isAfter(checkIn)) {
            session.remove(SLOT_CHECK_OUT);
            session.setStep(ConversationStep.WAITING_CHECKOUT_DATE);

            return response(
                    "La salida debe ser posterior a la entrada. "
                            + "¿Cuál es la fecha de salida?");
        }

        if (!today.equals(checkIn)) {
            session.remove(SLOT_CHECK_IN);
            session.setStep(ConversationStep.WAITING_CHECKIN_DATE);

            return response(
                    "El check-in de grupo registra entradas en este momento. "
                            + "La fecha de entrada debe ser hoy.");
        }

        if (!session.has(SLOT_ROOM_TYPE)) {
            if (session.getStep() == ConversationStep.WAITING_ROOM_TYPE
                    && !input.isBlank()) {

                session.put(
                        SLOT_ROOM_TYPE,
                        DeterministicParser.normalize(input));

            } else {
                session.setStep(ConversationStep.WAITING_ROOM_TYPE);

                return response(
                        "¿Qué tipo de habitaciones necesita el grupo?");
            }
        }

        if (!session.has(SLOT_BATCH_COUNT)) {

            if (session.getStep() == ConversationStep.WAITING_BATCH_COUNT) {
                final String normalized =
                        DeterministicParser.normalize(input);

                if (!normalized.matches("\\d+")) {
                    return response(
                            "Indica cuántas habitaciones se van a registrar, "
                                    + "con un número del 1 al 10.");
                }

                session.put(SLOT_BATCH_COUNT, normalized);

            } else {
                session.setStep(ConversationStep.WAITING_BATCH_COUNT);

                return response(
                        "¿Cuántas habitaciones quieres registrar en este grupo? "
                                + "Máximo 10.");
            }
        }

        final int count =
                Integer.parseInt(session.get(SLOT_BATCH_COUNT));

        if (count < 1 || count > MAX_BATCH_CHECK_INS) {
            session.remove(SLOT_BATCH_COUNT);
            session.setStep(ConversationStep.WAITING_BATCH_COUNT);

            return response(
                    "El lote debe contener entre 1 y 10 habitaciones. "
                            + "¿Cuántas habitaciones son?");
        }

        /*
         * --------------------------------------------------------
         * ORIGEN COMERCIAL DEL GRUPO
         * --------------------------------------------------------
         */

        if (!session.has(SLOT_BATCH_ORIGIN_TYPE)) {

            if (session.getStep()
                    == ConversationStep.WAITING_BATCH_ORIGIN_TYPE) {

                final String origin =
                        parseBatchOriginType(input);

                if (origin == null) {
                    return batchOriginQuestion();
                }

                session.put(
                        SLOT_BATCH_ORIGIN_TYPE,
                        origin);

                session.setStep(
                        ConversationStep.NONE);

            } else {

                session.setStep(
                        ConversationStep.WAITING_BATCH_ORIGIN_TYPE);

                return batchOriginQuestion();
            }
        }

        final String originType =
                session.get(SLOT_BATCH_ORIGIN_TYPE);

        /*
         * Particular no necesita nombre de organización.
         * Empresa, agencia y convenio sí.
         */
        if (!"INDIVIDUAL".equals(originType)
                && !session.has(
                        SLOT_BATCH_ORGANIZATION_NAME)) {

            if (session.getStep()
                    == ConversationStep.WAITING_BATCH_ORGANIZATION_NAME
                    && input != null
                    && !input.isBlank()) {

                session.put(
                        SLOT_BATCH_ORGANIZATION_NAME,
                        input.trim());

                session.setStep(
                        ConversationStep.NONE);

            } else {

                session.setStep(
                        ConversationStep.WAITING_BATCH_ORGANIZATION_NAME);

                return response(
                        batchOrganizationQuestion(originType));
            }
        }

        /*
         * Particular = facturación individual por defecto.
         *
         * Empresa/agencia/convenio:
         * preguntar si será individual, consolidada o pendiente.
         */
        if (!session.has(SLOT_BATCH_BILLING_MODE)) {

            if ("INDIVIDUAL".equals(originType)) {

                session.put(
                        SLOT_BATCH_BILLING_MODE,
                        "INDIVIDUAL");

            } else if (session.getStep()
                    == ConversationStep.WAITING_BATCH_BILLING_MODE) {

                final String billingMode =
                        parseBatchBillingMode(input);

                if (billingMode == null) {
                    return batchBillingQuestion(session);
                }

                session.put(
                        SLOT_BATCH_BILLING_MODE,
                        billingMode);

                session.setStep(
                        ConversationStep.NONE);

            } else {

                session.setStep(
                        ConversationStep.WAITING_BATCH_BILLING_MODE);

                return batchBillingQuestion(session);
            }
        }

        if (session.getStep()
                == ConversationStep.WAITING_BATCH_GUEST_LIST) {

            if (input.isBlank()) {
                return batchListInstructions(count);
            }

            session.put(SLOT_BATCH_RAW_LIST, input);

            return prepareBatchPlan(session, input);
        }

        if (!session.has(SLOT_BATCH_RAW_LIST)) {
            session.setStep(
                    ConversationStep.WAITING_BATCH_GUEST_LIST);

            return batchListInstructions(count);
        }

        return prepareBatchPlan(
                session,
                session.get(SLOT_BATCH_RAW_LIST));
    }

    private static AssistantChatResponse batchOriginQuestion() {

        return response(
                "¿Cuál es el origen de este grupo?"
                        + LINE_BREAK
                        + LINE_BREAK
                        + "1. Particular / individual"
                        + LINE_BREAK
                        + "2. Empresa"
                        + LINE_BREAK
                        + "3. Agencia"
                        + LINE_BREAK
                        + "4. Convenio / evento");
    }

    private static String parseBatchOriginType(
            final String input) {

        final String normalized =
                DeterministicParser.normalize(input);

        return switch (normalized) {

            case "1",
                 "particular",
                 "individual",
                 "particulares",
                 "individuales" ->
                    "INDIVIDUAL";

            case "2",
                 "empresa",
                 "corporativo",
                 "corporativa" ->
                    "COMPANY";

            case "3",
                 "agencia",
                 "agencia de viajes" ->
                    "AGENCY";

            case "4",
                 "convenio",
                 "evento",
                 "convenio evento",
                 "convenio / evento" ->
                    "CONVENTION";

            default -> null;
        };
    }

    private static String batchOrganizationQuestion(
            final String originType) {

        return switch (originType) {

            case "COMPANY" ->
                    "¿Cuál es el nombre de la empresa?";

            case "AGENCY" ->
                    "¿Cuál es el nombre de la agencia?";

            case "CONVENTION" ->
                    "¿Cuál es el nombre del convenio, evento "
                            + "u organización?";

            default ->
                    "¿Cuál es el nombre de la organización?";
        };
    }

    private static AssistantChatResponse batchBillingQuestion(
            final ConversationSession session) {

        final String organization =
                session.has(SLOT_BATCH_ORGANIZATION_NAME)
                        ? session.get(
                                SLOT_BATCH_ORGANIZATION_NAME)
                        : "la organización";

        return response(
                "¿Cómo se manejará la facturación?"
                        + LINE_BREAK
                        + LINE_BREAK
                        + "1. Individual por habitación / huésped"
                        + LINE_BREAK
                        + "2. Una sola factura consolidada a "
                        + organization
                        + LINE_BREAK
                        + "3. Pendiente por definir");
    }

    private static String parseBatchBillingMode(
            final String input) {

        final String normalized =
                DeterministicParser.normalize(input);

        if (normalized.equals("1")
                || normalized.contains("individual")
                || normalized.contains("separad")) {

            return "INDIVIDUAL";
        }

        if (normalized.equals("2")
                || normalized.contains("consolid")
                || normalized.contains("empresa")
                || normalized.contains("agencia")
                || normalized.contains("una sola")) {

            return "ORGANIZATION";
        }

        if (normalized.equals("3")
                || normalized.contains("pendiente")
                || normalized.contains("despues")
                || normalized.contains("por definir")) {

            return "PENDING";
        }

        return null;
    }

    private static String batchOriginLabel(
            final String originType) {

        return switch (originType) {

            case "INDIVIDUAL" ->
                    "Particular / individual";

            case "COMPANY" ->
                    "Empresa";

            case "AGENCY" ->
                    "Agencia";

            case "CONVENTION" ->
                    "Convenio / evento";

            default ->
                    originType;
        };
    }

    private static String batchBillingLabel(
            final String billingMode) {

        return switch (billingMode) {

            case "INDIVIDUAL" ->
                    "Individual";

            case "ORGANIZATION" ->
                    "Consolidada a organización";

            case "PENDING" ->
                    "Pendiente por definir";

            default ->
                    billingMode;
        };
    }

    private AssistantChatResponse batchListInstructions(
            final int count) {

        return response(
                "Pega la lista de las " + count
                        + " habitaciones, una por línea, así:"
                        + LINE_BREAK
                        + LINE_BREAK
                        + "Nombre completo | personas"
                        + LINE_BREAK
                        + "Juan Pérez | 3"
                        + LINE_BREAK
                        + "María López | 2"
                        + LINE_BREAK
                        + LINE_BREAK
                        + "Los huéspedes deben existir en el PMS. "
                        + "No se ejecutará ningún check-in hasta "
                        + "que confirmes el lote completo.");
    }

    private AssistantChatResponse prepareBatchPlan(
            final ConversationSession session,
            final String rawList) {

        final int expected =
                Integer.parseInt(session.get(SLOT_BATCH_COUNT));

        final List<BatchGuestLine> lines =
                parseBatchGuestLines(rawList);

        if (lines.size() != expected) {
            session.setStep(
                    ConversationStep.WAITING_BATCH_GUEST_LIST);

            return response(
                    "Esperaba " + expected
                            + " líneas y recibí " + lines.size() + "."
                            + LINE_BREAK
                            + "Vuelve a pegar la lista completa.");
        }

        final LocalDate checkIn =
                LocalDate.parse(session.get(SLOT_CHECK_IN));

        final LocalDate checkOut =
                LocalDate.parse(session.get(SLOT_CHECK_OUT));

        final List<RoomResponse> rooms =
                filterByRequestedType(
                        reservationService.getAvailableRooms(
                                checkIn,
                                checkOut),
                        session.get(SLOT_ROOM_TYPE))
                        .stream()
                        .sorted(java.util.Comparator.comparing(
                                RoomResponse::roomNumber))
                        .toList();

        if (rooms.size() < expected) {
            session.setStep(
                    ConversationStep.WAITING_BATCH_GUEST_LIST);

            return response(
                    "No hay suficientes habitaciones "
                            + session.get(SLOT_ROOM_TYPE)
                            + " disponibles."
                            + LINE_BREAK
                            + "Necesitas: " + expected
                            + LINE_BREAK
                            + "Disponibles: " + rooms.size()
                            + LINE_BREAK
                            + "No se realizó ningún cambio.");
        }

        final List<BatchResolvedGuest> resolved =
                new java.util.ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {

            final BatchGuestLine line = lines.get(index);

            final GuestSearchPageResponse result =
                    guestClient.searchGuestsStrict(
                            line.query(),
                            MAX_GUEST_RESULTS);

            if (result.content().isEmpty()) {
                session.setStep(
                        ConversationStep.WAITING_BATCH_GUEST_LIST);

                return response(
                        "No encontré al huésped “"
                                + line.query()
                                + "”."
                                + LINE_BREAK
                                + "Regístralo en Huéspedes y después "
                                + "vuelve a pegar la lista completa."
                                + LINE_BREAK
                                + "No se realizó ningún check-in.");
            }

            if (result.content().size() > 1) {
                session.setStep(
                        ConversationStep.WAITING_BATCH_GUEST_LIST);

                return response(
                        "Encontré varios huéspedes para “"
                                + line.query()
                                + "”."
                                + LINE_BREAK
                                + "Usa un nombre más específico en la lista."
                                + LINE_BREAK
                                + "No se realizó ningún check-in.");
            }

            final GuestResponse guest =
                    result.content().getFirst();

            final RoomResponse room =
                    rooms.get(index);

            final Integer maxOccupancy =
                    room.roomType().maxOccupancy();

            if (line.occupants() < 1
                    || maxOccupancy == null
                    || line.occupants() > maxOccupancy) {

                session.setStep(
                        ConversationStep.WAITING_BATCH_GUEST_LIST);

                return response(
                        "La habitación "
                                + room.roomNumber()
                                + " admite máximo "
                                + maxOccupancy
                                + " personas, pero “"
                                + fullName(guest)
                                + "” tiene "
                                + line.occupants()
                                + "."
                                + LINE_BREAK
                                + "Corrige la lista."
                                + LINE_BREAK
                                + "No se realizó ningún check-in.");
            }

            resolved.add(
                    new BatchResolvedGuest(
                            guest,
                            room,
                            line.occupants()));
        }

        session.clearOptions();

        final StringBuilder proposal =
                new StringBuilder(
                        "[PROPUESTA DE CHECK-IN DE GRUPO]")
                        .append(LINE_BREAK)
                        .append("Origen: ")
                        .append(batchOriginLabel(
                                session.get(SLOT_BATCH_ORIGIN_TYPE)))
                        .append(LINE_BREAK);

        if (session.has(SLOT_BATCH_ORGANIZATION_NAME)) {
            proposal.append("Organización: ")
                    .append(session.get(
                            SLOT_BATCH_ORGANIZATION_NAME))
                    .append(LINE_BREAK);
        }

        proposal.append("Facturación: ")
                .append(batchBillingLabel(
                        session.get(SLOT_BATCH_BILLING_MODE)))
                .append(LINE_BREAK)
                .append("Habitaciones: ")
                        .append(expected)
                        .append(LINE_BREAK)
                        .append("Tipo: ")
                        .append(session.get(SLOT_ROOM_TYPE))
                        .append(LINE_BREAK)
                        .append("Entrada: ")
                        .append(session.get(SLOT_CHECK_IN))
                        .append(LINE_BREAK)
                        .append("Salida: ")
                        .append(session.get(SLOT_CHECK_OUT))
                        .append(LINE_BREAK)
                        .append(LINE_BREAK);

        int number = 1;

        for (final BatchResolvedGuest item : resolved) {

            final String key =
                    "batch:" + number;

            final String encoded =
                    item.guest().id()
                            + "|"
                            + item.room().id()
                            + "|"
                            + item.occupants();

            final String label =
                    item.room().roomNumber()
                            + " | "
                            + fullName(item.guest())
                            + " | "
                            + item.occupants()
                            + " personas";

            session.getOptionIds().put(key, encoded);
            session.getOptionLabels().put(key, label);

            proposal.append(number)
                    .append(". ")
                    .append(label)
                    .append(LINE_BREAK);

            number++;
        }

        proposal.append(LINE_BREAK)
                .append("Todos los datos fueron validados.")
                .append(LINE_BREAK)
                .append("¿Confirmas los ")
                .append(expected)
                .append(" check-ins?");

        session.setStep(
                ConversationStep.WAITING_BATCH_CONFIRMATION);

        return response(proposal.toString());
    }

    private AssistantChatResponse confirmBatchCheckIn(
            final UUID hotelId,
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final LocalDate today) {

        if (command.intent() == LocalIntent.DECLINE) {
            session.setIntent(LocalIntent.IDLE);
            session.clearOptions();

            return response(
                    "Check-in de grupo cancelado. "
                            + "No se realizó ningún cambio.");
        }

        if (command.intent() != LocalIntent.CONFIRM) {
            return response(
                    "Responde “sí” para confirmar los check-ins "
                            + "o “no” para cancelar.");
        }

        final LocalDate checkIn =
                LocalDate.parse(session.get(SLOT_CHECK_IN));

        if (!today.equals(checkIn)) {
            session.remove(SLOT_CHECK_IN);
            session.setStep(
                    ConversationStep.WAITING_CHECKIN_DATE);

            return response(
                    "La entrada del grupo debe corresponder a hoy. "
                            + "No se ejecutó ningún check-in.");
        }

        final int count =
                Integer.parseInt(session.get(SLOT_BATCH_COUNT));

        /*
         * Re-check availability immediately before mutation.
         * If one assigned room was taken since proposal time,
         * rebuild the complete proposal and require confirmation again.
         */
        final LocalDate checkOut =
                LocalDate.parse(session.get(SLOT_CHECK_OUT));

        final java.util.Set<String> currentlyAvailable =
                filterByRequestedType(
                        reservationService.getAvailableRooms(
                                checkIn,
                                checkOut),
                        session.get(SLOT_ROOM_TYPE))
                        .stream()
                        .map(room -> room.id().toString())
                        .collect(java.util.stream.Collectors.toSet());

        for (int number = 1; number <= count; number++) {

            final String encoded =
                    session.getOptionIds().get(
                            "batch:" + number);

            if (encoded == null) {
                session.setStep(
                        ConversationStep.WAITING_BATCH_GUEST_LIST);

                return response(
                        "El plan del grupo ya no está completo. "
                                + "Vuelve a pegar la lista.");
            }

            final String[] parts =
                    encoded.split("\\|");

            if (parts.length != 3
                    || !currentlyAvailable.contains(parts[1])) {

                /*
                 * Availability changed after operator saw the proposal.
                 * Rebuild instead of silently assigning a different room.
                 */
                session.clearOptions();
                session.setStep(
                        ConversationStep.WAITING_BATCH_GUEST_LIST);

                return response(
                        "La disponibilidad cambió antes de confirmar. "
                                + "No ejecuté ningún check-in."
                                + LINE_BREAK
                                + "Vuelve a pegar la lista para preparar "
                                + "una nueva asignación.");
            }
        }

        int success = 0;
        int failed = 0;

        final StringBuilder answer =
                new StringBuilder(
                        "RESULTADO CHECK-IN DE GRUPO")
                        .append(LINE_BREAK)
                        .append(LINE_BREAK);

        for (int number = 1; number <= count; number++) {

            final String key =
                    "batch:" + number;

            final String encoded =
                    session.getOptionIds().get(key);

            final String label =
                    session.getOptionLabels().get(key);

            final String[] parts =
                    encoded.split("\\|");

            final UUID guestId =
                    UUID.fromString(parts[0]);

            final UUID roomId =
                    UUID.fromString(parts[1]);

            final int occupants =
                    Integer.parseInt(parts[2]);

            try {

                final StayRequest stayRequest =
                        new StayRequest(
                                hotelId,
                                null,
                                guestId,
                                roomId,
                                StayStatus.CHECKED_IN,
                                checkOut,
                                null,
                                null,
                                occupants,
                                List.of());

                final var stay =
                        stayService.checkIn(stayRequest);

                success++;

                answer.append("✅ ")
                        .append(label);

                if (stay.invoiceCreationFailed()) {
                    answer.append(
                            " — check-in realizado; factura pendiente");
                }

                answer.append(LINE_BREAK);

            } catch (final RuntimeException ex) {

                failed++;

                log.warn(
                        "[BATCH_CHECK_IN] ITEM_FAILED "
                                + "| hotelId={} "
                                + "| item={} "
                                + "| guestId={} "
                                + "| roomId={} "
                                + "| errorType={}",
                        hotelId,
                        number,
                        guestId,
                        roomId,
                        ex.getClass().getSimpleName());

                answer.append("❌ ")
                        .append(label)
                        .append(" — no se pudo completar")
                        .append(LINE_BREAK);
            }
        }

        answer.append(LINE_BREAK)
                .append("Completados: ")
                .append(success)
                .append("/")
                .append(count);

        if (failed > 0) {
            answer.append(LINE_BREAK)
                    .append("Pendientes: ")
                    .append(failed)
                    .append(LINE_BREAK)
                    .append("Los pendientes NO se marcaron "
                            + "como completados.");
        }

        session.clearOptions();
        session.setIntent(LocalIntent.IDLE);

        return response(answer.toString());
    }

    private static List<BatchGuestLine> parseBatchGuestLines(
            final String raw) {

        final List<BatchGuestLine> result =
                new java.util.ArrayList<>();

        if (raw == null || raw.isBlank()) {
            return result;
        }

        final java.util.regex.Pattern linePattern =
                java.util.regex.Pattern.compile(
                        "^(.+?)\\s*[|;]\\s*(\\d+)\\s*"
                                + "(?:personas?|huespedes?)?\\s*$",
                        java.util.regex.Pattern.CASE_INSENSITIVE);

        for (final String originalLine : raw.split("\\R")) {

            final String line = originalLine.trim();

            if (line.isBlank()) {
                continue;
            }

            final java.util.regex.Matcher matcher =
                    linePattern.matcher(line);

            if (!matcher.matches()) {
                return List.of();
            }

            result.add(
                    new BatchGuestLine(
                            matcher.group(1).trim(),
                            Integer.parseInt(
                                    matcher.group(2))));
        }

        return List.copyOf(result);
    }

    private record BatchGuestLine(
            String query,
            int occupants) {
    }

    private record BatchResolvedGuest(
            GuestResponse guest,
            RoomResponse room,
            int occupants) {
    }

    private AssistantChatResponse resolveGuest(
            final ConversationSession session,
            final boolean forCheckIn,
            final UUID hotelId,
            final LocalDate today) {
        final String query = session.get(SLOT_GUEST_QUERY);
        final GuestSearchPageResponse result = guestClient.searchGuestsStrict(query, MAX_GUEST_RESULTS);
        if (result.content().isEmpty()) {
            session.setStep(ConversationStep.WAITING_CREATE_GUEST_DECISION);
            return response("No encontré ningún huésped con “" + query + "”. ¿Deseas crearlo?");
        }
        if (result.content().size() == 1) {
            storeGuest(session, result.content().getFirst());
            session.setStep(ConversationStep.NONE);
            if (forCheckIn) {
                return handlePrepareCheckIn(hotelId, session,
                        new DeterministicParser.ParsedCommand(LocalIntent.UNKNOWN, Map.of()), "", today);
            }
            session.setIntent(LocalIntent.IDLE);
            return response("Huésped encontrado: " + session.get(SLOT_GUEST_FULL_NAME)
                    + formatEmail(session.get(SLOT_GUEST_EMAIL)) + ".");
        }
        session.clearOptions();
        final StringBuilder answer = new StringBuilder("Encontré varios huéspedes. Elige una opción:\n");
        int option = 1;
        for (final GuestResponse guest : result.content()) {
            final String key = Integer.toString(option++);
            final String label = fullName(guest) + formatEmail(guest.email());
            session.getOptionIds().put(key, guest.id().toString());
            session.getOptionLabels().put(key, label);
            answer.append(key).append(". ").append(label).append('\n');
        }
        session.put(SLOT_SELECTION_PURPOSE, forCheckIn ? "CHECK_IN_GUEST" : "FIND_GUEST");
        session.setStep(ConversationStep.WAITING_GUEST_SELECTION);
        return response(answer.toString().trim());
    }

    private AssistantChatResponse selectGuest(
            final ConversationSession session,
            final String input,
            final boolean forCheckIn,
            final UUID hotelId,
            final LocalDate today) {
        final String key = input.trim();
        final String guestId = session.getOptionIds().get(key);
        if (guestId == null) {
            return response("Opción inválida. Elige uno de los números mostrados o escribe “cancelar”.");
        }
        final GuestResponse guest = guestClient.getGuestById(UUID.fromString(guestId));
        storeGuest(session, guest);
        session.clearOptions();
        session.setStep(ConversationStep.NONE);
        if (forCheckIn || "CHECK_IN_GUEST".equals(session.get(SLOT_SELECTION_PURPOSE))) {
            return handlePrepareCheckIn(hotelId, session,
                    new DeterministicParser.ParsedCommand(LocalIntent.UNKNOWN, Map.of()), "", today);
        }
        session.setIntent(LocalIntent.IDLE);
        return response("Huésped seleccionado: " + fullName(guest) + formatEmail(guest.email()) + ".");
    }

    private AssistantChatResponse handleCreateDecision(
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final UUID hotelId,
            final LocalDate today) {
        if (command.intent() == LocalIntent.DECLINE) {
            session.setIntent(LocalIntent.IDLE);
            return response("Operación cancelada. No se creó ningún huésped.");
        }
        if (command.intent() != LocalIntent.CONFIRM) {
            return response("Responde “sí” para iniciar el registro o “no” para cancelar.");
        }
        session.put(SLOT_RETURN_INTENT, session.getIntent().name());
        session.setIntent(LocalIntent.CREATE_GUEST);
        session.setStep(ConversationStep.NONE);
        return handleCreateGuest(hotelId, session, command, "", today);
    }

    private AssistantChatResponse handleCreateGuest(
            final UUID hotelId,
            final ConversationSession session,
            final DeterministicParser.ParsedCommand command,
            final String input,
            final LocalDate today) {
        if (!session.has(SLOT_FIRST_NAME) && session.has(SLOT_GUEST_QUERY)
                && session.get(SLOT_GUEST_QUERY).matches("[^ ]+")) {
            session.put(SLOT_FIRST_NAME, titleCase(session.get(SLOT_GUEST_QUERY)));
        }
        if (!session.has(SLOT_FIRST_NAME)) {
            if (session.getStep() == ConversationStep.WAITING_FIRST_NAME && !input.isBlank()) {
                session.put(SLOT_FIRST_NAME, input.trim());
            } else {
                session.setStep(ConversationStep.WAITING_FIRST_NAME);
                return response("¿Cuál es el nombre del nuevo huésped?");
            }
        }
        if (!session.has(SLOT_LAST_NAME)) {
            if (session.getStep() == ConversationStep.WAITING_LAST_NAME && !input.isBlank()) {
                session.put(SLOT_LAST_NAME, input.trim());
            } else {
                session.setStep(ConversationStep.WAITING_LAST_NAME);
                return response("¿Cuáles son sus apellidos?");
            }
        }
        if (!session.has(SLOT_EMAIL)) {
            session.setStep(ConversationStep.WAITING_EMAIL);
            return response("¿Cuál es su correo electrónico?");
        }
        if (session.getStep() == ConversationStep.WAITING_CREATE_GUEST_CONFIRMATION) {
            if (command.intent() == LocalIntent.DECLINE) {
                session.setIntent(LocalIntent.IDLE);
                return response("Creación cancelada. No se realizó ningún cambio.");
            }
            if (command.intent() != LocalIntent.CONFIRM) {
                return response("Responde “sí” para confirmar o “no” para cancelar.");
            }
            final GuestResponse created = guestClient.createGuest(new GuestCreateRequest(
                    session.get(SLOT_FIRST_NAME),
                    session.get(SLOT_LAST_NAME),
                    session.get(SLOT_EMAIL)));
            storeGuest(session, created);
            if (LocalIntent.PREPARE_CHECK_IN.name().equals(session.get(SLOT_RETURN_INTENT))) {
                session.setIntent(LocalIntent.PREPARE_CHECK_IN);
                session.setStep(ConversationStep.NONE);
                return handlePrepareCheckIn(hotelId, session,
                        new DeterministicParser.ParsedCommand(LocalIntent.UNKNOWN, Map.of()), "", today);
            }
            session.setIntent(LocalIntent.IDLE);
            return response("Huésped creado: " + fullName(created) + ".");
        }
        session.setStep(ConversationStep.WAITING_CREATE_GUEST_CONFIRMATION);
        return response("[PROPUESTA DE NUEVO HUÉSPED]" + LINE_BREAK
                + "Nombre: " + session.get(SLOT_FIRST_NAME) + LINE_BREAK
                + "Apellidos: " + session.get(SLOT_LAST_NAME) + LINE_BREAK
                + "Correo: " + session.get(SLOT_EMAIL) + LINE_BREAK + LINE_BREAK + "¿Confirmas?");
    }

    private AssistantChatResponse resolveRoom(final ConversationSession session) {
        final LocalDate checkIn = LocalDate.parse(session.get(SLOT_CHECK_IN));
        final LocalDate checkOut = LocalDate.parse(session.get(SLOT_CHECK_OUT));
        final List<RoomResponse> rooms = filterByRequestedType(
                reservationService.getAvailableRooms(checkIn, checkOut), session.get(SLOT_ROOM_TYPE));
        if (rooms.isEmpty()) {
            session.remove(SLOT_ROOM_TYPE);
            session.setStep(ConversationStep.WAITING_ROOM_ALTERNATIVE);
            return response("No hay habitaciones de ese tipo disponibles para esas fechas. "
                    + "Indica otro tipo, otras fechas o escribe “cancelar”.");
        }
        if (rooms.size() == 1) {
            storeRoom(session, rooms.getFirst());
            session.setStep(ConversationStep.NONE);
            return null;
        }
        session.clearOptions();
        final StringBuilder answer = new StringBuilder("Hay varias habitaciones disponibles. Elige una:\n");
        int option = 1;
        for (final RoomResponse room : rooms) {
            final String key = Integer.toString(option++);
            session.getOptionIds().put(key, room.id().toString());
            session.getOptionLabels().put(key, formatRoom(room));
            answer.append(key).append(". ").append(formatRoom(room)).append('\n');
        }
        session.setStep(ConversationStep.WAITING_ROOM_SELECTION);
        return response(answer.toString().trim());
    }

    private AssistantChatResponse selectRoom(final ConversationSession session, final String input) {
        final String roomId = session.getOptionIds().get(input.trim());
        if (roomId == null) {
            return response("Opción inválida. Elige uno de los números mostrados o escribe “cancelar”.");
        }
        final LocalDate checkIn = LocalDate.parse(session.get(SLOT_CHECK_IN));
        final LocalDate checkOut = LocalDate.parse(session.get(SLOT_CHECK_OUT));
        final RoomResponse selected = reservationService.getAvailableRooms(checkIn, checkOut).stream()
                .filter(room -> room.id().equals(UUID.fromString(roomId)))
                .findFirst()
                .orElseThrow(() -> new ConflictException("ROOM_NO_LONGER_AVAILABLE"));
        storeRoom(session, selected);
        session.clearOptions();
        session.setStep(ConversationStep.NONE);
        return null;
    }

    private static AssistantChatResponse requireDates(final ConversationSession session) {
        if (session.has(SLOT_PENDING_SINGLE_DATE)) {
            session.setStep(ConversationStep.WAITING_SINGLE_DATE_MEANING);
            return response("La fecha " + session.get(SLOT_PENDING_SINGLE_DATE)
                    + " ¿corresponde a la entrada o a la salida?");
        }
        if (!session.has(SLOT_CHECK_IN)) {
            session.setStep(ConversationStep.WAITING_CHECKIN_DATE);
            return response("¿Cuál es la fecha de entrada?");
        }
        if (!session.has(SLOT_CHECK_OUT)) {
            session.setStep(ConversationStep.WAITING_CHECKOUT_DATE);
            return response("¿Cuál es la fecha de salida?");
        }
        return null;
    }

    private static void resolvePendingDateMeaning(final ConversationSession session, final String input) {
        if (!session.has(SLOT_PENDING_SINGLE_DATE)) {
            return;
        }
        final String normalized = DeterministicParser.normalize(input);
        if (normalized.contains("entrada") || normalized.contains("llegada") || normalized.contains("checkin")) {
            session.put(SLOT_CHECK_IN, session.get(SLOT_PENDING_SINGLE_DATE));
            session.remove(SLOT_PENDING_SINGLE_DATE);
        } else if (normalized.contains("salida") || normalized.contains("checkout")) {
            session.put(SLOT_CHECK_OUT, session.get(SLOT_PENDING_SINGLE_DATE));
            session.remove(SLOT_PENDING_SINGLE_DATE);
        }
    }

    private static void absorbEntities(
            final ConversationSession session, final Map<String, String> entities) {
        entities.forEach((key, value) -> {
            if (DeterministicParser.SLOT_SINGLE_DATE.equals(key)) {
                if (session.getStep() == ConversationStep.WAITING_CHECKIN_DATE) {
                    session.put(SLOT_CHECK_IN, value);
                } else if (session.getStep() == ConversationStep.WAITING_CHECKOUT_DATE) {
                    session.put(SLOT_CHECK_OUT, value);
                } else {
                    session.put(SLOT_PENDING_SINGLE_DATE, value);
                }
            } else {
                session.put(key, value);
            }
        });
    }

    private List<RoomResponse> filterByRequestedType(
            final List<RoomResponse> rooms, final String requestedType) {
        if (requestedType == null || requestedType.isBlank()) {
            return rooms;
        }
        final String normalizedType = canonicalRoomType(requestedType);
        return rooms.stream()
                .filter(room -> canonicalRoomType(room.roomType().name()).contains(normalizedType)
                        || normalizedType.contains(canonicalRoomType(room.roomType().name())))
                .toList();
    }

    private static String canonicalRoomType(final String value) {
        return DeterministicParser.normalize(value)
                .replace("sencillo", "sencilla")
                .replace("estandar", "standard");
    }

    private static void storeGuest(final ConversationSession session, final GuestResponse guest) {
        session.put(SLOT_GUEST_ID, guest.id());
        session.put(SLOT_GUEST_FULL_NAME, fullName(guest));
        if (guest.email() != null && !guest.email().isBlank()) {
            session.put(SLOT_GUEST_EMAIL, guest.email());
        }
    }

    private static void storeRoom(final ConversationSession session, final RoomResponse room) {
        session.put(SLOT_ROOM_ID, room.id());
        session.put(SLOT_ROOM_NUMBER, room.roomNumber());
        session.put(SLOT_ROOM_TYPE_NAME, room.roomType().name());
        session.put(SLOT_ROOM_MAX_OCCUPANCY, room.roomType().maxOccupancy());
    }

    private ZoneId resolveHotelZone(final UUID hotelId) {
        final String timezone = hotelSettingsRepository.findById(hotelId)
                .map(settings -> settings.getTimezone())
                .orElse("America/Monterrey");
        try {
            return ZoneId.of(timezone);
        } catch (final DateTimeException ex) {
            log.warn("Invalid hotel timezone; using application locale | hotelId={}", hotelId);
            return ZoneId.of("America/Monterrey");
        }
    }

    private static String latestUserMessage(final AssistantChatRequest request) {
        for (int index = request.messages().size() - 1; index >= 0; index--) {
            if ("user".equals(request.messages().get(index).role())) {
                return request.messages().get(index).content() == null
                        ? "" : request.messages().get(index).content().trim();
            }
        }
        return "";
    }

    private static String formatRoom(final RoomResponse room) {
        final StringBuilder result = new StringBuilder("Habitación ")
                .append(room.roomNumber()).append(" — ").append(room.roomType().name())
                .append(" — capacidad ").append(room.roomType().maxOccupancy());
        final BigDecimal price = room.resolvedTotalPrice();
        if (price != null) {
            result.append(" — ").append(price).append(" MXN");
        }
        return result.toString();
    }

    private static String requestedTypeSuffix(final ConversationSession session) {
        return session.has(SLOT_ROOM_TYPE) ? " del tipo " + session.get(SLOT_ROOM_TYPE) : "";
    }

    private static String fullName(final GuestResponse guest) {
        return (guest.firstName() + " " + guest.lastName()).trim();
    }

    private static String formatEmail(final String email) {
        return email == null || email.isBlank() ? "" : " (" + email + ")";
    }

    private static String titleCase(final String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-MX")) + value.substring(1);
    }

    private static AssistantChatResponse response(final String answer) {
        return new AssistantChatResponse(answer, List.of());
    }


    private static boolean aiFirstEnabled() {
        final String configured = System.getenv("ASSISTANT_AI_FIRST");

        if (configured == null || configured.isBlank()) {
            return true;
        }

        return !"false".equalsIgnoreCase(configured.trim());
    }

}
