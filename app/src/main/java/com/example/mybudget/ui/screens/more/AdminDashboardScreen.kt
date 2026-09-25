package com.example.mybudget.ui.screens.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Suggestion(
    val id: String = "",
    val userEmail: String = "",
    val content: String = "",
    val timestamp: Long = 0
)

data class UserTracker(
    val id: String = "",
    val email: String = "",
    val lastActive: Long = 0
)

class AdminDashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    var suggestions by mutableStateOf<List<Suggestion>>(emptyList())
        private set
        
    var activeUsers by mutableStateOf<List<UserTracker>>(emptyList())
        private set
        
    var websiteDownloads by mutableStateOf(0L)
        private set
        
    var isLoading by mutableStateOf(true)
        private set

    init {
        fetchSuggestions()
        fetchActiveUsers()
        fetchWebsiteDownloads()
    }

    private fun fetchWebsiteDownloads() {
        db.collection("analytics").document("downloads")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    websiteDownloads = snapshot.getLong("count") ?: 0L
                }
            }
    }

    private fun fetchSuggestions() {
        isLoading = true
        db.collection("suggestions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        Suggestion(
                            id = doc.id,
                            userEmail = doc.getString("userEmail") ?: "Unknown",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    suggestions = list
                    isLoading = false
                }
            }
    }
    
    private fun fetchActiveUsers() {
        db.collection("users")
            .orderBy("lastActive", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        UserTracker(
                            id = doc.id,
                            email = doc.getString("email") ?: "Unknown",
                            lastActive = doc.getLong("lastActive") ?: 0L
                        )
                    }
                    activeUsers = list
                }
            }
    }

    fun deleteSuggestion(id: String) {
        viewModelScope.launch {
            try {
                db.collection("suggestions").document(id).delete().await()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun deleteUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).delete().await()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Suggestions", "Active Users")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${viewModel.suggestions.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Total Suggestions", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${viewModel.activeUsers.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Active Users", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${viewModel.websiteDownloads}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Web Downloads", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            if (viewModel.isLoading && selectedTabIndex == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (selectedTabIndex == 0) {
                if (viewModel.suggestions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                        Text("No suggestions received yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy((-16).dp)
                    ) {
                        items(viewModel.suggestions, key = { it.id }) { suggestion ->
                            SuggestionItem(
                                suggestion = suggestion,
                                onDelete = { viewModel.deleteSuggestion(suggestion.id) }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 1) {
                if (viewModel.activeUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                        Text("No active users recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy((-16).dp)
                    ) {
                        items(viewModel.activeUsers, key = { it.id }) { user ->
                            val itemContext = androidx.compose.ui.platform.LocalContext.current
                            ActiveUserItem(
                                user = user,
                                onSendPasswordReset = {
                                    viewModel.sendPasswordResetEmail(user.email) { success, msg ->
                                        if (success) {
                                            android.widget.Toast.makeText(itemContext, "Password reset email sent to ${user.email}", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(itemContext, "Error: $msg", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                onDeleteProfile = {
                                    viewModel.deleteUserProfile(user.id)
                                    android.widget.Toast.makeText(itemContext, "Profile data deleted for ${user.email}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(suggestion: Suggestion, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = if (suggestion.timestamp > 0) dateFormat.format(Date(suggestion.timestamp)) else "Unknown Date"

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion.userEmail, 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(dateString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(suggestion.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun ActiveUserItem(
    user: UserTracker,
    onSendPasswordReset: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = if (user.lastActive > 0) dateFormat.format(Date(user.lastActive)) else "Unknown Date"

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.email, 
                            style = MaterialTheme.typography.bodyLarge, 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text("User ID: ${user.id.take(8)}...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSendPasswordReset) {
                    Text("Reset Password", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = onDeleteProfile,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Profile", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
