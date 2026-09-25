package com.example.mybudget.ui.screens.more

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SubmitSuggestionViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    var isSubmitting by mutableStateOf(false)
        private set

    fun submitSuggestion(content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onError("You must be logged in to submit a suggestion.")
            return
        }
        if (content.isBlank()) {
            onError("Suggestion cannot be empty.")
            return
        }
        
        isSubmitting = true
        viewModelScope.launch {
            try {
                val suggestion = hashMapOf(
                    "userId" to user.uid,
                    "userEmail" to (user.email ?: "Unknown"),
                    "content" to content,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("suggestions").add(suggestion).await()
                isSubmitting = false
                onSuccess()
            } catch (e: Exception) {
                isSubmitting = false
                onError(e.message ?: "Failed to submit suggestion.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitSuggestionScreen(
    onBackClick: () -> Unit,
    viewModel: SubmitSuggestionViewModel = viewModel()
) {
    var suggestionText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Suggestion") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "We value your feedback!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please describe your suggestion, feature request, or issue below. Your feedback helps us improve the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = suggestionText,
                onValueChange = { suggestionText = it },
                label = { Text("Your Suggestion") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                singleLine = false,
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.submitSuggestion(
                        content = suggestionText,
                        onSuccess = {
                            Toast.makeText(context, "Suggestion submitted successfully!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !viewModel.isSubmitting && suggestionText.isNotBlank()
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit")
                }
            }
        }
    }
}
