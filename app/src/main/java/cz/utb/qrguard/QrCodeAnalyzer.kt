package cz.utb.qrguard

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

// Analyzátor snímků z CameraX – každý snímek předá ML Kitu a hledá v něm QR kód.
// Výsledek ohlásí jen jednou, aby se po nalezení kódu neotevíralo více obrazovek.
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Hledáme pouze QR kódy – ostatní formáty (EAN, Code128…) zbytečně zpomalují detekci.
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    private val alreadyScanned = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || alreadyScanned.get()) {
            imageProxy.close()
            return
        }

        // Rotace je nutná – senzor je v telefonu otočený a ML Kit by jinak dostal obraz na boku.
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }
                if (rawValue != null && alreadyScanned.compareAndSet(false, true)) {
                    onQrCodeScanned(rawValue)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("QrCodeAnalyzer", "Analýza snímku selhala", exception)
            }
            .addOnCompleteListener {
                // Bez uzavření by CameraX neposlala další snímek a analýza by se zastavila.
                imageProxy.close()
            }
    }

    fun close() {
        scanner.close()
    }
}