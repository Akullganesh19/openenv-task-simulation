package com.example.data.remote

object StripePaymentService {
    data class StripeCheckoutResult(
        val success: Boolean,
        val sessionId: String? = null,
        val message: String = "Stripe checkout is not enabled in this local build."
    )

    fun createCheckoutSession(amountCoins: Int): StripeCheckoutResult {
        return StripeCheckoutResult(
            success = false,
            sessionId = null,
            message = "Stripe checkout is disabled in this local build."
        )
    }
}
