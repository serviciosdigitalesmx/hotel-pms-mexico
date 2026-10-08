export type ServiceOrderStatus =
  | 'INTAKE' | 'REVIEW' | 'DIAGNOSIS' | 'QUOTED' | 'AUTHORIZED'
  | 'IN_PROGRESS' | 'QA' | 'COMPLETED' | 'CANCELLED';

export interface ServiceOrderResponse {
  id: string;
  hotelId: string;
  branchId?: string;
  customerId: string;
  deviceId?: string;
  status: ServiceOrderStatus;
  summary: string;
  diagnosis?: string;
  workLog?: string;
  assignedUserId?: string;
  authorizedBy?: string;
  authorizationEvidence?: string;
  authorizedAt?: string;
}

export interface ServiceOrderRequest {
  customerId: string;
  deviceId?: string;
  branchId?: string;
  summary: string;
}

export interface ServiceOrderUpdateRequest {
  status?: ServiceOrderStatus;
  diagnosis?: string;
  workLog?: string;
  assignedUserId?: string;
}

