import api from './api';
import type { ServiceOrderRequest, ServiceOrderResponse, ServiceOrderUpdateRequest } from '../types/serviceOrder.types';

const PATH = '/api/v1/service-orders';

export const serviceOrderService = {
  listAll: async (): Promise<ServiceOrderResponse[]> =>
    (await api.get<ServiceOrderResponse[]>(PATH)).data,
  list: async (customerId: string): Promise<ServiceOrderResponse[]> =>
    (await api.get<ServiceOrderResponse[]>(`${PATH}/customer/${customerId}`)).data,
  get: async (id: string): Promise<ServiceOrderResponse> =>
    (await api.get<ServiceOrderResponse>(`${PATH}/${id}`)).data,
  create: async (request: ServiceOrderRequest): Promise<ServiceOrderResponse> =>
    (await api.post<ServiceOrderResponse>(PATH, request)).data,
  update: async (id: string, request: ServiceOrderUpdateRequest): Promise<ServiceOrderResponse> =>
    (await api.patch<ServiceOrderResponse>(`${PATH}/${id}`, request)).data,
  authorize: async (id: string, evidence: string): Promise<ServiceOrderResponse> =>
    (await api.post<ServiceOrderResponse>(`${PATH}/${id}/authorize`, { evidence })).data,
};
