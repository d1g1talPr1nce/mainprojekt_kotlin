package cz.utb.qrguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.utb.qrguard.ui.navigation.QRGuardApp
import cz.utb.qrguard.ui.theme.QRGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRGuardTheme {
                QRGuardApp()
            }
        }
    }
}
