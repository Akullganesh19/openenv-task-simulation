package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.LegalBond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        GigEntity::class,
        MilestoneEntity::class,
        TransactionEntity::class,
        DisputeEntity::class,
        SkillBadgeEntity::class,
        ProposalBidEntity::class,
        LegalBondEntity::class,
        AiAuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gigDao(): GigDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun transactionDao(): TransactionDao
    abstract fun disputeDao(): DisputeDao
    abstract fun skillBadgeDao(): SkillBadgeDao
    abstract fun proposalBidDao(): ProposalBidDao
    abstract fun legalBondDao(): LegalBondDao
    abstract fun aiAuditLogDao(): AiAuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unigig_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // 1. Seed Authorized Users (Issue #8)
            val studentUser = UserEntity(
                id = "usr_student_alex",
                email = "alex.mercer@stanford.edu",
                fullName = "Alex Mercer",
                anonymizedHandle = "Student #S-9102",
                role = "STUDENT",
                kycStatus = "UNVERIFIED",
                strikeCount = 0,
                isBanned = false,
                campusAffiliation = "Stanford University (CS Department)"
            )
            val clientUser = UserEntity(
                id = "usr_client_nexus",
                email = "hiring@nexusai.io",
                fullName = "Nexus AI Labs",
                anonymizedHandle = "Client #C-8421",
                role = "CLIENT",
                kycStatus = "VERIFIED_LEVEL_2",
                strikeCount = 0,
                isBanned = false,
                campusAffiliation = "Enterprise Sponsor"
            )
            val adminUser = UserEntity(
                id = "usr_admin_elena",
                email = "admin@unigig.internal",
                fullName = "Elena Rostova",
                anonymizedHandle = "Admin Tribunal #A-42",
                role = "ADMIN",
                kycStatus = "VERIFIED_LEVEL_3",
                strikeCount = 0,
                isBanned = false,
                campusAffiliation = "UniGig Governance"
            )
            db.userDao().insertUsers(listOf(studentUser, clientUser, adminUser))

            // 2. Seed Real Gigs with counterparty anonymity (Issue #68)
            val gig1 = GigEntity(
                id = "GIG-101",
                title = "Build Automated Judge0 Test Runner for Python Assignments",
                description = "Develop containerized script runner with input/output evaluation and time/memory limits for automated code grading. Submissions must pass automated test suites.",
                clientId = "usr_client_nexus",
                clientName = "Client #C-8421",
                category = "CODING",
                budgetCoins = 450,
                urgency = "RUSH",
                deadline = "In 24 Hours",
                status = "OPEN",
                studentAssigneeId = null,
                studentAssignee = null,
                rubricSummary = "Correctness (40%), Execution sandbox speed (30%), Clean docstrings & typing (30%)",
                testCasesSummary = "3 Judge0 sandboxed unit tests configured",
                isGroupProject = false,
                escrowLocked = true,
                createdAt = System.currentTimeMillis() - 3600000 * 4
            )
            val gig2 = GigEntity(
                id = "GIG-102",
                title = "Fine-Tuning Transformer on Campus Forum QA Dataset",
                description = "Prepare data pipeline, clean scraped Discourse forum threads, and fine-tune small LLM checkpoint for student FAQ queries with benchmark latency under 200ms.",
                clientId = "usr_client_nexus",
                clientName = "Client #C-8421",
                category = "AI_DATA",
                budgetCoins = 680,
                urgency = "STANDARD",
                deadline = "In 4 Days",
                status = "IN_PROGRESS",
                studentAssigneeId = "usr_student_alex",
                studentAssignee = "Student #S-9102",
                rubricSummary = "F1 / Exact Match Score > 85%, Docker reproducibility, latency report",
                testCasesSummary = "Benchmark dataset validation script included",
                isGroupProject = true,
                escrowLocked = true,
                createdAt = System.currentTimeMillis() - 3600000 * 20
            )
            val gig3 = GigEntity(
                id = "GIG-103",
                title = "Mobile Design System & Dark Theme for Student Portal",
                description = "Create cohesive Figma component library and Material 3 design tokens for a student association event ticketing and voting portal.",
                clientId = "usr_client_nexus",
                clientName = "Client #C-1290",
                category = "UI_UX",
                budgetCoins = 320,
                urgency = "STANDARD",
                deadline = "In 3 Days",
                status = "OPEN",
                studentAssigneeId = null,
                studentAssignee = null,
                rubricSummary = "Accessibility AA contrast (35%), Complete token set (35%), Interactive prototype (30%)",
                testCasesSummary = "Figma inspect link + token JSON export",
                isGroupProject = false,
                escrowLocked = true,
                createdAt = System.currentTimeMillis() - 3600000 * 8
            )
            val gig4 = GigEntity(
                id = "GIG-104",
                title = "Literature Review: Decentralized Identity Protocols",
                description = "Draft structured 12-page academic literature review comparing Ballerine, Keycloak, and Verifiable Credentials in university registrar workflows.",
                clientId = "usr_client_nexus",
                clientName = "Client #C-7743",
                category = "WRITING",
                budgetCoins = 390,
                urgency = "CRITICAL",
                deadline = "In 12 Hours",
                status = "IN_REVIEW",
                studentAssigneeId = "usr_student_alex",
                studentAssignee = "Student #S-9102",
                rubricSummary = "Zero plagiarism via Dolos (<5%), APA 7th edition referencing, clear comparison matrix",
                testCasesSummary = "Plagiarism scan & citation verification required",
                isGroupProject = false,
                escrowLocked = true,
                createdAt = System.currentTimeMillis() - 3600000 * 18
            )
            val gig5 = GigEntity(
                id = "GIG-105",
                title = "Peer Mentorship: Data Structures & Algorithms (Python)",
                description = "Conduct 4 weekly 1-on-1 tutoring sessions covering graph traversal, dynamic programming, and mock technical interview preparation for sophomore students.",
                clientId = "usr_client_nexus",
                clientName = "Client #C-3180",
                category = "TUTORING",
                budgetCoins = 240,
                urgency = "STANDARD",
                deadline = "In 2 Weeks",
                status = "OPEN",
                studentAssigneeId = null,
                studentAssignee = null,
                rubricSummary = "Session notes (30%), Code exercises (40%), Student feedback rating >= 4.5 (30%)",
                testCasesSummary = "4 syllabus milestones + exercise submissions",
                isGroupProject = false,
                escrowLocked = true,
                createdAt = System.currentTimeMillis() - 3600000 * 2
            )
            db.gigDao().insertGigs(listOf(gig1, gig2, gig3, gig4, gig5))

            // 3. Seed Milestones
            val m1 = MilestoneEntity(
                id = "MS-101-1",
                gigId = "GIG-101",
                title = "Sandbox Environment Dockerfile & API connector",
                coins = 200,
                status = "Pending",
                submissionHash = null,
                submissionNotes = null,
                plagiarismScore = 0.0f,
                submittedAt = null
            )
            val m2 = MilestoneEntity(
                id = "MS-101-2",
                gigId = "GIG-101",
                title = "Execution Engine & Test Assertions suite",
                coins = 250,
                status = "Pending",
                submissionHash = null,
                submissionNotes = null,
                plagiarismScore = 0.0f,
                submittedAt = null
            )
            val m3 = MilestoneEntity(
                id = "MS-102-1",
                gigId = "GIG-102",
                title = "Data pre-processing & tokenization pipeline",
                coins = 300,
                status = "Approved",
                submissionHash = "git:8f3c1a9b2c4d",
                submissionNotes = "HuggingFace tokenizers with subword BPE pipeline",
                plagiarismScore = 1.8f,
                submittedAt = System.currentTimeMillis() - 3600000 * 16
            )
            val m4 = MilestoneEntity(
                id = "MS-102-2",
                gigId = "GIG-102",
                title = "LoRA weights and inference benchmark demo",
                coins = 380,
                status = "Submitted",
                submissionHash = "git:c4e72a19b8f0",
                submissionNotes = "Rank-16 LoRA checkpoint with 142ms average inference latency",
                plagiarismScore = 2.4f,
                submittedAt = System.currentTimeMillis() - 3600000 * 2
            )
            db.milestoneDao().insertMilestones(listOf(m1, m2, m3, m4))

            // 4. Seed Real Legal Bonds with Computed SHA-256 Hashes (Issue #11, #95)
            val bond1Hash = LegalBond.computeSha256Hash("GIG-101|Client #C-8421|Student #S-9102|450|In 24 Hours|true")
            val bond1 = LegalBondEntity(
                contractId = "BOND-GIG-101",
                gigId = "GIG-101",
                studentHandle = "Student #S-9102",
                clientHandle = "Client #C-8421",
                agreedCoins = 450,
                deadline = "In 24 Hours",
                confidentialityAgreed = true,
                auditHash = bond1Hash,
                isSigned = true,
                signedBy = "Client #C-8421",
                signedAt = System.currentTimeMillis() - 3600000 * 4
            )
            val bond2Hash = LegalBond.computeSha256Hash("GIG-102|Client #C-8421|Student #S-9102|680|In 4 Days|true")
            val bond2 = LegalBondEntity(
                contractId = "BOND-GIG-102",
                gigId = "GIG-102",
                studentHandle = "Student #S-9102",
                clientHandle = "Client #C-8421",
                agreedCoins = 680,
                deadline = "In 4 Days",
                confidentialityAgreed = true,
                auditHash = bond2Hash,
                isSigned = true,
                signedBy = "Student #S-9102",
                signedAt = System.currentTimeMillis() - 3600000 * 20
            )
            db.legalBondDao().insertBond(bond1)
            db.legalBondDao().insertBond(bond2)

            // 5. Seed Real Blnk Double-Entry Ledger Transactions (Issue #8, #10, #35)
            val tx1 = TransactionEntity(
                id = "TX-" + UUID.randomUUID().toString().uppercase(),
                userId = "usr_student_alex",
                type = "TOP_UP",
                amount = 500,
                source = "Campus Bank Transfer (Hyperswitch)",
                destination = "Blnk Vault @student_wallet",
                status = "SETTLED",
                timestamp = System.currentTimeMillis() - 3600000 * 48,
                description = "Account Coin Top-Up ($50.00 USD) via Hyperswitch Instant Bank Transfer",
                actorType = "USER_INITIATED"
            )
            val tx2 = TransactionEntity(
                id = "TX-" + UUID.randomUUID().toString().uppercase(),
                userId = "usr_student_alex",
                type = "ESCROW_RELEASE",
                amount = 270,
                source = "Blnk Escrow (GIG-102)",
                destination = "Student Wallet (Student #S-9102)",
                status = "SETTLED",
                timestamp = System.currentTimeMillis() - 3600000 * 12,
                description = "Milestone 1 Approved: 90% Settlement ($27.00 USD) on GIG-102",
                actorType = "SYSTEM_ESCROW_ENGINE"
            )
            val tx3 = TransactionEntity(
                id = "TX-" + UUID.randomUUID().toString().uppercase(),
                userId = "usr_student_alex",
                type = "PLATFORM_FEE",
                amount = 30,
                source = "Blnk Escrow (GIG-102)",
                destination = "UniGig Protocol Treasury",
                status = "SETTLED",
                timestamp = System.currentTimeMillis() - 3600000 * 12,
                description = "Platform Protocol Fee (10% on GIG-102 Milestone 1)",
                actorType = "SYSTEM_ESCROW_ENGINE"
            )
            val tx4 = TransactionEntity(
                id = "TX-" + UUID.randomUUID().toString().uppercase(),
                userId = "usr_student_alex",
                type = "REFERRAL_BONUS",
                amount = 25,
                source = "UniGig Growth Pool (RefRef)",
                destination = "@student_wallet",
                status = "SETTLED",
                timestamp = System.currentTimeMillis() - 3600000 * 24,
                description = "Campus Peer Referral Payout: New student verified milestone",
                actorType = "SYSTEM_ESCROW_ENGINE"
            )
            val txClient = TransactionEntity(
                id = "TX-" + UUID.randomUUID().toString().uppercase(),
                userId = "usr_client_nexus",
                type = "TOP_UP",
                amount = 2000,
                source = "Corporate SEPA / Card (Hyperswitch)",
                destination = "Blnk Vault @client_wallet",
                status = "SETTLED",
                timestamp = System.currentTimeMillis() - 3600000 * 72,
                description = "Client Treasury Top-Up ($200.00 USD) via Hyperswitch Corporate Billing",
                actorType = "USER_INITIATED"
            )
            val txClientLock = TransactionEntity(
                id = "TX-" + UUID.randomUUID().toString().uppercase(),
                userId = "usr_client_nexus",
                type = "ESCROW_LOCK",
                amount = 450,
                source = "@client_wallet",
                destination = "Blnk Escrow Ledger (GIG-101)",
                status = "IN_FLIGHT",
                timestamp = System.currentTimeMillis() - 3600000 * 4,
                description = "Funds held in Escrow pending milestone delivery on GIG-101",
                actorType = "SYSTEM_ESCROW_ENGINE"
            )
            db.transactionDao().insertTransactions(listOf(tx1, tx2, tx3, tx4, txClient, txClientLock))

            // 6. Seed Disputes (Issue #60)
            val disp1 = DisputeEntity(
                id = "DSP-401",
                gigId = "GIG-104",
                gigTitle = "Literature Review: Decentralized Identity Protocols",
                claimantId = "usr_client_nexus",
                claimant = "Client #C-7743",
                respondentId = "usr_student_alex",
                respondent = "Student #S-9102",
                amount = 120,
                reason = "Draft deliverable missing benchmark comparison section for W3C VC spec",
                status = "AI_MEDIATION",
                verdict = "SmartResolution AI proposes: Student granted 4-hour revision extension; 30 coin penalty if unsubmitted.",
                timestamp = System.currentTimeMillis() - 3600000 * 2
            )
            db.disputeDao().insertDisputes(listOf(disp1))

            // 7. Seed Verified Skill Badges (Issue #53, #54)
            val b1 = SkillBadgeEntity(
                id = "SKL-1",
                userId = "usr_student_alex",
                name = "Kotlin & Compose Expert",
                category = "Coding",
                scorePercent = 96,
                recertificationDaysLeft = 45,
                verified = true
            )
            val b2 = SkillBadgeEntity(
                id = "SKL-2",
                userId = "usr_student_alex",
                name = "Python & PyTorch Specialist",
                category = "AI / Data",
                scorePercent = 91,
                recertificationDaysLeft = 28,
                verified = true
            )
            val b3 = SkillBadgeEntity(
                id = "SKL-3",
                userId = "usr_student_alex",
                name = "Academic Research & Dolos Originality",
                category = "Writing",
                scorePercent = 98,
                recertificationDaysLeft = 60,
                verified = true
            )
            val b4 = SkillBadgeEntity(
                id = "SKL-4",
                userId = "usr_student_alex",
                name = "UI/UX Heuristic Evaluation",
                category = "Design",
                scorePercent = 88,
                recertificationDaysLeft = 14,
                verified = true
            )
            db.skillBadgeDao().insertBadges(listOf(b1, b2, b3, b4))
        }
    }
}
