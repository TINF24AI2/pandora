package app.pandorapass.pandora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.pandorapass.pandora.logic.services.VaultService

class TestVaultViewModelFactory(
    private val vaultService: VaultService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestVaultViewModel::class.java)) {
            return TestVaultViewModel(vaultService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
