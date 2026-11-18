package cz.utb.qrguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.utb.qrguard.model.ScanResult
import cz.utb.qrguard.model.WarningType
import cz.utb.qrguard.ui.theme.QRGuardTheme
import cz.utb.qrguard.ui.viewmodel.ScanUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Obrazovka výsledku kontroly. Zobrazuje stav, který připraví ScanViewModel.
@Composable
fun ResultScreen(
    uiState: ScanUiState,
    onBack: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ResultHeader(onBack = onBack)

        when (uiState) {
            ScanUiState.Idle ->
                Text("Zatím nebyl naskenován žádný QR kód.")

            is ScanUiState.NotWebLink ->
                NotWebLinkContent(
                    rawValue = uiState.rawValue,
                    onScanAgain = onBack
                )

            is ScanUiState.Success ->
                ResultContent(
                    result = uiState.result,
                    onOpenInBrowser = onOpenInBrowser,
                    onSave = onSave
                )
        }
    }
}

// Srozumitelná zpráva pro QR kód, který neobsahuje webový odkaz.
@Composable
private fun NotWebLinkContent(
    rawValue: String,
    onScanAgain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "QR kód neobsahuje webový odkaz",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "Aplikace kontroluje pouze odkazy začínající http:// nebo https://.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (rawValue.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Obsah QR kódu",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = rawValue,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    Button(
        onClick = onScanAgain,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Skenovat znovu")
    }
}

@Composable
private fun ResultHeader(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Zpět"
            )
        }
        Text(
            text = "Výsledek kontroly",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun ResultContent(
    result: ScanResult,
    onOpenInBrowser: () -> Unit,
    onSave: () -> Unit
) {
    // Doména a celý odkaz
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Doména",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = result.domain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Celý odkaz",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = highlightDomain(
                url = result.fullUrl,
                domain = result.domain,
                highlightColor = MaterialTheme.colorScheme.primary
            ),
            style = MaterialTheme.typography.bodyLarge
        )
    }

    // Upozornění
    WarningsCard(warnings = result.warnings)

    Text(
        text = "Zkontrolováno: ${formatTimestamp(result.timestamp)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // Akce – odkaz se otevře až po stisknutí tlačítka
    Button(
        onClick = onOpenInBrowser,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Otevřít v prohlížeči")
    }
    OutlinedButton(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Save,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Uložit do historie")
    }
}

@Composable
private fun WarningsCard(warnings: List<WarningType>) {
    val hasWarnings = warnings.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (hasWarnings) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasWarnings) {
                Text(
                    text = "Nalezená upozornění (${warnings.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                warnings.forEach { warning ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(warning.label)
                    }
                }
            } else {
                // Aplikace nikdy netvrdí, že je odkaz bezpečný.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Bez zjištěných varovných znaků",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "To neznamená, že je odkaz bezpečný. Před otevřením ho zkontrolujte.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Zvýrazní doménu uvnitř celého odkazu (tučně a barvou).
private fun highlightDomain(
    url: String,
    domain: String,
    highlightColor: Color
): AnnotatedString {
    val start = url.indexOf(domain, ignoreCase = true)
    if (domain.isEmpty() || start < 0) return AnnotatedString(url)

    val end = start + domain.length
    return buildAnnotatedString {
        append(url.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
            append(url.substring(start, end))
        }
        append(url.substring(end))
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("d. M. yyyy HH:mm", Locale.forLanguageTag("cs-CZ"))
        .format(Date(timestamp))

// Ukázková data pro náhledy v Android Studiu
private val previewWithWarnings = ScanResult(
    fullUrl = "http://bit.ly/3xYzAbc?utm_source=qr",
    domain = "bit.ly",
    warnings = listOf(WarningType.INSECURE_HTTP, WarningType.URL_SHORTENER)
)

private val previewWithoutWarnings = ScanResult(
    fullUrl = "https://www.utb.cz/studenti/rozvrh",
    domain = "www.utb.cz",
    warnings = emptyList()
)

@Preview(showBackground = true, name = "S upozorněními")
@Composable
private fun ResultScreenWarningsPreview() {
    QRGuardTheme {
        ResultScreen(
            uiState = ScanUiState.Success(previewWithWarnings),
            onBack = {},
            onOpenInBrowser = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Bez upozornění")
@Composable
private fun ResultScreenNoWarningsPreview() {
    QRGuardTheme {
        ResultScreen(
            uiState = ScanUiState.Success(previewWithoutWarnings),
            onBack = {},
            onOpenInBrowser = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Prázdný stav")
@Composable
private fun ResultScreenEmptyPreview() {
    QRGuardTheme {
        ResultScreen(
            uiState = ScanUiState.Idle,
            onBack = {},
            onOpenInBrowser = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Není webový odkaz")
@Composable
private fun ResultScreenNotWebLinkPreview() {
    QRGuardTheme {
        ResultScreen(
            uiState = ScanUiState.NotWebLink("WIFI:S:MojeSit;T:WPA;P:tajneheslo;;"),
            onBack = {},
            onOpenInBrowser = {},
            onSave = {}
        )
    }
}

