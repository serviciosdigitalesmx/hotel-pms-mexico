export type OrderStatus = 'PENDING' | 'PREPARING' | 'PREPARED' | 'READY' | 'DELIVERED' | 'CANCELLED' | 'BILLED_TO_ROOM';

export interface MenuItemResponse {
  id: string;
  name: string;
  price: number;
  category: string;
  description: string | null;
  available: boolean;
  inventoryProductId?: string | null;
}

export interface MenuItemRequest {
  name: string;
  price: number;
  category: string;
  description?: string;
  available: boolean;
  inventoryProductId?: string | null;
}

export interface OrderItemRequest {
  menuItemId: string;
  quantity: number;
}

export interface OrderItemResponse {
  id: string; // UUID
  menuItemId?: string | null;
  inventoryProductId?: string | null;
  itemName: string;
  quantity: number;
  unitPrice: number;
}

export interface RestaurantOrderRequest {
  stayId: string;
  branchId?: string | null;
  items: OrderItemRequest[];
}

export interface RestaurantOrderResponse {
  id: string; // UUID
  stayId: string;
  branchId?: string | null;
  roomNumber: string | null;
  guestDisplayName: string | null;
  orderDate: string; // ISO DateTime
  totalAmount: number;
  status: OrderStatus;
  items: OrderItemResponse[];
  createdAt: string;
  updatedAt: string;
}
