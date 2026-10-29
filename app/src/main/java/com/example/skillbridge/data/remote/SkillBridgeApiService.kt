package com.example.skillbridge.data.remote

import retrofit2.http.*

data class ApiResponseDto<T>(
    val success: Boolean,
    val correlationId: String,
    val data: T?,
    val error: ApiErrorDto?
)

data class ApiErrorDto(
    val code: String,
    val message: String
)

data class CategoryDto(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val workType: String,
    val basePricePaise: Long
)

data class LocalJobRequestDto(
    val categoryId: String,
    val problemDescription: String,
    val urgency: String,
    val address: String,
    val city: String,
    val budgetPaise: Long
)

data class JobDto(
    val jobId: String,
    val status: String,
    val escrowHeld: Boolean,
    val budgetPaise: Long,
    val idempotencyKey: String?,
    val matchExplanation: String?
)

data class AdvanceStatusDto(
    val targetStatus: String,
    val photoProofUrl: String? = null
)

data class ReleaseEscrowDto(
    val rating: Float,
    val comment: String? = null
)

data class EscrowSettlementDto(
    val jobId: String,
    val status: String,
    val escrowReleased: Boolean,
    val debitEscrowLiabilityPaise: Long,
    val creditPlatformCommissionPaise: Long,
    val creditWorkerWalletPaise: Long,
    val invoiceNumber: String
)

interface SkillBridgeApiService {

    @GET("categories")
    suspend fun getCategories(): ApiResponseDto<List<CategoryDto>>

    @POST("jobs/request-local")
    suspend fun requestLocalJob(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: LocalJobRequestDto
    ): ApiResponseDto<JobDto>

    @POST("jobs/{id}/accept")
    suspend fun acceptJob(
        @Path("id") jobId: String,
        @Query("workerId") workerId: String
    ): ApiResponseDto<JobDto>

    @POST("jobs/{id}/advance-status")
    suspend fun advanceStatus(
        @Path("id") jobId: String,
        @Body request: AdvanceStatusDto
    ): ApiResponseDto<JobDto>

    @POST("jobs/{id}/release-escrow")
    suspend fun releaseEscrow(
        @Path("id") jobId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ReleaseEscrowDto
    ): ApiResponseDto<EscrowSettlementDto>
}
