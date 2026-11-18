package cz.utb.qrguard.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.utb.qrguard.CameraPermissionContent

// Obrazovka skeneru – řeší oprávnění, náhled kamery a čtení QR kódu (ML Kit).
// Načtený text předá přes onScanned, vyhodnocení odkazu dělá ScanViewModel.
@Composable
fun ScannerScreen(
    onScanned: (rawValue: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        CameraPermissionContent(
            onQrCodeScanned = onScanned
        )
    }
}
