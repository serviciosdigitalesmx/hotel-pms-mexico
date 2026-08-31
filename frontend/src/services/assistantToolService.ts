import { z } from 'zod';
import { billingReportService } from './billingReportService';
import { billingService } from './billingService';
import { dashboardService } from './dashboardService';
import { fbService } from './fbService';
import { guestService } from './guestService';
import { inventoryService } from './inventoryService';
import { quotationService } from './quotationService';
import { rateSeasonService } from './rateSeasonService';
import { reservationService } from './reservationService';
import { stayService } from './stayService';
import { userService } from './userService';
import type { Role } from '../types/auth.types';
import type { InvoiceStatus, PaymentRequest } from '../types/billing.types';
import type { MenuItemRequest, RestaurantOrderRequest } from '../types/fb.types';
import type { GuestRequestDTO } from '../types/guest.types';
import type {
  RateBulkApplyRequest,
  RateSeasonRequest,
  RoomRequest,
  RoomStatus,
  RoomTypeRequest,
} from '../types/inventory.types';
import type { QuotationRequest } from '../types/quotation.types';
import type { ReservationRequest } from '../types/reservation.types';
import type { HotelSettingsRequest, StayRequest } from '../types/stay.types';

const envelopeSchema = z.object({
  operacion: z.string().min(1),
  parametros: z.record(z.string(), z.unknown()),
});

type Parameters = z.infer<typeof envelopeSchema>['parametros'];

const MAX_TOOL_RESULT_CHARS = 10_000;

const requiredString = (parameters: Parameters, key: string): string =>
  z.string().min(1).parse(parameters[key]);

const optionalString = (parameters: Parameters, key: string): string | undefined =>
  z.string().optional().parse(parameters[key]);

const optionalNumber = (parameters: Parameters, key: string, fallback: number): number =>
  z.number().int().nonnegative().optional().default(fallback).parse(parameters[key]);

const requiredData = <T>(parameters: Parameters): T =>
  z.record(z.string(), z.unknown()).parse(parameters.data) as T;

const invoiceStatusSchema = z.enum(['ISSUED', 'PAID', 'CANCELLED']);

const guestRequestSchema = z.object({
  firstName: z.string().min(1),
  lastName: z.string().min(1),
  email: z.email().optional(),
  phone: z.string().min(1).optional(),
  address: z.string().optional(),
  city: z.string().optional(),
  country: z.string().optional(),
  fiscalCode: z.string().optional(),
  vatNumber: z.string().optional(),
  companyName: z.string().optional(),
  sdiCode: z.string().optional(),
  pecEmail: z.email().optional(),
  cap: z.string().optional(),
  comune: z.string().optional(),
  provincia: z.string().optional(),
}).strict();

const isoDateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/);
const stayGuestRequestSchema = z.object({
  firstName: z.string().min(1),
  lastName: z.string().min(1),
  gender: z.string().min(1),
  dateOfBirth: isoDateSchema,
  placeOfBirth: z.string().min(1),
  citizenship: z.string().min(1),
  documentType: z.string().min(1).optional(),
  documentNumber: z.string().min(1).optional(),
  documentPlaceOfIssue: z.string().min(1).optional(),
  isPrimaryGuest: z.boolean(),
  travellerType: z.enum([
    'OSPITE_SINGOLO', 'CAPOFAMIGLIA', 'CAPOGRUPPO', 'FAMILIARE', 'MEMBRO_GRUPPO',
  ]).optional(),
  travelPurpose: z.string().optional(),
});
const stayRequestSchema = z.object({
  hotelId: z.uuid().optional(),
  reservationId: z.uuid().nullish().transform((value) => value ?? undefined),
  guestId: z.uuid(),
  roomId: z.uuid(),
  status: z.literal('CHECKED_IN'),
  expectedCheckOutDate: isoDateSchema.optional(),
  actualCheckInTime: z.string().optional(),
  actualCheckOutTime: z.string().optional(),
  occupantCount: z.number().int().min(1),
  guests: z.array(stayGuestRequestSchema).min(1),
}).superRefine((data, context) => {
  if (!data.reservationId && !data.expectedCheckOutDate) {
    context.addIssue({
      code: 'custom',
      path: ['expectedCheckOutDate'],
      message: 'es obligatoria para un check-in sin reservación',
    });
  }
});

const safeHotelSettingsSchema = z.object({
  hotelName: z.string().optional(),
  address: z.string().optional(),
  logoUrl: z.string().optional(),
  sendReservationConfirmedEmail: z.boolean().optional(),
  sendCheckoutEmail: z.boolean().optional(),
  emailSubjectReservationConfirmed: z.string().optional(),
  emailSubjectCheckout: z.string().optional(),
  emailGreetingText: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  country: z.string().optional(),
  postalCode: z.string().optional(),
  currency: z.string().optional(),
  locale: z.string().optional(),
  timezone: z.string().optional(),
  publicSlug: z.string().optional(),
});

const safeHotelSettings = (parameters: Parameters): HotelSettingsRequest =>
  safeHotelSettingsSchema.parse(parameters.data);

const serialize = (value: unknown): string => {
  const json = JSON.stringify(value ?? { success: true });
  return json.length > MAX_TOOL_RESULT_CHARS
    ? `${json.slice(0, MAX_TOOL_RESULT_CHARS - 1)}…`
    : json;
};

const actionLabels: Record<string, string> = {
  crear_huesped: 'Crear huésped', actualizar_huesped: 'Actualizar huésped',
  eliminar_huesped: 'Eliminar huésped', crear_reservacion: 'Crear reservación',
  actualizar_reservacion: 'Actualizar reservación', eliminar_reservacion: 'Eliminar reservación',
  cambiar_habitacion_reservacion: 'Cambiar habitación de reservación',
  reintentar_email_reservacion: 'Reintentar correo de reservación',
  registrar_check_in: 'Registrar check-in', registrar_check_out: 'Registrar check-out',
  reintentar_factura_estancia: 'Reintentar creación de factura',
  reintentar_email_check_out: 'Reintentar correo de check-out',
  actualizar_estado_habitacion: 'Actualizar estado de habitación',
  agregar_cargo: 'Agregar cargo a la estancia', registrar_pago: 'Registrar pago',
  crear_pedido_fb: 'Crear pedido de restaurante', confirmar_pedido_fb: 'Confirmar pedido y cargarlo',
  crear_habitacion: 'Crear habitación', actualizar_habitacion: 'Actualizar habitación',
  eliminar_habitacion: 'Eliminar habitación', crear_tipo_habitacion: 'Crear tipo de habitación',
  actualizar_tipo_habitacion: 'Actualizar tipo de habitación',
  eliminar_tipo_habitacion: 'Eliminar tipo de habitación', crear_menu_item: 'Crear artículo de menú',
  actualizar_menu_item: 'Actualizar artículo de menú', eliminar_menu_item: 'Eliminar artículo de menú',
  crear_cotizacion: 'Crear cotización', actualizar_cotizacion: 'Actualizar cotización',
  duplicar_cotizacion: 'Duplicar cotización', enviar_cotizacion: 'Enviar cotización',
  convertir_cotizacion: 'Convertir cotización en reservación', rechazar_cotizacion: 'Rechazar cotización',
  eliminar_cotizacion: 'Eliminar cotización', aplicar_tarifa: 'Aplicar tarifa',
  crear_temporada: 'Crear temporada', actualizar_temporada: 'Actualizar temporada',
  eliminar_temporada: 'Eliminar temporada', activar_usuario: 'Activar usuario',
  desactivar_usuario: 'Desactivar usuario', actualizar_configuracion_hotel: 'Actualizar configuración del hotel',
};

const validateActionParameters = (operation: string, parameters: Parameters): void => {
  try {
    if (operation === 'crear_huesped' || operation === 'actualizar_huesped') {
      guestRequestSchema.parse(parameters.data);
    }
    if (operation === 'registrar_check_in') stayRequestSchema.parse(parameters.data);
  } catch (error) {
    if (!(error instanceof z.ZodError)) throw error;
    const fields = [...new Set(error.issues.map((issue) => issue.path.join('.')).filter(Boolean))];
    const detail = fields.length > 0 ? fields.join(', ') : 'data';
    throw new Error(
      `La propuesta de ${actionLabels[operation] ?? operation} está incompleta: ${detail}. `
      + 'El agente debe consultar los IDs reales y solicitar los datos faltantes.',
    );
  }
};

export const assistantToolService = {
  describeAction: (argumentsValue: unknown): { title: string; parameters: Parameters } => {
    const { operacion, parametros } = envelopeSchema.parse(argumentsValue);
    validateActionParameters(operacion, parametros);
    return { title: actionLabels[operacion] ?? operacion, parameters: parametros };
  },

  executeRead: async (argumentsValue: unknown, role: Role): Promise<string> => {
    const { operacion, parametros } = envelopeSchema.parse(argumentsValue);
    let result: unknown;
    switch (operacion) {
      case 'resumen_operativo': result = await dashboardService.getDashboardStats(role === 'ADMIN' || role === 'OWNER'); break;
      case 'buscar_huespedes': result = await guestService.searchGuestsPaged(optionalString(parametros, 'query') ?? '', optionalNumber(parametros, 'page', 0), optionalNumber(parametros, 'size', 20)); break;
      case 'obtener_huesped': result = await guestService.getGuestById(requiredString(parametros, 'id')); break;
      case 'buscar_reservaciones': result = await reservationService.searchReservations({ query: optionalString(parametros, 'query'), upcomingOnly: z.boolean().optional().parse(parametros.upcomingOnly), page: optionalNumber(parametros, 'page', 0), size: optionalNumber(parametros, 'size', 20) }); break;
      case 'obtener_reservacion': result = await reservationService.getReservationById(requiredString(parametros, 'id')); break;
      case 'listar_estancias': result = await stayService.getAllStays(optionalNumber(parametros, 'page', 0), optionalNumber(parametros, 'size', 20)); break;
      case 'obtener_estancia': result = await stayService.getStayById(requiredString(parametros, 'id')); break;
      case 'listar_habitaciones': result = await inventoryService.getAllRooms(optionalNumber(parametros, 'page', 0), optionalNumber(parametros, 'size', 100)); break;
      case 'obtener_habitacion': result = await inventoryService.getRoomById(requiredString(parametros, 'id')); break;
      case 'habitaciones_disponibles': result = await inventoryService.getAvailableRooms(requiredString(parametros, 'checkInDate'), requiredString(parametros, 'checkOutDate')); break;
      case 'listar_tipos_habitacion': result = await inventoryService.getAllRoomTypes(); break;
      case 'buscar_facturas': result = await billingService.searchInvoices({ status: invoiceStatusSchema.optional().parse(parametros.status) as InvoiceStatus | undefined, query: optionalString(parametros, 'query'), dateFrom: optionalString(parametros, 'dateFrom'), dateTo: optionalString(parametros, 'dateTo'), page: optionalNumber(parametros, 'page', 0), size: optionalNumber(parametros, 'size', 20) }); break;
      case 'obtener_factura': result = await billingService.getInvoiceById(requiredString(parametros, 'id')); break;
      case 'obtener_factura_estancia': result = await billingService.getInvoiceByStayId(requiredString(parametros, 'stayId')); break;
      case 'listar_pedidos_fb': result = await fbService.getAllOrders(); break;
      case 'pedidos_fb_estancia': result = await fbService.getOrdersByStayId(requiredString(parametros, 'stayId')); break;
      case 'listar_menu': result = await fbService.getMenuItems(); break;
      case 'listar_cotizaciones': result = await quotationService.getAllQuotations(optionalNumber(parametros, 'page', 0), optionalNumber(parametros, 'size', 20)); break;
      case 'obtener_cotizacion': result = await quotationService.getQuotationById(requiredString(parametros, 'id')); break;
      case 'calendario_tarifas': result = await rateSeasonService.getRateCalendar(requiredString(parametros, 'from'), requiredString(parametros, 'to')); break;
      case 'listar_temporadas': result = await rateSeasonService.listSeasons(requiredString(parametros, 'roomTypeId')); break;
      case 'reporte_financiero': result = await billingReportService.getOwnerFinancialReport(requiredString(parametros, 'startDate'), requiredString(parametros, 'endDate')); break;
      case 'listar_usuarios': result = await userService.listUsers(); break;
      case 'obtener_configuracion_hotel': result = await stayService.getHotelSettings(); break;
      default: throw new Error(`Consulta no permitida: ${operacion}`);
    }
    return serialize(result);
  },

  executeAction: async (argumentsValue: unknown): Promise<string> => {
    const { operacion, parametros } = envelopeSchema.parse(argumentsValue);
    let result: unknown;
    switch (operacion) {
      case 'crear_huesped': result = await guestService.createGuest(guestRequestSchema.parse(parametros.data) as GuestRequestDTO); break;
      case 'actualizar_huesped': result = await guestService.updateGuest(requiredString(parametros, 'id'), guestRequestSchema.parse(parametros.data) as GuestRequestDTO); break;
      case 'eliminar_huesped': await guestService.deleteGuest(requiredString(parametros, 'id')); result = { success: true }; break;
      case 'crear_reservacion': result = await reservationService.createReservation(requiredData<ReservationRequest>(parametros)); break;
      case 'actualizar_reservacion': result = await reservationService.updateReservation(requiredString(parametros, 'id'), requiredData<ReservationRequest>(parametros)); break;
      case 'eliminar_reservacion': await reservationService.deleteReservation(requiredString(parametros, 'id')); result = { success: true }; break;
      case 'cambiar_habitacion_reservacion': {
        const id = requiredString(parametros, 'id');
        const current = await reservationService.getReservationById(id);
        const oldRoomId = requiredString(parametros, 'roomIdActual');
        const newRoomId = requiredString(parametros, 'roomIdNuevo');
        result = await reservationService.updateReservation(id, { ...current, lineItems: current.lineItems.map((item) => ({ roomId: item.roomId === oldRoomId ? newRoomId : item.roomId })) });
        break;
      }
      case 'reintentar_email_reservacion': result = await reservationService.retryConfirmationEmail(requiredString(parametros, 'id')); break;
      case 'registrar_check_in': result = await stayService.createStay(stayRequestSchema.parse(parametros.data) as StayRequest); break;
      case 'registrar_check_out': result = await stayService.checkOut(requiredString(parametros, 'id')); break;
      case 'reintentar_factura_estancia': result = await stayService.retryInvoiceCreation(requiredString(parametros, 'id')); break;
      case 'reintentar_email_check_out': result = await stayService.retryCheckoutEmail(requiredString(parametros, 'id')); break;
      case 'actualizar_estado_habitacion': result = await inventoryService.updateRoomStatus(requiredString(parametros, 'id'), requiredString(parametros, 'status') as RoomStatus); break;
      case 'agregar_cargo': result = await billingService.addCharge(requiredString(parametros, 'stayId'), requiredData(parametros)); break;
      case 'registrar_pago': result = await billingService.processPayment(requiredString(parametros, 'invoiceId'), requiredData<PaymentRequest>(parametros)); break;
      case 'crear_pedido_fb': result = await fbService.createOrder(requiredData<RestaurantOrderRequest>(parametros)); break;
      case 'confirmar_pedido_fb': result = await fbService.confirmOrder(requiredString(parametros, 'id')); break;
      case 'crear_habitacion': result = await inventoryService.createRoom(requiredData<RoomRequest>(parametros)); break;
      case 'actualizar_habitacion': result = await inventoryService.updateRoom(requiredString(parametros, 'id'), requiredData<RoomRequest>(parametros)); break;
      case 'eliminar_habitacion': await inventoryService.deleteRoom(requiredString(parametros, 'id')); result = { success: true }; break;
      case 'crear_tipo_habitacion': result = await inventoryService.createRoomType(requiredData<RoomTypeRequest>(parametros)); break;
      case 'actualizar_tipo_habitacion': result = await inventoryService.updateRoomType(requiredString(parametros, 'id'), requiredData<RoomTypeRequest>(parametros)); break;
      case 'eliminar_tipo_habitacion': await inventoryService.deleteRoomType(requiredString(parametros, 'id')); result = { success: true }; break;
      case 'crear_menu_item': result = await fbService.createMenuItem(requiredData<MenuItemRequest>(parametros)); break;
      case 'actualizar_menu_item': result = await fbService.updateMenuItem(requiredString(parametros, 'id'), requiredData<MenuItemRequest>(parametros)); break;
      case 'eliminar_menu_item': await fbService.deleteMenuItem(requiredString(parametros, 'id')); result = { success: true }; break;
      case 'crear_cotizacion': result = await quotationService.createQuotation(requiredData<QuotationRequest>(parametros)); break;
      case 'actualizar_cotizacion': result = await quotationService.updateQuotation(requiredString(parametros, 'id'), requiredData<QuotationRequest>(parametros)); break;
      case 'duplicar_cotizacion': result = await quotationService.duplicateQuotation(requiredString(parametros, 'id')); break;
      case 'enviar_cotizacion': result = await quotationService.sendQuotation(requiredString(parametros, 'id')); break;
      case 'convertir_cotizacion': result = await quotationService.convertToReservation(requiredString(parametros, 'id'), optionalString(parametros, 'optionId')); break;
      case 'rechazar_cotizacion': result = await quotationService.declineQuotation(requiredString(parametros, 'id')); break;
      case 'eliminar_cotizacion': await quotationService.deleteQuotation(requiredString(parametros, 'id')); result = { success: true }; break;
      case 'aplicar_tarifa': result = await rateSeasonService.bulkApplyRate(requiredData<RateBulkApplyRequest>(parametros)); break;
      case 'crear_temporada': result = await rateSeasonService.createSeason(requiredString(parametros, 'roomTypeId'), requiredData<RateSeasonRequest>(parametros)); break;
      case 'actualizar_temporada': result = await rateSeasonService.updateSeason(requiredString(parametros, 'roomTypeId'), requiredString(parametros, 'id'), requiredData<RateSeasonRequest>(parametros)); break;
      case 'eliminar_temporada': await rateSeasonService.deleteSeason(requiredString(parametros, 'roomTypeId'), requiredString(parametros, 'id')); result = { success: true }; break;
      case 'activar_usuario': result = await userService.activateUser(requiredString(parametros, 'id')); break;
      case 'desactivar_usuario': result = await userService.deactivateUser(requiredString(parametros, 'id')); break;
      case 'actualizar_configuracion_hotel': result = await stayService.updateHotelSettings(safeHotelSettings(parametros)); break;
      default: throw new Error(`Acción no permitida: ${operacion}`);
    }
    return serialize(result);
  },
};
