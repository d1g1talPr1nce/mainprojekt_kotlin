package cz.utb.qrguard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPermissionContent(modifier: Modifier = Modifier) {
    val activity = checkNotNull(LocalActivity.current)
    val lifecycleOwner = LocalLifecycleOwner.current

    val preferences = remember(activity) {
        activity.getSharedPreferences(
            "camera_permission",
            Context.MODE_PRIVATE
        )
    }

    var permissionGranted by remember { mutableStateOf(false) }
    var permissionBlocked by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    fun refreshPermission() {
        permissionGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        permissionRequested = preferences.getBoolean("requested", false)

        permissionBlocked =
            !permissionGranted &&
                    permissionRequested &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.CAMERA
                    )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshPermission()
    }

    DisposableEffect(activity, lifecycleOwner) {
        refreshPermission()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermission()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            when {
                permissionGranted ->
                    "Přístup ke kameře je povolen."

                permissionBlocked ->
                    "Oprávnění kamery můžete povolit v nastavení aplikace."

                permissionRequested ->
                    "Přístup byl odmítnut. Pro skenování QR kódů potřebujeme kameru."

                else ->
                    "Pro skenování QR kódů potřebujeme přístup ke kameře."
            }
        )

        if (!permissionGranted) {
            Button(
                onClick = {
                    if (permissionBlocked) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                activity.packageName,
                                null
                            )
                        )
                        activity.startActivity(intent)
                    } else {
                        preferences.edit()
                            .putBoolean("requested", true)
                            .apply()

                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                }
            ) {
                Text(
                    if (permissionBlocked) {
                        "Otevřít nastavení"
                    } else {
                        "Povolit kameru"
                    }
                )
            }
        }
    }
}