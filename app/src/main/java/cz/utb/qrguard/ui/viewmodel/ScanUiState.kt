package cz.utb.qrguard.ui.viewmodel

import cz.utb.qrguard.model.ScanResult

// Stav, který zobrazuje obrazovka Výsledek.
sealed interface ScanUiState {
    // Zatím nebyl načten žádný QR kód.
    data object Idle : ScanUiState

    // QR kód obsahoval webový odkaz a byl vyhodnocen.
    data class Success(val result: ScanResult) : ScanUiState

    // QR kód obsahoval něco jiného než webový odkaz (text, e-mail, Wi-Fi…).
    data class NotWebLink(val rawValue: String) : ScanUiState
}
