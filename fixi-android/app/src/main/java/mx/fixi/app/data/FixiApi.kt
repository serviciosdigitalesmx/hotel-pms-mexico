package mx.fixi.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import retrofit2.http.PUT

interface FixiApi {
    @POST("api/v1/auth/login") suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    @POST("api/v1/auth/refresh") suspend fun refresh(): Response<AuthResponse>
    @POST("api/v1/auth/logout") suspend fun logout(): Response<Unit>
    @GET("api/v1/auth/me") suspend fun me(): Response<UserResponse>
    @GET("api/v1/service-orders") suspend fun serviceOrders(): Response<List<ServiceOrder>>
    @GET("api/v1/customers") suspend fun customers(): Response<PageResponse<Customer>>
    @GET("api/v1/customers/{id}") suspend fun customer(@Path("id") id: String): Response<Customer>
    @GET("api/v1/customers/{customerId}/devices") suspend fun devices(@Path("customerId") customerId: String): Response<List<Device>>
    @GET("api/v1/service-orders/{id}") suspend fun serviceOrder(@Path("id") id: String): Response<ServiceOrder>
    @GET("api/v1/service-orders/{id}/timeline") suspend fun timeline(@Path("id") id: String): Response<List<TimelineEvent>>
    @GET("api/v1/service-orders/{id}/tests") suspend fun tests(@Path("id") id: String): Response<List<RepairTest>>
    @GET("api/v1/inventory/products") suspend fun inventory(): Response<List<InventoryProduct>>
    @Multipart @POST("api/v1/documents") suspend fun uploadEvidence(@Part file: MultipartBody.Part): Response<Unit>
    @POST("api/v1/customers") suspend fun createCustomer(@Body request: CustomerRequest): Response<Customer>
    @POST("api/v1/customers/{customerId}/devices") suspend fun createDevice(@Path("customerId") customerId: String, @Body request: DeviceRequest): Response<Device>
    @POST("api/v1/service-orders") suspend fun createServiceOrder(@Body request: ServiceOrderRequest): Response<ServiceOrder>
    @POST("api/v1/service-orders/{id}/authorize") suspend fun authorize(@Path("id") id: String): Response<ActionResponse>
    @POST("api/v1/service-orders/{id}/reject") suspend fun reject(@Path("id") id: String): Response<ActionResponse>
    @POST("api/v1/service-orders/{id}/deliver") suspend fun deliver(@Path("id") id: String): Response<ActionResponse>
    @GET("api/v1/auth/branches") suspend fun branches(): Response<List<Branch>>
}
