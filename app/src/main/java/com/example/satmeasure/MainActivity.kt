package com.example.satmeasure

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.satmeasure.ui.navigation.SatMesNavApp
import com.example.satmeasure.ui.theme.SatMeasureTheme
import com.mapbox.common.MapboxOptions

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.satmeasure.data.SettingsManager
import com.example.satmeasure.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : AppCompatActivity() {
    private val deepLinkParams = MutableStateFlow<Pair<String?, String?>>(null to null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.satmeasure.utils.HapticHelper.init(applicationContext)

        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN

        // Handle intent if completely closed
        handleDeepLink(intent)

        setContent {
            val settingsManager = remember { SettingsManager(applicationContext) }
            val themeMode by settingsManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val dynamicColor by settingsManager.dynamicColorFlow.collectAsState(initial = false)

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemDark
            }

            // Dynamically collect deep link parameters so Compose updates even if the app was in the background
            val (sharedPlotId, sharedOwnerId) = deepLinkParams.collectAsState().value

            SatMeasureTheme(darkTheme = useDarkTheme, dynamicColor = dynamicColor) {
                SatMesNavApp(initialPlotId = sharedPlotId, initialOwnerId = sharedOwnerId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(incomingIntent: Intent?) {
        if (Intent.ACTION_VIEW == incomingIntent?.action) {
            val uri = incomingIntent.data
            if (uri != null) {
                val plotId = uri.getQueryParameter("plotId")
                val ownerId = uri.getQueryParameter("ownerId")
                if (plotId != null && ownerId != null) {
                    deepLinkParams.value = plotId to ownerId
                }
            }
        }
    }
}