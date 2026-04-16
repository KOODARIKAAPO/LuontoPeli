package com.example.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authManager: AuthManager = AuthManager(),
    private val firestoreManager: FirestoreManager = FirestoreManager()
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

        // 🔒 Jos ei käyttäjää → ei haeta mitään
        if (user == null) {
            _totalSpots.value = 0
            return
        }

        val userId = user.uid

        viewModelScope.launch {
            firestoreManager.getUserSpots(userId)
                .map { spots -> spots.size }
                .catch { emit(0) }
                .collect {
                    _totalSpots.value = it
                }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            authManager.signInAnonymously()
        }
    }

    fun signOut() {
        authManager.signOut()
    }
}