package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DisputeEntity
import com.example.data.local.GigEntity
import com.example.data.model.LedgerBalance
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.DisputeRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.GigRepository
import com.example.data.repository.SkillRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userRepository = UserRepository(database)
    private val gigRepository = GigRepository(database, userRepository)
    private val walletRepository = WalletRepository(database)
    private val disputeRepository = DisputeRepository(database, userRepository)
    private val skillRepository = SkillRepository(database)
    private val geminiRepository = GeminiRepository(database)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUserName = MutableStateFlow("Guest")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserSession = MutableStateFlow<UserSession?>(null)
    val currentUserSession: StateFlow<UserSession?> = _currentUserSession.asStateFlow()

    private val _selectedGig = MutableStateFlow<GigEntity?>(null)
    val selectedGig: StateFlow<GigEntity?> = _selectedGig.asStateFlow()

    private val _ledgerBalance = MutableStateFlow(LedgerBalance(0, 0, 0))
    val ledgerBalance: StateFlow<LedgerBalance> = _ledgerBalance.asStateFlow()

    private val _highThinkingResult = MutableStateFlow<String?>(null)
    val highThinkingResult: StateFlow<String?> = _highThinkingResult.asStateFlow()

    val allUsers = userRepository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val allGigs = gigRepository.allGigs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val allTransactions = walletRepository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val allDisputes = disputeRepository.allDisputes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val allBadges = skillRepository.allBadges.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun selectGig(gig: GigEntity?) {
        _selectedGig.value = gig
        if (gig != null) {
            _selectedTab.value = 1
        }
    }

    private fun storeSession(session: UserSession) {
        _currentUserSession.value = session
        _currentRole.value = session.role
        _currentUserName.value = session.anonymizedHandle
        _currentUserId.value = session.userId
        _isLoggedIn.value = true
        refreshLedgerForCurrentUser()
    }

    fun loginWithKeycloak(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.authenticateWithKeycloak(email, password)
            if (result.isSuccess) {
                val session = result.getOrThrow()
                storeSession(session)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "Authentication failed.")
            }
        }
    }

    fun loginWithFirebaseUser(email: String, displayName: String?, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.authenticateWithFirebaseUser(email, displayName)
            if (result.isSuccess) {
                val session = result.getOrThrow()
                storeSession(session)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "Google sign-in failed.")
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserSession.value = null
        _currentUserId.value = null
        _currentRole.value = UserRole.STUDENT
        _currentUserName.value = "Guest"
        _selectedTab.value = 0
        _selectedGig.value = null
        _ledgerBalance.value = LedgerBalance(0, 0, 0)
    }

    fun refreshLedgerForCurrentUser() {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            walletRepository.getLedgerBalance(userId).collect { balance ->
                _ledgerBalance.value = balance
            }
        }
    }

    fun reviewAndCertifyKyc(targetUserId: String, approve: Boolean, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.reviewAndCertifyKyc(_currentRole.value, targetUserId, approve)
            if (result.isSuccess) {
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "KYC review failed.")
            }
        }
    }

    fun issueDisciplinaryStrike(userId: String, reason: String, callback: (Int, Boolean) -> Unit) {
        viewModelScope.launch {
            val strikes = userRepository.addStrike(userId, reason)
            val banned = strikes >= 3
            callback(strikes, banned)
        }
    }

    fun escalateDisputeToCeo(dispute: DisputeEntity, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = disputeRepository.escalateToCeo(dispute)
            if (result.isSuccess) {
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "Escalation failed.")
            }
        }
    }

    fun runHighThinkingMediation(prompt: String, gigContext: String? = null) {
        viewModelScope.launch {
            val result = geminiRepository.analyzeWithHighThinking(prompt, gigContext)
            if (result.isSuccess) {
                _highThinkingResult.value = result.getOrThrow().second
            } else {
                _highThinkingResult.value = result.exceptionOrNull()?.message ?: "AI mediation unavailable."
            }
        }
    }

    fun resolveDispute(
        dispute: DisputeEntity,
        verdictText: String,
        penalizeRespondent: Boolean,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = disputeRepository.resolveDispute(
                dispute = dispute,
                verdictText = verdictText,
                callerRole = _currentRole.value,
                penalizeRespondent = penalizeRespondent
            )
            if (result.isSuccess) {
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "Dispute resolution failed.")
            }
        }
    }
}
