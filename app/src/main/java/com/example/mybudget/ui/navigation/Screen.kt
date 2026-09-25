package com.example.mybudget.ui.navigation

sealed class Screen(val route: String, val title: String, val icon: String? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Home : Screen("home", "Home")
    object Transactions : Screen("transactions", "Transactions")
    object Budget : Screen("budget", "Budget")
    object Analytics : Screen("analytics", "Analytics")
    object More : Screen("more", "More")
    object AddWallet : Screen("add_wallet", "Add Wallet")
    object AddTransaction : Screen("add_transaction?type={type}", "Add Transaction") {
        fun createRoute(type: String? = null): String {
            return if (type != null) "add_transaction?type=$type" else "add_transaction"
        }
    }
    object ManageCategories : Screen("manage_categories", "Manage Categories")
    object AddBill : Screen("add_bill", "Add Bill")
    object AddGoal : Screen("add_goal", "Add Goal")
    object AddDebt : Screen("add_debt", "Add Debt")
    object SubmitSuggestion : Screen("submit_suggestion", "Submit Suggestion")
    object AdminDashboard : Screen("admin_dashboard", "Admin Dashboard")
    
    object WalletDetails : Screen("wallet_details/{walletId}", "Wallet Details") {
        fun createRoute(walletId: Long) = "wallet_details/$walletId"
    }
}
