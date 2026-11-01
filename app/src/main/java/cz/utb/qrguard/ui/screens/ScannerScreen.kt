package cz.utb.qrguard.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.utb.qrguard.CameraPermissionContent

// Obrazovka skeneru – zatím jen obaluje řešení oprávnění a náhled kamery.
// Po napojení ML Kit se odsud bude přecházet na výsledek přes onScanned.
@Composable
fun ScannerScreen(
    onScanned: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        CameraPermissionContent()
    }
}
