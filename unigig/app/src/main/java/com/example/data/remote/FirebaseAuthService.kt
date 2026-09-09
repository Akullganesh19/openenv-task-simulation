package com.example.data.remote

import android.content.Context
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthService {
    fun launchGoogleSignIn(context: Context): Result<String> {
        return Result.failure(
            UnsupportedOperationException(
                "Google Sign-In is not configured in this local build. Add Firebase credentials to enable SSO."
            )
        )
    }

    fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> {
        return Result.failure(
            UnsupportedOperationException(
                "Firebase Google ID token sign-in is not available in this local build."
            )
        )
    }
}
