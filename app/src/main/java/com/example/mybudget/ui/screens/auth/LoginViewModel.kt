package com.example.mybudget.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import android.app.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow<SignInState>(SignInState.Initial)
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    fun signInWithGoogle() {
        // Dummy implementation until we get Web Client ID
        _signInState.value = SignInState.Error("Google Sign-In requires Web Client ID setup.")
    }

    fun signInWithGitHub(activity: Activity) {
        _signInState.value = SignInState.Loading
        val provider = OAuthProvider.newBuilder("github.com")
        
        FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener {
                _signInState.value = SignInState.Success
            }
            .addOnFailureListener { e ->
                _signInState.value = SignInState.Error(e.message ?: "GitHub Sign-In failed")
            }
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _signInState.value = SignInState.Error("Email and password cannot be empty")
            return
        }
        _signInState.value = SignInState.Loading
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signInState.value = SignInState.Success
                } else {
                    _signInState.value = SignInState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun createAccountWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _signInState.value = SignInState.Error("Email and password cannot be empty")
            return
        }
        if (pass.length < 6) {
            _signInState.value = SignInState.Error("Password must be at least 6 characters")
            return
        }
        _signInState.value = SignInState.Loading
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signInState.value = SignInState.Success
                } else {
                    _signInState.value = SignInState.Error(task.exception?.message ?: "Account creation failed")
                }
            }
    }

    fun signInWithFirebase(idToken: String) {
        _signInState.value = SignInState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                // We don't have a suspend version of signInWithCredential easily available without play-services-auth coroutines library.
                // For simplicity, we can just use the Task API directly from AuthRepository or here.
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _signInState.value = SignInState.Success
                        } else {
                            _signInState.value = SignInState.Error(task.exception?.message ?: "Sign-in failed")
                        }
                    }
            } catch (e: Exception) {
                _signInState.value = SignInState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email address.")
            return
        }
        authRepository.sendPasswordResetEmail(email) { success, error ->
            onResult(success, error)
        }
    }
}

sealed class SignInState {
    object Initial : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    data class Error(val message: String) : SignInState()
}
