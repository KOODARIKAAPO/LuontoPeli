package com.example.luontopeli.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _totalSpots = MutableStateFlow(0)
    val totalSpots: StateFlow<Int> = _totalSpots

    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
            observeUserSpots()
        }
        observeUserSpots()
    }

    private fun observeUserSpots() {
        val user = auth.currentUser

        if (user == null) {
            _totalSpots.value = 0
            return
        }

        val userId = user.uid

        viewModelScope.launch {
            firestoreManager.getUserSpots(userId)
                .map { it.size }
                .catch {
                    Log.e("PROFILE_TEST", "Error fetching spots", it)
                    emit(0)
                }
                .collect { count ->
                    _totalSpots.value = count
                }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            Log.e("PROFILE_TEST", "ViewModel: signInAnonymously called")

            try {
                val uid = authManager.signInAnonymouslyIfNeeded()
                Log.e("PROFILE_TEST", "ViewModel: SUCCESS uid=$uid")
            } catch (e: Exception) {
                Log.e("PROFILE_TEST", "ViewModel: FAILED", e)
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        Log.e("PROFILE_TEST", "User signed out")
    }
}