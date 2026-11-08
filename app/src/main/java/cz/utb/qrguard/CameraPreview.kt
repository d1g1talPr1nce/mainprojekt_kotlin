package cz.utb.qrguard

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

// Tuto komponentu zobrazujeme pouze po udělení oprávnění kamery.
// Kromě náhledu běží i analýza snímků, která hledá QR kód (ML Kit).
@SuppressLint("MissingPermission")
@Composable
fun CameraPreview(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Vždy aktuální callback, i když se komponenta překreslí a DisposableEffect se nerestartuje.
    val currentOnQrCodeScanned by rememberUpdatedState(onQrCodeScanned)

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context, lifecycleOwner, previewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val preview = Preview.Builder().build()

        // Analýza běží na vlastním vlákně, aby nezdržovala hlavní (UI) vlákno.
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val qrCodeAnalyzer = QrCodeAnalyzer { value -> currentOnQrCodeScanned(value) }
        // KEEP_ONLY_LATEST: když analýza nestíhá, starší snímky se zahodí místo hromadění ve frontě.
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(analysisExecutor, qrCodeAnalyzer) }

        var cameraProvider: ProcessCameraProvider? = null
        var disposed = false

        providerFuture.addListener(
            {
                if (!disposed) {
                    try {
                        val provider = providerFuture.get()
                        cameraProvider = provider

                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (exception: Exception) {
                        Log.e("CameraPreview", "Nelze spustit kameru", exception)
                        errorMessage = "Kameru se nepodařilo spustit."
                    }
                }
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            disposed = true
            cameraProvider?.unbind(preview, imageAnalysis)
            imageAnalysis.clearAnalyzer()
            qrCodeAnalyzer.close()
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        errorMessage?.let { message ->
            Text(message)
        }
    }
}