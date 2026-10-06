package mx.fixi.app.data

import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val username: String, val password: String)
@Serializable data class AuthResponse(val username: String? = null, val tenantId: String? = null, val branchId: String? = null)
@Serializable data class UserResponse(val username: String? = null, val email: String? = null, val tenantId: String? = null, val branchId: String? = null, val roles: List<String> = emptyList(), val capabilities: List<String> = emptyList())
@Serializable data class ServiceOrder(val id: String? = null, val orderNumber: String? = null, val status: String? = null, val customerName: String? = null, val deviceDescription: String? = null, val assignedTechnician: String? = null)
@Serializable data class Customer(val id: String? = null, val firstName: String? = null, val lastName: String? = null, val email: String? = null, val phone: String? = null)
@Serializable data class Device(val id: String? = null, val brand: String? = null, val model: String? = null, val serialNumber: String? = null, val customerId: String? = null)
@Serializable data class PageResponse<T>(val content: List<T> = emptyList(), val totalElements: Long = 0, val totalPages: Int = 0)
@Serializable data class Diagnostic(val id: String? = null, val orderId: String? = null, val summary: String? = null, val status: String? = null)
@Serializable data class Quotation(val id: String? = null, val orderId: String? = null, val status: String? = null, val total: Double? = null)
@Serializable data class TimelineEvent(val id: String? = null, val type: String? = null, val description: String? = null, val createdAt: String? = null)
@Serializable data class RepairTest(val id: String? = null, val orderId: String? = null, val name: String? = null, val passed: Boolean? = null)
@Serializable data class InventoryProduct(val id: String? = null, val sku: String? = null, val name: String? = null, val quantity: Double? = null)
@Serializable data class CustomerRequest(val firstName: String, val lastName: String, val email: String? = null, val phone: String? = null)
@Serializable data class DeviceRequest(val brand: String, val model: String, val serialNumber: String? = null, val customerId: String)
@Serializable data class ServiceOrderRequest(val customerId: String, val deviceId: String, val reportedFailure: String, val branchId: String? = null)
@Serializable data class ActionResponse(val message: String? = null, val status: String? = null)
@Serializable data class Branch(val id: String? = null, val name: String? = null, val code: String? = null)
