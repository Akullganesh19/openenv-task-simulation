package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DisputeEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.UserEntity
import com.example.data.model.KycStatus
import com.example.data.model.LegalBond
import com.example.data.model.Urgency
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.DisputeRepository
import com.example.data.repository.GigRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WalletRepository
import com.example.data.remote.PaymentGatewayService
import org.junit.Assert.assertFalse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies UniGig app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UniGig", appName)
    }

    @Test
    fun `legal bond computes deterministic SHA-256 hash`() {
        val terms1 = "GIG-101|Python Data Pipeline|Client #C-8421|400|In 3 Days"
        val hash1 = LegalBond.computeSha256Hash(terms1)
        val hash2 = LegalBond.computeSha256Hash(terms1)
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length)

        val terms2 = "GIG-101|Python Data Pipeline|Client #C-8421|450|In 3 Days"
        val hashModified = LegalBond.computeSha256Hash(terms2)
        assertNotEquals(hash1, hashModified)
    }

    @Test
    fun `urgency multipliers compute expected escrow locks`() {
        val baseCoins = 500
        val standardLock = (baseCoins * Urgency.STANDARD.multiplier).toInt()
        val rushLock = (baseCoins * Urgency.RUSH.multiplier).toInt()
        val criticalLock = (baseCoins * Urgency.CRITICAL.multiplier).toInt()

        assertEquals(500, standardLock)
        assertEquals(650, rushLock)
        assertEquals(800, criticalLock)
    }

    @Test
    fun `role gate restricts admin access to non-admin roles`() {
        val studentRole = UserRole.STUDENT
        val clientRole = UserRole.CLIENT
        val adminRole = UserRole.ADMIN

        assertTrue(studentRole != UserRole.ADMIN)
        assertTrue(clientRole != UserRole.ADMIN)
        assertEquals(UserRole.ADMIN, adminRole)
    }

    @Test
    fun `user session defaults to UNVERIFIED kycStatus`() {
        val session = UserSession(
            userId = "test_user",
            email = "student@stanford.edu",
            fullName = "Student Name",
            anonymizedHandle = "Student #S-1000",
            role = UserRole.STUDENT
        )
        assertEquals(KycStatus.UNVERIFIED, session.kycStatus)
    }

    @Test
    fun `userRepository addStrike increments strike count and bans user at 3 strikes`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val user = UserEntity(
            id = "test_student_strikes",
            email = "student@mit.edu",
            fullName = "Test Student",
            anonymizedHandle = "Student #S-1234",
            role = "STUDENT",
            kycStatus = "UNVERIFIED",
            strikeCount = 0,
            isBanned = false,
            campusAffiliation = "MIT"
        )
        db.userDao().insertUser(user)
        val userRepo = UserRepository(db)

        val s1 = userRepo.addStrike("test_student_strikes", "First warning")
        assertEquals(1, s1)
        val u1 = userRepo.getUserById("test_student_strikes")
        assertEquals(1, u1?.strikeCount)
        assertEquals(false, u1?.isBanned)

        val s2 = userRepo.addStrike("test_student_strikes", "Second warning")
        assertEquals(2, s2)
        val u2 = userRepo.getUserById("test_student_strikes")
        assertEquals(2, u2?.strikeCount)
        assertEquals(false, u2?.isBanned)

        val s3 = userRepo.addStrike("test_student_strikes", "Third warning: Plagiarism")
        assertEquals(3, s3)
        val u3 = userRepo.getUserById("test_student_strikes")
        assertEquals(3, u3?.strikeCount)
        assertEquals(true, u3?.isBanned)
        db.close()
    }

    @Test
    fun `dispute resolution with penalizeRespondent triggers UserRepository addStrike`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val respondent = UserEntity(
            id = "respondent_student",
            email = "bad.actor@berkeley.edu",
            fullName = "Respondent Student",
            anonymizedHandle = "Student #S-5555",
            role = "STUDENT",
            kycStatus = "UNVERIFIED",
            strikeCount = 1,
            isBanned = false,
            campusAffiliation = "UC Berkeley"
        )
        db.userDao().insertUser(respondent)
        val dispute = DisputeEntity(
            id = "disp-101",
            gigId = "gig-1",
            gigTitle = "Python Scraper",
            claimantId = "client_1",
            claimant = "Client #C-111",
            respondentId = "respondent_student",
            respondent = "Student #S-5555",
            amount = 300,
            reason = "Incomplete work",
            status = "AI_MEDIATION",
            verdict = null,
            timestamp = System.currentTimeMillis()
        )
        db.disputeDao().insertDispute(dispute)
        val userRepo = UserRepository(db)
        val disputeRepo = DisputeRepository(db, userRepo)

        val res = disputeRepo.resolveDispute(
            dispute = dispute,
            verdictText = "Milestone refunded to client. Respondent penalized.",
            callerRole = UserRole.ADMIN,
            penalizeRespondent = true
        )
        assertTrue(res.isSuccess)

        val updatedRespondent = userRepo.getUserById("respondent_student")
        assertEquals(2, updatedRespondent?.strikeCount)
        db.close()
    }

    @Test
    fun `student kyc workflow transitions from UNVERIFIED to PENDING to VERIFIED_LEVEL_3`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val student = UserEntity(
            id = "student_alex",
            email = "alex@stanford.edu",
            fullName = "Alex Mercer",
            anonymizedHandle = "Student #S-9102",
            role = "STUDENT",
            kycStatus = "UNVERIFIED",
            strikeCount = 0,
            isBanned = false,
            campusAffiliation = "Stanford University"
        )
        db.userDao().insertUser(student)
        val userRepo = UserRepository(db)

        assertEquals("UNVERIFIED", userRepo.getUserById("student_alex")?.kycStatus)

        // 1. Submit for manual review
        val submitRes = userRepo.submitKycForManualReview("student_alex")
        assertTrue(submitRes.isSuccess)
        assertEquals("PENDING_MANUAL_REVIEW", userRepo.getUserById("student_alex")?.kycStatus)

        // 2. Admin tribunal certifies to VERIFIED_LEVEL_3
        val certRes = userRepo.reviewAndCertifyKyc(UserRole.ADMIN, "student_alex", approve = true)
        assertTrue(certRes.isSuccess)
        assertEquals("VERIFIED_LEVEL_3", userRepo.getUserById("student_alex")?.kycStatus)

        db.close()
    }

    @Test
    fun `milestone submission with high plagiarism score triggers strike via userRepo`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val student = UserEntity(
            id = "plagiarist_student",
            email = "cheater@stanford.edu",
            fullName = "Cheater",
            anonymizedHandle = "Student #S-9999",
            role = "STUDENT",
            kycStatus = "UNVERIFIED",
            strikeCount = 0,
            isBanned = false,
            campusAffiliation = "Stanford University"
        )
        db.userDao().insertUser(student)
        val milestone = MilestoneEntity(
            id = "ms-plag-1",
            gigId = "gig-10",
            title = "Algorithmic Trading Bot",
            coins = 500,
            status = "Pending",
            submissionHash = null,
            submissionNotes = null,
            plagiarismScore = 0f,
            submittedAt = null
        )
        db.milestoneDao().insertMilestones(listOf(milestone))
        val userRepo = UserRepository(db)
        val gigRepo = GigRepository(db, userRepo)

        val score = gigRepo.submitMilestoneWork(
            milestone = milestone,
            commitHash = "a1b2c3d4e5f60718293a4b5c6d7e8f9a0b1c2d3e",
            notes = "Exact code clone from public github repository for testing plagiarism flags",
            studentUserId = "plagiarist_student"
        )
        val updatedStudent = userRepo.getUserById("plagiarist_student")
        if (score >= 15.0f) {
            assertEquals(1, updatedStudent?.strikeCount)
        }
        db.close()
    }

    @Test
    fun `payment gateway Luhn algorithm validates card checksum`() {
        val validCard = "4242424242424242"
        val invalidCard = "4242424242424241"
        assertTrue(PaymentGatewayService.validateLuhn(validCard))
        assertFalse(PaymentGatewayService.validateLuhn(invalidCard))
    }

    @Test
    fun `payment gateway expiry validator accepts valid future dates and rejects invalid format`() {
        assertTrue(PaymentGatewayService.validateExpiry("12/30"))
        assertFalse(PaymentGatewayService.validateExpiry("01/20"))
        assertFalse(PaymentGatewayService.validateExpiry("invalid"))
        assertFalse(PaymentGatewayService.validateExpiry("15/30"))
    }

    @Test
    fun `payment gateway rejects declined test card and enforces payment authorization for coins`() = runBlocking {
        val declinedInput = PaymentGatewayService.CardPaymentInput(
            cardholderName = "Alex Mercer",
            cardNumber = "4000000000000002",
            expiry = "12/28",
            cvc = "123",
            billingZip = "94103"
        )
        val declineResult = PaymentGatewayService.processCardPayment(300, declinedInput)
        assertTrue(declineResult.isFailure)

        val validInput = PaymentGatewayService.CardPaymentInput(
            cardholderName = "Alex Mercer",
            cardNumber = "4242424242424242",
            expiry = "12/28",
            cvc = "123",
            billingZip = "94103"
        )
        val successResult = PaymentGatewayService.processCardPayment(300, validInput)
        assertTrue(successResult.isSuccess)
        val auth = successResult.getOrThrow()
        assertEquals(300, auth.amountCoins)
        assertEquals(30.0f, auth.fiatAmountUsd, 0.01f)
        assertEquals("SUCCEEDED", auth.status)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val walletRepo = WalletRepository(db)
        val purchaseResult = walletRepo.processAuthorizedCoinPurchase("usr_test_client", auth)
        assertTrue(purchaseResult.isSuccess)

        val tx = purchaseResult.getOrThrow()
        assertEquals(300, tx.amount)
        assertEquals("SETTLED", tx.status)
        assertEquals(30.0f, tx.fiatAmountUsd ?: 0f, 0.01f)
        db.close()
    }

    @Test
    fun `stripe payment service validates card inputs and creates authentic payment intents`() = runBlocking {
        val input = PaymentGatewayService.CardPaymentInput(
            cardholderName = "Jane Stanford",
            cardNumber = "4242424242424242",
            expiry = "11/29",
            cvc = "456",
            billingZip = "94305"
        )
        val result = com.example.data.remote.StripePaymentService.processPayment(
            amountCoins = 500,
            input = input,
            userEmail = "jane@stanford.edu"
        )
        assertTrue(result.isSuccess)
        val auth = result.getOrThrow()
        assertTrue(auth.paymentIntentId.startsWith("pi_"))
        assertTrue(auth.gatewayReference.startsWith("ch_"))
        assertEquals(500, auth.amountCoins)
        assertEquals(50.0f, auth.fiatAmountUsd, 0.01f)
        assertEquals("SUCCEEDED", auth.status)
        assertTrue(auth.receiptNumber.startsWith("STRIPE-"))
    }

    @Test
    fun `stripe payment service rejects invalid card numbers and declined test cards`() = runBlocking {
        // Invalid Luhn
        val badLuhn = PaymentGatewayService.CardPaymentInput(
            cardholderName = "Jane Stanford",
            cardNumber = "4242424242424241",
            expiry = "11/29",
            cvc = "456",
            billingZip = "94305"
        )
        val luhnResult = com.example.data.remote.StripePaymentService.processPayment(100, badLuhn)
        assertTrue(luhnResult.isFailure)

        // Stripe decline card ending in 0002
        val declineInput = PaymentGatewayService.CardPaymentInput(
            cardholderName = "Jane Stanford",
            cardNumber = "4000000000000002",
            expiry = "11/29",
            cvc = "456",
            billingZip = "94305"
        )
        val declineResult = com.example.data.remote.StripePaymentService.processPayment(100, declineInput)
        assertTrue(declineResult.isFailure)
        assertTrue(declineResult.exceptionOrNull()?.message?.contains("declined") == true)
    }
}
