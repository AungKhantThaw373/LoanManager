package com.example.kotlinmulti.loanmanager.data.remote

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface LoanApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>

    @GET("api/users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): CurrentUserApiResponse

    @GET("api/loans")
    suspend fun getLoans(
        @Header("Authorization") token: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10,
        @Query("search") search: String? = null,
        @Query("loanType") loanType: String? = null,
        @Query("status") status: String? = null
    ): LoansApiResponse

    @GET("api/loans/{loanId}")
    suspend fun getLoanDetails(
        @Path("loanId") loanId: String,
        @Header("Authorization") token: String? = null
    ): LoanDetailApiResponse

    @GET("api/repayments/loan/{loanId}")
    suspend fun getRepaymentHistory(
        @Path("loanId") loanId: String,
        @Header("Authorization") token: String? = null
    ): RepaymentHistoryApiResponse

    @GET("api/repayments")
    suspend fun getRepayments(
        @Header("Authorization") token: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null
    ): RepaymentsApiResponse

    companion object {
        val BASE_URL: String =  "http://192.168.99.10:8000/" //BuildConfig.LOAN_API_BASE_URL

        fun create(): LoanApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LoanApiService::class.java)
        }

        val instance: LoanApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { create() }
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginApiResponse(
    val status: String?,
    val message: String?,
    val token: String?
)

data class CurrentUserApiResponse(
    val status: String? = null,
    val message: String? = null,
    val user: CurrentUserDto? = null
)

data class CurrentUserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)
