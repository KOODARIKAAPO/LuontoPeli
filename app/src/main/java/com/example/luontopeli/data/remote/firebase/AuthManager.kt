package com.example.luontopeli.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signInAnonymouslyIfNeeded(): String {
        auth.currentUser?.uid?.let {
            Log.d("AuthManager", "User already signed in: $it")
            return it
        }

        return try {
            Log.d("AuthManager", "Starting anonymous sign-in")
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid
                ?: throw IllegalStateException("Anonymous sign-in succeeded but user uid was null")

            Log.d("AuthManager", "Anonymous sign-in success: $uid")
            uid
        } catch (e: Exception) {
            Log.e("AuthManager", "Anonymous sign-in failed", e)
            throw e
        }
    }

    fun signOut() {
        auth.signOut()
        Log.d("AuthManager", "Signed out")
    }
}