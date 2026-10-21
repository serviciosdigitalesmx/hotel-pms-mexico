package mx.fixi.app.data

import mx.fixi.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class) object NetworkModule {
    @Provides @Singleton fun cookieJar(): CookieJar = object : CookieJar {
        private val cookies = mutableListOf<Cookie>()
        override fun saveFromResponse(url: HttpUrl, values: List<Cookie>) { synchronized(cookies) { cookies.removeAll { cookie -> cookie.name in values.map { value: Cookie -> value.name } }; cookies.addAll(values) } }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(cookies) { cookies.filter { it.matches(url) } }
    }
    @Provides @Singleton fun api(jar: CookieJar): FixiApi {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val csrf = Interceptor { chain ->
            val request = chain.request()
            val safeMethod = request.method in setOf("GET", "HEAD", "OPTIONS", "TRACE")
            if (safeMethod || request.header("X-CSRF-Token") != null) {
                chain.proceed(request)
            } else {
                val token = jar.loadForRequest(request.url)
                    .firstOrNull { it.name == "csrf_token" }
                    ?.value
                val protectedRequest = token?.let {
                    request.newBuilder().header("X-CSRF-Token", it).build()
                } ?: request
                chain.proceed(protectedRequest)
            }
        }
        val client = OkHttpClient.Builder()
            .cookieJar(jar)
            .addInterceptor(csrf)
            .addInterceptor(log)
            .build()
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        return Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client).addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(FixiApi::class.java)
    }
}
