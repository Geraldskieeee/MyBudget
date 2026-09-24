package com.example.mybudget.ui.navigation

import androidx.compose.foundation.layout.padding
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mybudget.ui.components.BottomNavBar
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

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isUserLoggedIn == true) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onAddBillClick = { navController.navigate(Screen.AddBill.route) },
                    onAddGoalClick = { navController.navigate(Screen.AddGoal.route) },
                    onAddDebtClick = { navController.navigate(Screen.AddDebt.route) },
                    onDeleteBill = { bill -> viewModel.deleteBill(bill) },
                    onDeleteGoal = { goal -> viewModel.deleteGoal(goal) },
                    onDeleteDebt = { debt -> viewModel.deleteDebt(debt) },
                    onSettleDebt = { debt, walletId -> viewModel.settleDebt(debt, walletId) }
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
                    }
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
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onNavigateBack = { navController.popBackStack() }
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
    }
}
