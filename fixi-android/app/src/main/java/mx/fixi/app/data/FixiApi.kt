package mx.fixi.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FixiApi {
    @POST("api/v1/auth/login") suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    @POST("api/v1/auth/refresh") suspend fun refresh(): Response<AuthResponse>
    @POST("api/v1/auth/logout") suspend fun logout(): Response<Unit>
    @GET("api/v1/auth/me") suspend fun me(): Response<UserResponse>
    @GET("api/v1/service-orders") suspend fun serviceOrders(): Response<List<ServiceOrder>>
    @GET("api/v1/customers") suspend fun customers(): Response<PageResponse<Customer>>
    @GET("api/v1/customers/{id}") suspend fun customer(@Path("id") id: String): Response<Customer>
    @GET("api/v1/customers/{customerId}/devices") suspend fun devices(@Path("customerId") customerId: String): Response<List<Device>>
}
