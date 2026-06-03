package com.example.satmeasure.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Crop169
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

enum class CalcMode { PINS, SHAPES, DRAW }
enum class ShapeType { RECTANGLE, SQUARE, CIRCLE, TRIANGLE }

@Composable
fun SatMapComponent(
    modifier: Modifier = Modifier,
    currentMapStyle: String,
    bottomPadding: Dp = 0.dp,
    onMapInteract: () -> Unit = {}
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
    var isCalcExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        // --- 1. THE MAP LAYER ---
        MapboxMap(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            if (event.changes.any { it.pressed && !it.isConsumed }) {
                                isCalcExpanded = false
                                onMapInteract()
                            }
                        }
                    }
                },
            mapViewportState = viewportState,
            compass = {},
            scaleBar = {},
            style = { MapStyle(style = currentMapStyle) }
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

        // --- 2. RIGHT SIDE CONTROLS (Compass + MyLocation + Calculate Area) ---
        val controlModifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = bottomPadding + 16.dp, end = 16.dp)

        // COMPASS BUTTON COMPOSABLE
        @Composable
        fun CompassButton() {
            AnimatedVisibility(
                visible = kotlin.math.abs(currentBearing) > 1.0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        viewportState.flyTo(CameraOptions.Builder().bearing(0.0).build())
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.compass_logo),
                        contentDescription = "Reset North",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp).rotate((-currentBearing).toFloat())
                    )
                }
            }
        }

        if (isLandscape) {
            Column(
                modifier = controlModifier,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                CompassButton()
                if (hasLocationPermission) {
                    FloatingActionButton(
                        onClick = { followUser = true },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Outlined.MyLocation, "Follow Me")
                    }
                }
                CalculateAreaOverlay(
                    isExpanded = isCalcExpanded,
                    onExpandToggle = { isCalcExpanded = !isCalcExpanded }
                )
            }
        } else {
            Row(
                modifier = controlModifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                CompassButton()
                if (hasLocationPermission) {
                    FloatingActionButton(
                        onClick = { followUser = true },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Outlined.MyLocation, "Follow Me")
                    }
                }
                CalculateAreaOverlay(
                    isExpanded = isCalcExpanded,
                    onExpandToggle = { isCalcExpanded = !isCalcExpanded }
                )
            }
        }
    }
}

@Composable
fun CalculateAreaOverlay(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    val orientation = LocalConfiguration.current.orientation
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        CalculateAreaOverlayLandscape(isExpanded = isExpanded, onExpandToggle = onExpandToggle)
    } else {
        CalculateAreaOverlayPortrait(isExpanded = isExpanded, onExpandToggle = onExpandToggle)
    }
}

@Composable
fun CalculateAreaOverlayPortrait(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    var activeMode by remember { mutableStateOf<CalcMode?>(null) }
    var selectedShape by remember { mutableStateOf(ShapeType.RECTANGLE) }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Calculate UI Transition"
    ) { expanded ->
        if (!expanded) {
            // COLLAPSED STATE: 12.dp Rounded Rectangle
            ExtendedFloatingActionButton(
                onClick = onExpandToggle,
                icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
                text = { Text("Calculate Area", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            // EXPANDED STATE: Two-Step Navigation
            Card(
                modifier = Modifier.width(84.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = activeMode,
                        transitionSpec = {
                            if (targetState != null) {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }
                        },
                        label = "Menu Transition"
                    ) { mode ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (mode == null) {
                                // LEVEL 1: MAIN MENU
                                val modes = listOf(CalcMode.PINS, CalcMode.SHAPES, CalcMode.DRAW)
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    modes.forEach { m ->
                                        val icon = when(m) {
                                            CalcMode.PINS -> Icons.Default.LocationOn
                                            CalcMode.SHAPES -> Icons.Default.CropSquare
                                            CalcMode.DRAW -> Icons.Default.Draw
                                        }
                                        val label = when(m) {
                                            CalcMode.PINS -> "Pins"
                                            CalcMode.SHAPES -> "Shapes"
                                            CalcMode.DRAW -> "Draw"
                                        }
                                        
                                        Surface(
                                            onClick = { activeMode = m },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(Modifier.height(4.dp))
                                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(
                                    Modifier, DividerDefaults.Thickness,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // CLOSE BUTTON AT BOTTOM
                                IconButton(onClick = {
                                    activeMode = null
                                    onExpandToggle()
                                }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                                    Icon(Icons.Default.Clear, "Close")
                                }

                            } else {
                                // LEVEL 2: SUB-MENU
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    when (mode) {
                                        CalcMode.PINS -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(
                                                    onClick = { /* TODO */ },
                                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(24.dp))
                                                        Text("Drop\nPin", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { /* TODO */ },
                                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(24.dp))
                                                        Text("Undo", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }

                                        CalcMode.SHAPES -> {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                ShapeIconButton(Icons.Default.Crop169, selectedShape == ShapeType.RECTANGLE, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { selectedShape = ShapeType.RECTANGLE }
                                                ShapeIconButton(Icons.Default.CropSquare, selectedShape == ShapeType.SQUARE, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { selectedShape = ShapeType.SQUARE }
                                                ShapeIconButton(Icons.Default.Circle, selectedShape == ShapeType.CIRCLE, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { selectedShape = ShapeType.CIRCLE }
                                                ShapeIconButton(Icons.Default.ChangeHistory, selectedShape == ShapeType.TRIANGLE, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { selectedShape = ShapeType.TRIANGLE }
                                                
                                                HorizontalDivider(Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                                                Button(
                                                    onClick = { /* TODO */ },
                                                    modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.AddLocation, null, modifier = Modifier.size(24.dp))
                                                        Text("Drop", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }

                                        CalcMode.DRAW -> {
                                            var isDrawing by remember { mutableStateOf(false) }
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(
                                                    onClick = { isDrawing = !isDrawing },
                                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isDrawing) MaterialTheme.colorScheme.errorContainer else ButtonDefaults.buttonColors().containerColor,
                                                        contentColor = if (isDrawing) MaterialTheme.colorScheme.onErrorContainer else ButtonDefaults.buttonColors().contentColor
                                                    ),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(if (isDrawing) Icons.Default.Stop else Icons.Default.Draw, null, modifier = Modifier.size(24.dp))
                                                        Text(if (isDrawing) "Stop" else "Start", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { /* TODO: Clear Drawing */ },
                                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(24.dp))
                                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(
                                    Modifier, DividerDefaults.Thickness,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // BACK BUTTON AT BOTTOM
                                IconButton(onClick = { activeMode = null }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculateAreaOverlayLandscape(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    var activeMode by remember { mutableStateOf<CalcMode?>(null) }
    var selectedShape by remember { mutableStateOf(ShapeType.RECTANGLE) }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Calculate UI Transition"
    ) { expanded ->
        if (!expanded) {
            ExtendedFloatingActionButton(
                onClick = onExpandToggle,
                icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
                text = { Text("Calculate Area", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = activeMode,
                        transitionSpec = {
                            if (targetState != null) {
                                slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                            } else {
                                slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                            }
                        },
                        label = "Menu Transition"
                    ) { mode ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mode == null) {
                                // LEVEL 1: MAIN MENU
                                val modes = listOf(CalcMode.PINS, CalcMode.SHAPES, CalcMode.DRAW)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    modes.forEach { m ->
                                        val icon = when(m) {
                                            CalcMode.PINS -> Icons.Default.LocationOn
                                            CalcMode.SHAPES -> Icons.Default.CropSquare
                                            CalcMode.DRAW -> Icons.Default.Draw
                                        }
                                        val label = when(m) {
                                            CalcMode.PINS -> "Pins"
                                            CalcMode.SHAPES -> "Shapes"
                                            CalcMode.DRAW -> "Draw"
                                        }
                                        
                                        Surface(
                                            onClick = { activeMode = m },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.size(64.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(Modifier.height(4.dp))
                                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                VerticalDivider(
                                    Modifier.height(48.dp), DividerDefaults.Thickness,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // CLOSE BUTTON AT RIGHT
                                IconButton(onClick = {
                                    activeMode = null
                                    onExpandToggle()
                                }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Clear, "Close")
                                }

                            } else {
                                // LEVEL 2: SUB-MENU
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    when (mode) {
                                        CalcMode.PINS -> {
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(
                                                    onClick = { /* TODO */ },
                                                    modifier = Modifier.height(64.dp).width(80.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(24.dp))
                                                        Text("Drop Pin", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { /* TODO */ },
                                                    modifier = Modifier.height(64.dp).width(80.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(24.dp))
                                                        Text("Undo", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }

                                        CalcMode.SHAPES -> {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                ShapeIconButton(Icons.Default.Crop169, selectedShape == ShapeType.RECTANGLE, modifier = Modifier.height(64.dp).width(45.dp)) { selectedShape = ShapeType.RECTANGLE }
                                                ShapeIconButton(Icons.Default.CropSquare, selectedShape == ShapeType.SQUARE, modifier = Modifier.height(64.dp).width(45.dp)) { selectedShape = ShapeType.SQUARE }
                                                ShapeIconButton(Icons.Default.Circle, selectedShape == ShapeType.CIRCLE, modifier = Modifier.height(64.dp).width(45.dp)) { selectedShape = ShapeType.CIRCLE }
                                                ShapeIconButton(Icons.Default.ChangeHistory, selectedShape == ShapeType.TRIANGLE, modifier = Modifier.height(64.dp).width(45.dp)) { selectedShape = ShapeType.TRIANGLE }
                                                
                                                VerticalDivider(Modifier.height(48.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                                                Button(
                                                    onClick = { /* TODO */ },
                                                    modifier = Modifier.height(64.dp).width(56.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.AddLocation, null, modifier = Modifier.size(24.dp))
                                                        Text("Drop", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }

                                        CalcMode.DRAW -> {
                                            var isDrawing by remember { mutableStateOf(false) }
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(
                                                    onClick = { isDrawing = !isDrawing },
                                                    modifier = Modifier.height(64.dp).width(80.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isDrawing) MaterialTheme.colorScheme.errorContainer else ButtonDefaults.buttonColors().containerColor,
                                                        contentColor = if (isDrawing) MaterialTheme.colorScheme.onErrorContainer else ButtonDefaults.buttonColors().contentColor
                                                    ),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(if (isDrawing) Icons.Default.Stop else Icons.Default.Draw, null, modifier = Modifier.size(24.dp))
                                                        Text(if (isDrawing) "Stop" else "Start", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { /* TODO: Clear Drawing */ },
                                                    modifier = Modifier.height(64.dp).width(80.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(24.dp))
                                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))
                                    VerticalDivider(
                                        Modifier.height(48.dp), DividerDefaults.Thickness,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // BACK BUTTON AT RIGHT
                                    IconButton(onClick = { activeMode = null }, modifier = Modifier.size(48.dp)) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShapeIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val iconColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
    }
}