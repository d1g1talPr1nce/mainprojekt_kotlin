package cz.utb.qrguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import cz.utb.qrguard.model.UrlAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Sdílený ViewModel pro Skener a Výsledek. Obrazovky z něj jen čtou stav
// a hlásí události – samy nevyhodnocují odkaz ani nepracují s daty.
class ScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Zavolá Skener po načtení QR kódu.
    fun onQrCodeScanned(rawValue: String) {
        val result = UrlAnalyzer.analyze(rawValue)
        _uiState.value = if (result != null) {
            ScanUiState.Success(result)
        } else {
            ScanUiState.NotWebLink(rawValue)
        }
    }
}
