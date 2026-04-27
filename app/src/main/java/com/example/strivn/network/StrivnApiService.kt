package com.example.strivn.network

import com.example.strivn.network.models.AuthEmailPasswordRequest
import com.example.strivn.network.models.CheckinSubmitRequest
import com.example.strivn.network.models.MetricsSnapshotResponse
import com.example.strivn.network.models.RunResponse
import com.example.strivn.network.models.RunSubmitRequest
import com.example.strivn.network.models.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit contract aligned with the STRIVN FastAPI app (`/api` prefix; [BASE_URL] ends with `/`).
 */
interface StrivnApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: AuthEmailPasswordRequest): TokenResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: AuthEmailPasswordRequest): TokenResponse

    @POST("api/runs")
    suspend fun createRun(@Body body: RunSubmitRequest): MetricsSnapshotResponse

    @GET("api/runs")
    suspend fun getRuns(): List<RunResponse>

    @POST("api/checkin")
    suspend fun createCheckin(@Body body: CheckinSubmitRequest): MetricsSnapshotResponse

    @GET("api/metrics/latest")
    suspend fun getLatestMetrics(): MetricsSnapshotResponse
}
