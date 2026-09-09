package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Backend API Service routed via UniGig Kong API Gateway.
 * All LLM API keys, OpenBao secrets, and cloud credentials remain server-side.
 * The mobile client never receives or stores any model API keys (Issue #41, #42).
 */
interface BackendAgentApiService {

    @POST("api/v1/agent/chat")
    suspend fun chat(
        @Header("Authorization") token: String? = null,
        @Body request: AgentChatRequest
    ): AgentChatResponse

    @POST("api/v1/agent/thinking")
    suspend fun highThinking(
        @Header("Authorization") token: String? = null,
        @Body request: AgentThinkingRequest
    ): AgentThinkingResponse

    @POST("api/v1/agent/quick-summary")
    suspend fun quickSummarize(
        @Header("Authorization") token: String? = null,
        @Body request: FastSummaryRequest
    ): FastSummaryResponse

    @POST("api/v1/agent/search-grounding")
    suspend fun searchGrounding(
        @Header("Authorization") token: String? = null,
        @Body request: SearchGroundingRequest
    ): SearchGroundingResponse

    @POST("api/v1/agent/generate-banner")
    suspend fun generateBanner(
        @Header("Authorization") token: String? = null,
        @Body request: GenerateBannerRequest
    ): GenerateBannerResponse

    @POST("api/v1/agent/analyze-artifact")
    suspend fun analyzeArtifact(
        @Header("Authorization") token: String? = null,
        @Body request: AnalyzeArtifactRequest
    ): AnalyzeArtifactResponse

    @POST("api/v1/sandbox/execute")
    suspend fun executeSandbox(
        @Header("Authorization") token: String? = null,
        @Body request: SandboxExecutionRequest
    ): SandboxExecutionResponse

    @POST("api/v1/evaluation/dolos-scan")
    suspend fun evaluateDolos(
        @Header("Authorization") token: String? = null,
        @Body request: DolosScanRequest
    ): DolosScanResponse

    @POST("api/v1/kyc/third-party-validate")
    suspend fun validateThirdPartyKyc(
        @Header("Authorization") token: String? = null,
        @Body request: ThirdPartyKycRequest
    ): ThirdPartyKycResponse
}

object RetrofitClient {
    // Endpoint is build-time routing metadata; credentials and provider secrets stay server-side.
    private const val BASE_URL = BuildConfig.UNIGIG_BACKEND_URL

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: BackendAgentApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendAgentApiService::class.java)
    }
}
