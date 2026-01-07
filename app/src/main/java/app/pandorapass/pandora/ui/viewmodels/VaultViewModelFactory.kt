package app.pandorapass.pandora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.pandorapass.pandora.logic.services.VaultService

class VaultViewModelFactory(
    private val vaultService: VaultService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            return VaultViewModel(vaultService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
