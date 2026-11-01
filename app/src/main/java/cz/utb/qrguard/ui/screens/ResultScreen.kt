package cz.utb.qrguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.utb.qrguard.ui.theme.QRGuardTheme

// Obrazovka výsledku – zatím placeholder, obsah (odkaz, doména,
// upozornění a tlačítka) doplníme po napojení na ViewModel.
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Výsledek kontroly",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Zatím nebyl naskenován žádný QR kód.")
        OutlinedButton(onClick = onBack) {
            Text("Zpět")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    QRGuardTheme {
        ResultScreen(onBack = {})
    }
}
