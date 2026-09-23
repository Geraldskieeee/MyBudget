package com.example.mybudget.ui.screens.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.local.entity.Wallet
import com.example.mybudget.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    val wallets: StateFlow<List<Wallet>> = walletRepository.getAllWallets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWallet(name: String, startingBalance: Double, iconId: Int? = null) {
        viewModelScope.launch {
            walletRepository.addWallet(name, startingBalance, iconId)
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            walletRepository.deleteWallet(wallet)
        }
    }
}
