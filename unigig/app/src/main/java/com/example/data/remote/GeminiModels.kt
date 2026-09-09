package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgentChatRequest(
    val roleInstruction: String,
    val history: List<AgentMessageDto>,
    val message: String,
    val model: String = "gemini-3.5-flash"
)

@JsonClass(generateAdapter = true)
data class AgentMessageDto(
    val speaker: String,
    val text: String
)

@JsonClass(generateAdapter = true)
data class AgentChatResponse(
    val reply: String,
    val modelUsed: String? = null,
    val latencyMs: Long? = null,
    val groundingSources: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AgentThinkingRequest(
    val prompt: String,
    val thinkingLevel: String = "high",
    val gigContext: String? = null
)

@JsonClass(generateAdapter = true)
data class AgentThinkingResponse(
    val thinkingProcess: String,
    val finalVerdict: String
)

@JsonClass(generateAdapter = true)
data class FastSummaryRequest(
    val text: String
)

@JsonClass(generateAdapter = true)
data class FastSummaryResponse(
    val summary: String
)

@JsonClass(generateAdapter = true)
data class SearchGroundingRequest(
    val query: String
)

@JsonClass(generateAdapter = true)
data class SearchGroundingResponse(
    val answer: String,
    val sources: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GenerateBannerRequest(
    val prompt: String,
    val aspectRatio: String = "16:9"
)

@JsonClass(generateAdapter = true)
data class GenerateBannerResponse(
    val imageUrl: String?,
    val status: String
)

@JsonClass(generateAdapter = true)
data class AnalyzeArtifactRequest(
    val prompt: String,
    val artifactType: String,
    val base64Data: String? = null
)

@JsonClass(generateAdapter = true)
data class AnalyzeArtifactResponse(
    val analysisText: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class SandboxExecutionRequest(
    val code: String,
    val language: String,
    val testCases: List<SandboxTestCaseDto>
)

@JsonClass(generateAdapter = true)
data class SandboxTestCaseDto(
    val name: String,
    val input: String,
    val expectedOutput: String
)

@JsonClass(generateAdapter = true)
data class SandboxExecutionResponse(
    val passed: Boolean,
    val executionTimeMs: Long,
    val memoryUsageMb: Float,
    val output: String,
    val testResults: List<SandboxTestResultDto>
)

@JsonClass(generateAdapter = true)
data class SandboxTestResultDto(
    val name: String,
    val passed: Boolean,
    val actualOutput: String,
    val executionTimeMs: Long
)

@JsonClass(generateAdapter = true)
data class DolosScanRequest(
    val milestoneId: String,
    val gigId: String,
    val submittedContent: String,
    val language: String? = null,
    val studentUserId: String? = null
)

@JsonClass(generateAdapter = true)
data class DolosScanResponse(
    val similarityScore: Float,
    val isFlagged: Boolean,
    val summary: String? = null,
    val matchedSubmissionsCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ThirdPartyKycRequest(
    val userId: String,
    val email: String,
    val institutionDomain: String,
    val provider: String = "SheerID / National Student Clearinghouse"
)

@JsonClass(generateAdapter = true)
data class ThirdPartyKycResponse(
    val verified: Boolean,
    val verificationLevel: String = "VERIFIED_LEVEL_3",
    val providerReferenceId: String = "",
    val institutionVerified: String = "",
    val details: String = ""
)
