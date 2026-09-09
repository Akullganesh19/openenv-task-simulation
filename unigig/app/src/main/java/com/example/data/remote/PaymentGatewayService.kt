package com.example.data.remote

import java.util.UUID

object PaymentGatewayService {
    data class PaymentAuthorization(
        val status: String,
        val amountCoins: Int,
        val fiatAmountUsd: Float,
        val gatewayReference: String = "PG-${UUID.randomUUID()}",
        val paymentMethodLabel: String = "Visa •••• 4242",
        val instrumentSummary: String = "Card ending in 4242",
        val timestamp: Long = System.currentTimeMillis()
    )

    fun authorizeCoinPurchase(
        amountCoins: Int,
        paymentMethodLabel: String = "Visa •••• 4242",
        instrumentSummary: String = "Card ending in 4242"
    ): PaymentAuthorization {
        val safeAmount = amountCoins.coerceIn(50, 10000)
        return PaymentAuthorization(
            status = "SUCCEEDED",
            amountCoins = safeAmount,
            fiatAmountUsd = safeAmount * 0.10f,
            gatewayReference = "PG-${UUID.randomUUID().toString().take(10).uppercase()}",
            paymentMethodLabel = paymentMethodLabel,
            instrumentSummary = instrumentSummary
        )
    }
}
