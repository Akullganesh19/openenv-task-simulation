package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val anonymizedHandle: String,
    val role: String,
    val kycStatus: String,
    val strikeCount: Int = 0,
    val isBanned: Boolean = false,
    val campusAffiliation: String = "Stanford University",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gigs")
data class GigEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val clientId: String,
    val clientName: String, // Anonymized handle (e.g. Client #C-8421)
    val category: String,
    val budgetCoins: Int,
    val urgency: String,
    val deadline: String,
    val status: String,
    val studentAssigneeId: String?,
    val studentAssignee: String?, // Anonymized handle (e.g. Student #S-9102)
    val rubricSummary: String,
    val testCasesSummary: String,
    val isGroupProject: Boolean,
    val escrowLocked: Boolean,
    val createdAt: Long
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val gigId: String,
    val title: String,
    val coins: Int,
    val status: String, // Pending, Submitted, Approved
    val submissionHash: String?,
    val submissionNotes: String?,
    val plagiarismScore: Float,
    val submittedAt: Long?
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String, // UUID v4 format
    val userId: String, // Scoped to user account
    val type: String, // TOP_UP, ESCROW_LOCK, ESCROW_RELEASE, PLATFORM_FEE, PAYOUT_WITHDRAWAL, REFERRAL_BONUS, TIP_SENT, TIP_RECEIVED
    val amount: Int,
    val source: String,
    val destination: String,
    val status: String, // SETTLED, IN_FLIGHT, PENDING_REVIEW
    val timestamp: Long,
    val description: String,
    val actorType: String = "USER_INITIATED", // USER_INITIATED, SYSTEM_ESCROW_ENGINE, TRIBUNAL_ADMIN
    val fiatAmountUsd: Float = amount * 0.10f
) {
    val sourceAccount: String get() = source
    val destinationAccount: String get() = destination
}

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey val id: String,
    val gigId: String,
    val gigTitle: String,
    val claimantId: String,
    val claimant: String,
    val respondentId: String,
    val respondent: String,
    val amount: Int,
    val reason: String,
    val status: String, // AI_MEDIATION, HUMAN_CEO_ESCALATED, APPEAL_PENDING, RESOLVED
    val verdict: String?,
    val timestamp: Long
)

@Entity(tableName = "skill_badges")
data class SkillBadgeEntity(
    @PrimaryKey val id: String,
    val userId: String, // Scoped to student
    val name: String,
    val category: String,
    val scorePercent: Int,
    val recertificationDaysLeft: Int,
    val verified: Boolean,
    val tier: String = "GOLD",
    val credentialHash: String = "sha256:qst_8f4c1e"
) {
    val skillName: String get() = name
    val score: Int get() = scorePercent
}

@Entity(tableName = "proposal_bids")
data class ProposalBidEntity(
    @PrimaryKey val id: String,
    val gigId: String,
    val studentId: String,
    val studentHandle: String,
    val proposedCoins: Int,
    val coverLetter: String,
    val qstScore: Int,
    val mentorSignOff: Boolean,
    val status: String = "DISPATCHED",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "legal_bonds")
data class LegalBondEntity(
    @PrimaryKey val contractId: String,
    val gigId: String,
    val studentHandle: String,
    val clientHandle: String,
    val agreedCoins: Int,
    val deadline: String,
    val confidentialityAgreed: Boolean,
    val auditHash: String,
    val isSigned: Boolean,
    val signedBy: String?,
    val signedAt: Long?
)

@Entity(tableName = "ai_audit_logs")
data class AiAuditLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val actionType: String, // ARBITRATION_VERDICT, CODE_REVIEW, SCOPE_ANALYSIS
    val contextSummary: String,
    val modelUsed: String,
    val output: String,
    val timestamp: Long = System.currentTimeMillis()
)
