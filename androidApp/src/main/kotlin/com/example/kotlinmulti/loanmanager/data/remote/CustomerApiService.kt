package com.example.kotlinmulti.loanmanager.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomerApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @GET("api/loans")
    suspend fun getLoans(
        @Header("Authorization") token: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): LoansApiResponse

    // If your BASE_URL is "http://10.0.2.2:8000/"
    @GET("api/repayments/loan/{loanId}")
    suspend fun getRepaymentHistory(
        @Path("loanId") loanId: String,
        @Header("Authorization") token: String? = null
    ): RepaymentHistoryApiResponse

    companion object {
        const val BASE_URL = "http://192.168.99.10:8000/"
        //const val BASE_URL = "http://172.16.0.150:8000/"

        fun create(): CustomerApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CustomerApiService::class.java)
        }
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
