package com.example.mybudget.ui.screens.wallets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.repository.TransactionRepository
import com.example.mybudget.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WalletDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    walletRepository: WalletRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    private val walletId: Long = checkNotNull(savedStateHandle["walletId"])

    val wallet = walletRepository.getWalletByIdAsFlow(walletId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val transactions = transactionRepository.getTransactionsByWallet(walletId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
