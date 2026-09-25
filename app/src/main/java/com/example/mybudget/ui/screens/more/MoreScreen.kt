package com.example.mybudget.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    onLogoutClick: () -> Unit,
    onManageCategoriesClick: () -> Unit = {},
    onSubmitSuggestionClick: () -> Unit = {},
    onAdminDashboardClick: () -> Unit = {}
) {
    val totalWealth by viewModel.totalWealth.collectAsState()
    val transactionCount by viewModel.transactionCount.collectAsState()
    val activeGoals by viewModel.activeGoalsCount.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isAnimationsEnabled by viewModel.isAnimationsEnabled.collectAsState()

    val showFloatingCalculator by viewModel.showFloatingCalculator.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val context = LocalContext.current
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var isUpdatingPassword by remember { mutableStateOf(false) }
    
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            viewModel.updateProfileImage(it, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF32D74B),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.clickable { launcher.launch("image/*") }) {
                        if (userPhotoUrl != null) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(viewModel.userDisplayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(viewModel.userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 2. Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(modifier = Modifier.weight(1f), title = "Total Wealth", value = "₱${String.format(java.util.Locale.US, "%,.0f", totalWealth)}")
                    StatCard(modifier = Modifier.weight(1f), title = "Transactions", value = transactionCount.toString())
                    StatCard(modifier = Modifier.weight(1f), title = "Active Goals", value = activeGoals.toString())
                }
            }

            // 3. Settings Menu
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Category,
                    title = "Manage Categories",
                    subtitle = "Add or edit expense categories",
                    onClick = onManageCategoriesClick
                )
                
                SettingsSwitchItem(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Toggle dark theme",
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                
                SettingsSwitchItem(
                    icon = Icons.Filled.Animation,
                    title = "Animations",
                    subtitle = "Enable or disable UI animations",
                    checked = isAnimationsEnabled,
                    onCheckedChange = { viewModel.setAnimationsEnabled(it) }
                )


                SettingsSwitchItem(
                    icon = Icons.Filled.Calculate,
                    title = "Floating Calculator",
                    subtitle = "Show floating calculator on all screens",
                    checked = showFloatingCalculator,
                    onCheckedChange = { viewModel.setShowFloatingCalculator(it) }
                )
                
                SettingsSliderItem(
                    icon = Icons.Filled.FormatSize,
                    title = "Text Adjust",
                    subtitle = "Adjust the app's text size",
                    value = fontScale,
                    onValueChange = { viewModel.setFontScale(it) },
                    valueRange = 0.8f..1.5f,
                    steps = 6
                )
                
                SettingsItem(
                    icon = Icons.Filled.FileDownload,
                    title = "Export Data",
                    subtitle = "Download your transactions as CSV",
                    onClick = {
                        viewModel.exportDataToCsv(context) { uri ->
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export Transactions"))
                            }
                        }
                    }
                )

                SettingsItem(
                    icon = Icons.Filled.Feedback,
                    title = "Submit Suggestion",
                    subtitle = "Help us improve the app",
                    onClick = onSubmitSuggestionClick
                )
                
                if (viewModel.userEmail.trim().equals(com.example.mybudget.util.AdminConfig.ADMIN_EMAIL.trim(), ignoreCase = true)) {
                    SettingsItem(
                        icon = Icons.Filled.AdminPanelSettings,
                        title = "Admin Dashboard",
                        subtitle = "View suggestions & analytics",
                        titleColor = MaterialTheme.colorScheme.primary,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = onAdminDashboardClick
                    )
                }

                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About & Credits",
                    subtitle = "App information and developer credits",
                    onClick = { showCreditsDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Filled.LockReset,
                    title = "Change Password",
                    subtitle = "Update your account password",
                    onClick = { showChangePasswordDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    titleColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = {
                        showLogoutDialog = true
                    }
                )
            }
        }
    }
    
    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            title = { Text("About MyBudget") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info, 
                        contentDescription = "App Info",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Version 5.5.0 (Alpha Test - Free)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Developed & Designed by",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mr.ManaGER",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreditsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLogoutClick()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isUpdatingPassword) {
                    showChangePasswordDialog = false 
                    newPassword = ""
                }
            },
            title = { Text("Change Password") },
            text = { 
                Column {
                    Text("Enter your new password below.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    if (isUpdatingPassword) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPassword.isNotBlank()) {
                            isUpdatingPassword = true
                            viewModel.updatePassword(newPassword) { success, errorMsg ->
                                isUpdatingPassword = false
                                if (success) {
                                    android.widget.Toast.makeText(context, "Password updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    showChangePasswordDialog = false
                                    newPassword = ""
                                } else {
                                    android.widget.Toast.makeText(context, "Error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isUpdatingPassword && newPassword.isNotBlank()
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showChangePasswordDialog = false
                        newPassword = ""
                    },
                    enabled = !isUpdatingPassword
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Unspecified,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsSliderItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(String.format(java.util.Locale.US, "%.1fx", value), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
