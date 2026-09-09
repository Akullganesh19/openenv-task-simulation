package com.example.data.model

import java.security.MessageDigest
import java.util.UUID

enum class UserRole(val label: String, val badgeColorHex: Long) {
    STUDENT("Student Freelancer", 0xFF76A085),
    CLIENT("Project Hirer", 0xFFC46D4E),
    ADMIN("Platform Admin (CEO)", 0xFFD49755)
}

enum class KycStatus(val label: String, val level: Int) {
    UNVERIFIED("Unverified", 0),
    PENDING_REVIEW("KYC Pending Verification", 1),
    PENDING_MANUAL_REVIEW("KYC Pending Manual Review", 1),
    VERIFIED_LEVEL_1("Campus SSO Verified (.edu)", 1),
    VERIFIED_LEVEL_2("Institutional ID Card Verified", 2),
    VERIFIED_LEVEL_3("Full Biometric & Document Verified", 3)
}

data class UserSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val anonymizedHandle: String,
    val role: UserRole,
    val kycStatus: KycStatus = KycStatus.UNVERIFIED,
    val token: String = UUID.randomUUID().toString(),
    val strikeCount: Int = 0,
    val isBanned: Boolean = false,
    val campusAffiliation: String = "Stanford University"
)

enum class GigCategory(val displayName: String, val iconName: String) {
    CODING("Code & Software", "terminal"),
    AI_DATA("AI & Data Science", "psychology"),
    UI_UX("UI/UX & Design", "palette"),
    WRITING("Academic & Writing", "description"),
    TRANSLATION("Translation & Docs", "translate"),
    TUTORING("Peer Mentorship & Tutoring", "school")
}

enum class GigStatus(val label: String) {
    OPEN("Open for Bids"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("Under Review"),
    COMPLETED("Completed & Paid"),
    DISPUTED("In Dispute")
}

enum class Urgency(val label: String, val multiplier: Float) {
    STANDARD("Standard (48h+)", 1.0f),
    RUSH("Rush Job (24h)", 1.3f),
    CRITICAL("Urgent (<12h)", 1.6f)
}

enum class DisputeStatus(val label: String) {
    AI_MEDIATION("AI Arbitrator Reviewing"),
    HUMAN_CEO_ESCALATED("Escalated to CEO / Tribunal"),
    APPEAL_PENDING("Appeal Under Review"),
    RESOLVED("Resolved & Settled")
}

data class QualityRubricCriterion(
    val title: String,
    val description: String,
    val maxPoints: Int,
    val pointsAwarded: Int = 0
)

data class JudgeTestCase(
    val testName: String,
    val input: String,
    val expectedOutput: String,
    val actualOutput: String = "",
    val passed: Boolean = false,
    val executionTimeMs: Long = 0
)

data class LegalBond(
    val contractId: String,
    val gigId: String,
    val studentHandle: String,
    val clientHandle: String,
    val agreedCoins: Int,
    val deadline: String,
    val confidentialityAgreed: Boolean,
    val auditHash: String,
    val isSigned: Boolean = false,
    val signedBy: String? = null,
    val signedAt: Long? = null
) {
    companion object {
        fun computeSha256Hash(content: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(content.toByteArray(Charsets.UTF_8))
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}

data class LedgerBalance(
    val availableCoins: Int,
    val inFlightEscrowCoins: Int,
    val withdrawableCoins: Int
) {
    val escrowLockedCoins: Int get() = inFlightEscrowCoins
    // 1 UniGig Coin = $0.10 USD (Fixed Platform Peg)
    val availableUsd: Float get() = availableCoins * 0.10f
    val inFlightEscrowUsd: Float get() = inFlightEscrowCoins * 0.10f
    val withdrawableUsd: Float get() = withdrawableCoins * 0.10f
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAi: Boolean = false,
    val thinkingNotes: String? = null,
    val groundingSources: List<String> = emptyList(),
    val audioDurationSec: Int? = null
)

data class ProposalBid(
    val bidId: String = "BID-" + UUID.randomUUID().toString().take(8).uppercase(),
    val gigId: String,
    val studentId: String,
    val studentHandle: String,
    val proposedCoins: Int,
    val coverLetter: String,
    val qstScore: Int,
    val mentorSignOff: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
