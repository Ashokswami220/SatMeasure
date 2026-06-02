package com.example.satmeasure.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.satmeasure.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport

@Composable
fun SatMapComponent(
    modifier: Modifier = Modifier,
    currentMapStyle: String // Inherited from MapScreen
) {
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(77.3910, 28.5355))
            zoom(14.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val currentBearing = viewportState.cameraState?.bearing ?: 0.0
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var followUser by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            compass = {},
            scaleBar = {},
            style = { MapStyle(style = currentMapStyle) } // Reacts instantly to TopBar clicks
        ) {
            MapEffect(hasLocationPermission) { mapView ->
                try {
                    if (hasLocationPermission) {
                        mapView.location.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                            puckBearingEnabled = true
                            locationPuck = createDefault2DPuck(withBearing = true)
                        }
                    }
                } catch (_: Exception) {}
            }

            MapEffect(followUser) { mapView ->
                if (followUser && hasLocationPermission) {
                    try {
                        val viewportPlugin = mapView.viewport
                        val followPuckOptions = FollowPuckViewportStateOptions.Builder()
                            .zoom(16.5)
                            .pitch(0.0)
                            .build()
                        val followPuckState = viewportPlugin.makeFollowPuckViewportState(followPuckOptions)
                        viewportPlugin.transitionTo(followPuckState)
                    } catch (_: Exception) {}
                    followUser = false
                }
            }
        }

        // Animated Compass Logo
        AnimatedVisibility(
            visible = kotlin.math.abs(currentBearing) > 1.0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing) // Keeps it out of the status bar
                .padding(
                    top = if (isLandscape) 16.dp else 80.dp, // Pushed below the new TopControls
                    end = 24.dp
                )
        ) {
            SmallFloatingActionButton(
                onClick = {
                    viewportState.flyTo(CameraOptions.Builder().bearing(0.0).build())
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.compass_logo),
                    contentDescription = "Reset North",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate((-currentBearing).toFloat())
                )
            }
        }

        // My Location FAB
        if (hasLocationPermission) {
            FloatingActionButton(
                onClick = { followUser = true },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 16.dp)
                    .size(52.dp)
            ) {
                Icon(Icons.Outlined.MyLocation, "Follow Me")
            }
        }
    }
}