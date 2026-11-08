package cz.utb.qrguard.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.utb.qrguard.CameraPermissionContent

// Obrazovka skeneru – řeší oprávnění, náhled kamery a čtení QR kódu (ML Kit).
// Načtený text zatím jen předáváme dál; zpracování odkazu a předání
// do výsledku přes ViewModel přijde v dalším kroku.
@Composable
fun ScannerScreen(
    onScanned: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        CameraPermissionContent(
            onQrCodeScanned = { rawValue ->
                Log.d("ScannerScreen", "Načten QR kód: $rawValue")
                onScanned()
            }
        )
    }
}