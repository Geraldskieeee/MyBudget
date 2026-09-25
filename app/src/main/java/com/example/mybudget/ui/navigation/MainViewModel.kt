package com.example.mybudget.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.repository.AuthRepository
import com.example.mybudget.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.mybudget.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    init {
        viewModelScope.launch {
            while (true) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    user.reload().addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            if (task.exception is FirebaseAuthInvalidUserException) {
                                // User account was deleted or disabled in the backend
                                FirebaseAuth.getInstance().signOut()
                            }
                        }
                    }
                }
                delay(10000) // Check every 10 seconds to respond quickly to deletions
            }
        }
    }
    
    val isUserLoggedIn: StateFlow<Boolean?> = authRepository.currentUser
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // null means we don't know yet (loading)
        )

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isAnimationsEnabled: StateFlow<Boolean> = settingsRepository.isAnimationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
        
    val showFloatingCalculator: StateFlow<Boolean> = settingsRepository.showFloatingCalculator
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
        

    val fontScale: StateFlow<Float> = settingsRepository.fontScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

}
