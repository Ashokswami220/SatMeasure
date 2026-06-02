package com.example.satmeasure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.satmeasure.ui.navigation.SatMesNavApp
import com.example.satmeasure.ui.theme.SatMeasureTheme
import com.mapbox.common.MapboxOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN

        setContent {
            SatMeasureTheme {
                SatMesNavApp()
            }
        }
    }
}