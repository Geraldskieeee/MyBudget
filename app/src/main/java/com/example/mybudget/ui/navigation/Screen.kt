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
    object AddTransaction : Screen("add_transaction", "Add Transaction")
    object ManageCategories : Screen("manage_categories", "Manage Categories")
    object AddBill : Screen("add_bill", "Add Bill")
    object AddGoal : Screen("add_goal", "Add Goal")
    object AddDebt : Screen("add_debt", "Add Debt")
    
    object WalletDetails : Screen("wallet_details/{walletId}", "Wallet Details") {
        fun createRoute(walletId: Long) = "wallet_details/$walletId"
    }
}
