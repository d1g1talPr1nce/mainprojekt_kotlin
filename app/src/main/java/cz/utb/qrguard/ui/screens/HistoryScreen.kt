package cz.utb.qrguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.utb.qrguard.ui.theme.QRGuardTheme

// Obrazovka historie – zatím placeholder, seznam uložených kontrol
// doplníme po napojení na Room databázi přes ViewModel.
@Composable
fun HistoryScreen(
    onOpenResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Historie kontrol",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Historie je zatím prázdná.")
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    QRGuardTheme {
        HistoryScreen(onOpenResult = {})
    }
}
