package com.example.data.remote

import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity

object FirestorePersistenceService {
    fun persistUser(user: UserEntity) {
        // Local-only build keeps persistence layered through Room and optional Firebase sync.
    }

    fun persistEscrowTransaction(transaction: TransactionEntity) {
        // Local-only build keeps ledger sync in Room; this is a no-op when Firestore is unavailable.
    }

    fun recordEscrowLock(
        gigId: String,
        clientId: String,
        amount: Int,
        description: String,
        txId: String
    ) {
        // Firestore ledger sync hook; intentionally left as a no-op in offline/local builds.
    }

    fun recordEscrowRelease(
        gigId: String,
        studentUserId: String,
        amount: Int,
        txId: String
    ) {
        // Firestore ledger sync hook; intentionally left as a no-op in offline/local builds.
    }
}
