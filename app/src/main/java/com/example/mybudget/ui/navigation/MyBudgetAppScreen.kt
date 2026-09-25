package com.example.mybudget.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mybudget.ui.components.BottomNavBar
import com.example.mybudget.ui.components.FloatingCalculator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.mybudget.ui.screens.auth.LoginScreen
import com.example.mybudget.ui.screens.auth.RegisterScreen
import com.example.mybudget.ui.screens.auth.LoginViewModel
import com.example.mybudget.ui.screens.budget.BudgetScreen
import com.example.mybudget.ui.screens.home.AddBillScreen
import com.example.mybudget.ui.screens.home.AddGoalScreen
import com.example.mybudget.ui.screens.home.AddDebtScreen
import com.example.mybudget.ui.screens.home.HomeScreen
import com.example.mybudget.ui.screens.home.HomeViewModel
import com.example.mybudget.ui.screens.more.MoreScreen
import com.example.mybudget.ui.screens.categories.ManageCategoriesScreen
import com.example.mybudget.ui.screens.categories.CategoryViewModel
import com.example.mybudget.ui.screens.budget.BudgetScreen
import com.example.mybudget.ui.screens.budget.BudgetViewModel
import com.example.mybudget.ui.screens.transactions.AddTransactionScreen
import com.example.mybudget.ui.screens.transactions.TransactionsScreen
import com.example.mybudget.ui.screens.wallets.AddWalletScreen
import com.example.mybudget.ui.screens.wallets.WalletDetailsScreen
import com.example.mybudget.ui.screens.wallets.WalletDetailsViewModel
import com.example.mybudget.ui.screens.wallets.AnalyticsScreen
import com.example.mybudget.ui.screens.wallets.WalletViewModel

@Composable
fun MyBudgetAppScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isUserLoggedIn by mainViewModel.isUserLoggedIn.collectAsState()
    val isAnimationsEnabled by mainViewModel.isAnimationsEnabled.collectAsState()

    val showFloatingCalculator by mainViewModel.showFloatingCalculator.collectAsState()
    val fontScale by mainViewModel.fontScale.collectAsState()
    val context = LocalContext.current
    
    var showExitDialog by remember { mutableStateOf(false) }



    androidx.compose.runtime.LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == true) {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                try {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .set(
                            mapOf(
                                "email" to (user.email ?: "Unknown"),
                                "lastActive" to System.currentTimeMillis()
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                } catch (e: Exception) {
                    // Ignore, we don't want to crash the app if analytics fail
                }
            }
        }
    }

    // Loading state
    if (isUserLoggedIn == null) {
        return
    }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Transactions.route,
        Screen.Analytics.route,
        Screen.Budget.route,
        Screen.More.route
    ) && isUserLoggedIn == true

    val currentDensity = androidx.compose.ui.platform.LocalDensity.current
    val customDensity = androidx.compose.ui.unit.Density(
        density = currentDensity.density,
        fontScale = fontScale
    )

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides customDensity
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        navController = navController,
                        isAnimationsEnabled = isAnimationsEnabled
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = if (isUserLoggedIn == true) Screen.Home.route else Screen.Login.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { if (isAnimationsEnabled) fadeIn(animationSpec = tween(300)) else EnterTransition.None },
                exitTransition = { if (isAnimationsEnabled) fadeOut(animationSpec = tween(300)) else ExitTransition.None },
                popEnterTransition = { if (isAnimationsEnabled) fadeIn(animationSpec = tween(300)) else EnterTransition.None },
                popExitTransition = { if (isAnimationsEnabled) fadeOut(animationSpec = tween(300)) else ExitTransition.None }
            ) {
            composable(Screen.Login.route) {
                val viewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    viewModel = viewModel
                )
            }
            composable(Screen.Register.route) {
                val viewModel: LoginViewModel = hiltViewModel()
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }
            composable(Screen.Home.route) {
                BackHandler(enabled = true) {
                    showExitDialog = true
                }
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onAddBillClick = { navController.navigate(Screen.AddBill.route) },
                    onAddGoalClick = { navController.navigate(Screen.AddGoal.route) },
                    onAddDebtClick = { navController.navigate(Screen.AddDebt.route) },
                    onDeleteBill = { bill -> viewModel.deleteBill(bill) },
                    onDeleteGoal = { goal -> viewModel.deleteGoal(goal) },
                    onDeleteDebt = { debt -> viewModel.deleteDebt(debt) },
                    onSettleDebt = { debt, walletId -> viewModel.settleDebt(debt, walletId) },
                    onWalletClick = { wallet -> navController.navigate(Screen.WalletDetails.createRoute(wallet.id)) },
                    onAddWalletClick = { navController.navigate(Screen.AddWallet.route) },
                    onDeleteWallet = { wallet -> viewModel.deleteWallet(wallet) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                )
            }
            composable(Screen.Budget.route) {
                val viewModel: BudgetViewModel = hiltViewModel()
                BudgetScreen(viewModel = viewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(navController = navController)
            }
            composable(Screen.More.route) {
                val viewModel: com.example.mybudget.ui.screens.more.MoreViewModel = hiltViewModel()
                MoreScreen(
                    viewModel = viewModel,
                    onLogoutClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onManageCategoriesClick = {
                        navController.navigate(Screen.ManageCategories.route)
                    },
                    onSubmitSuggestionClick = {
                        navController.navigate(Screen.SubmitSuggestion.route)
                    },
                    onAdminDashboardClick = {
                        navController.navigate(Screen.AdminDashboard.route)
                    }
                )
            }
            composable(Screen.SubmitSuggestion.route) {
                com.example.mybudget.ui.screens.more.SubmitSuggestionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminDashboard.route) {
                com.example.mybudget.ui.screens.more.AdminDashboardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.ManageCategories.route) {
                val viewModel: CategoryViewModel = hiltViewModel()
                ManageCategoriesScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.AddWallet.route) {
                val viewModel: WalletViewModel = hiltViewModel()
                AddWalletScreen(navController = navController, viewModel = viewModel)
            }
            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(navArgument("type") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true 
                })
            ) { backStackEntry ->
                val transactionType = backStackEntry.arguments?.getString("type")
                AddTransactionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    initialType = transactionType
                )
            }
            composable(Screen.AddBill.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                AddBillScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddGoal.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                AddGoalScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddDebt.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                AddDebtScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.WalletDetails.route,
                arguments = listOf(navArgument("walletId") { type = NavType.LongType })
            ) {
                val viewModel: WalletDetailsViewModel = hiltViewModel()
                WalletDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
        }
        
        if (isUserLoggedIn == true && showFloatingCalculator) {
            FloatingCalculator()
        }
        
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit the application?") },
                confirmButton = {
                    TextButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                        Text("Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
}
}
