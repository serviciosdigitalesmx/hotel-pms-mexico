package mx.fixi.app.data

import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val username: String, val password: String)
@Serializable data class AuthResponse(val username: String? = null, val tenantId: String? = null, val branchId: String? = null)
@Serializable data class UserResponse(val username: String? = null, val email: String? = null, val tenantId: String? = null, val branchId: String? = null, val roles: List<String> = emptyList(), val capabilities: List<String> = emptyList())
@Serializable data class ServiceOrder(val id: String? = null, val orderNumber: String? = null, val status: String? = null, val customerName: String? = null, val deviceDescription: String? = null, val assignedTechnician: String? = null)
@Serializable data class Customer(val id: String? = null, val firstName: String? = null, val lastName: String? = null, val email: String? = null, val phone: String? = null)
@Serializable data class Device(val id: String? = null, val brand: String? = null, val model: String? = null, val serialNumber: String? = null, val customerId: String? = null)
@Serializable data class PageResponse<T>(val content: List<T> = emptyList(), val totalElements: Long = 0, val totalPages: Int = 0)
