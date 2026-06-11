@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.satmeasure.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import kotlin.math.*
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import com.example.satmeasure.ui.components.DiscardWarningDialog
import com.example.satmeasure.ui.components.ClearWarningDialog
import com.example.satmeasure.utils.HapticHelper
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.satmeasure.R
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.geojson.Point
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.Typeface
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.Style
import com.mapbox.bindgen.Value
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.sp
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.example.satmeasure.ui.map.models.CalcMode
import com.example.satmeasure.ui.map.models.ShapeType
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils.setAlphaComponent
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap
import com.example.satmeasure.ui.viewmodel.MapAction
import com.example.satmeasure.ui.viewmodel.MapViewModel

fun createNumberedPinBitmap(context: Context, number: Int): Bitmap {
    val drawable =
        ContextCompat.getDrawable(context, R.drawable.ic_push_pin) ?: return createBitmap(1, 1)
    val scale = 0.75f
    val width = (drawable.intrinsicWidth * scale).toInt()
    val height = (drawable.intrinsicHeight * scale).toInt()
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    val paint = Paint().apply {
        color = WHITE
        textSize = height * 0.40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    val xPos = (canvas.width / 2).toFloat()
    val yPos = (height * 0.35f) - ((paint.descent() + paint.ascent()) / 2)
    canvas.drawText(number.toString(), xPos, yPos, paint)

    return bitmap
}

fun createShapeNodeBitmap(): Bitmap {
    val size = 48
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = WHITE
        isAntiAlias = true
    }
    val shadowPaint = Paint().apply {
        color = BLACK
        isAntiAlias = true
        alpha = 100
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, shadowPaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, paint)
    return bitmap
}

fun createCenterNodeBitmap(): Bitmap {
    val size = 64
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = WHITE
        isAntiAlias = true
    }
    val shadowPaint = Paint().apply {
        color = BLACK
        isAntiAlias = true
        alpha = 100
    }
    val corePaint = Paint().apply {
        color = setAlphaComponent("#FF5252".toColorInt(), 255)
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, shadowPaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, paint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 16f, corePaint)
    return bitmap
}

fun createRotateNodeBitmap(context: Context): Bitmap {
    val size = 56
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = setAlphaComponent("#2196F3".toColorInt(), 255)
        isAntiAlias = true
    }
    val shadowPaint = Paint().apply {
        color = BLACK
        isAntiAlias = true
        alpha = 80
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, shadowPaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_rotate)
    if (drawable != null) {
        val iconSize = 32
        val offset = (size - iconSize) / 2
        drawable.setBounds(offset, offset, offset + iconSize, offset + iconSize)
        drawable.draw(canvas)
    }
    return bitmap
}

fun createResizeNodeBitmap(context: Context): Bitmap {
    val size = 56
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = setAlphaComponent("#4CAF50".toColorInt(), 255) // green for resize
        isAntiAlias = true
    }
    val shadowPaint = Paint().apply {
        color = BLACK
        isAntiAlias = true
        alpha = 80
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, shadowPaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_resize)
    if (drawable != null) {
        val iconSize = 32
        val offset = (size - iconSize) / 2
        drawable.setBounds(offset, offset, offset + iconSize, offset + iconSize)
        drawable.draw(canvas)
    }
    return bitmap
}

@Composable
fun SatMapComponent(
    modifier: Modifier = Modifier,
    currentMapStyle: String,
    bottomPadding: Dp = 0.dp,
    viewModel: MapViewModel = viewModel(),
    onMapInteract: () -> Unit = {},
    onMeasurementsUpdated: (area: Double, perimeter: Double) -> Unit = { _, _ -> },
) {
    val density = LocalDensity.current
    val bottomPaddingPx = with(density) { bottomPadding.toPx() }.toDouble()

    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(77.3910, 28.5355))
            zoom(14.0)
            pitch(0.0)
            bearing(0.0)
            padding(com.mapbox.maps.EdgeInsets(0.0, 0.0, bottomPaddingPx, 0.0))
        }
    }

    LaunchedEffect(bottomPaddingPx) {
        viewportState.flyTo(
            CameraOptions.Builder()
                .padding(com.mapbox.maps.EdgeInsets(0.0, 0.0, bottomPaddingPx, 0.0))
                .build()
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val cameraTargetBounds = uiState.cameraTargetBounds
    var mapboxMap by remember { mutableStateOf<com.mapbox.maps.MapboxMap?>(null) }
    LaunchedEffect(cameraTargetBounds, mapboxMap) {
        if (!cameraTargetBounds.isNullOrEmpty() && mapboxMap != null) {
            kotlinx.coroutines.delay(450) // Wait for screen transition to complete
            try {
                val padding =
                    com.mapbox.maps.EdgeInsets(100.0, 100.0, 100.0 + bottomPaddingPx, 100.0)
                val options = mapboxMap!!.cameraForCoordinates(
                    cameraTargetBounds,
                    padding,
                    null,
                    null
                )
                viewportState.flyTo(options)
            } catch (_: Exception) {
                // Fallback
                val centerLat = cameraTargetBounds.map { it.latitude() }
                    .average()
                val centerLng = cameraTargetBounds.map { it.longitude() }
                    .average()
                viewportState.flyTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(centerLng, centerLat))
                        .zoom(16.0)
                        .padding(com.mapbox.maps.EdgeInsets(0.0, 0.0, bottomPaddingPx, 0.0))
                        .build()
                )
            }
            viewModel.onAction(MapAction.SetCameraTargetBounds(null))
        }
    }

    val currentBearing = viewportState.cameraState?.bearing ?: 0.0
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current

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

    // Map viewmodel states via value properties
    val isCalcExpanded = uiState.isCalcExpanded
    val activeMode = uiState.activeMode
    val completedMode = uiState.completedMode
    val isDrawing = uiState.isDrawing
    val isShapeDropped = uiState.isShapeDropped
    val showDiscardDialog = uiState.showDiscardDialog
    val showClearDialog = uiState.showClearDialog

    val pinPoints = uiState.pinPoints
    val drawPoints = uiState.drawPoints
    val drawPointsHistory = uiState.drawPointsHistory
    val redoDrawPointsHistory = uiState.redoDrawPointsHistory
    val shapePoints = uiState.shapePoints
    val shapePointsHistory = uiState.shapePointsHistory
    val redoShapePointsHistory = uiState.redoShapePointsHistory
    val setShowDiscardDialog: (Boolean) -> Unit = {
        viewModel.onAction(
            MapAction.SetShowDiscardDialog(it)
        )
    }
    val setShowClearDialog: (Boolean) -> Unit =
        { viewModel.onAction(MapAction.SetShowClearDialog(it)) }

    // Local component states
    var is3DMode by rememberSaveable { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    // Auto-calculate measurements whenever points or modes change
    LaunchedEffect(
        activeMode, completedMode,
        pinPoints.size, shapePoints.size, drawPoints.size,
        pinPoints.toList(), shapePoints.toList(), drawPoints.toList()
    ) {
        val mode = activeMode ?: completedMode
        val pointsToMeasure = when (mode) {
            CalcMode.PINS -> pinPoints.toList()
            CalcMode.SHAPES -> shapePoints.toList()
            CalcMode.DRAW -> drawPoints.toList()
            else -> emptyList()
        }

        val (area, perimeter) = MathHelpers.calculatePolygonMeasurements(pointsToMeasure)
        onMeasurementsUpdated(area, perimeter)
    }

    val hasActiveCalculationData =
        pinPoints.isNotEmpty() || drawPoints.isNotEmpty() || isShapeDropped

    val handleBackRequest = {
        if (hasActiveCalculationData) {
            setShowDiscardDialog(true)
        } else {
            viewModel.onAction(MapAction.SetActiveMode(null))
        }
    }

    BackHandler(enabled = activeMode != null) {
        handleBackRequest()
    }

    if (showDiscardDialog) {
        DiscardWarningDialog(
            onDismiss = { setShowDiscardDialog(false) },
            onConfirm = {
                setShowDiscardDialog(false)
                viewModel.onAction(MapAction.ClearAll)
            }
        )
    }

    if (showClearDialog) {
        ClearWarningDialog(
            onDismiss = { setShowClearDialog(false) },
            onConfirm = {
                setShowClearDialog(false)
                viewModel.onAction(MapAction.ClearAll)
            }
        )
    }

    val handleClearAll: () -> Unit = {
        if (hasActiveCalculationData) {
            setShowClearDialog(true)
        } else {
            viewModel.onAction(MapAction.SetCompletedMode(null))
            viewModel.onAction(MapAction.SetCalcExpanded(false))
            viewModel.onAction(MapAction.SetActiveMode(null))
        }
    }

    var selectedShape by remember { mutableStateOf(ShapeType.HEXAGON) }


    val handleDropShape: () -> Unit = {
        viewModel.onAction(MapAction.SetShapeDropped(true))
        val sw = windowInfo.containerSize.width.toDouble()
        val sh = windowInfo.containerSize.height.toDouble() - bottomPaddingPx
        if (mapboxMap != null) {
            val nwCoordinates = mapboxMap!!.coordinateForPixel(
                com.mapbox.maps.ScreenCoordinate(sw * 0.25, sh * 0.45)
            )
            val seCoordinates = mapboxMap!!.coordinateForPixel(
                com.mapbox.maps.ScreenCoordinate(sw * 0.75, sh * 0.55)
            )
            val c = Point.fromLngLat(
                (nwCoordinates.longitude() + seCoordinates.longitude()) / 2.0,
                (nwCoordinates.latitude() + seCoordinates.latitude()) / 2.0
            )
            val isLandscape = sw > sh
            val multiplier = if (isLandscape) 0.55 else 1.25
            val offsetMeters = MathHelpers.distanceInMeters(
                c, Point.fromLngLat(seCoordinates.longitude(), c.latitude())
            ) * multiplier

            val newShape = mutableListOf<Point>()
            when (selectedShape) {
                ShapeType.TRIANGLE -> {
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 0.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 120.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 240.0))
                }

                ShapeType.SQUARE -> {
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 315.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 45.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 135.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 225.0))
                }

                ShapeType.HEXAGON -> {
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 30.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 90.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 150.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 210.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 270.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 330.0))
                }

                ShapeType.CIRCLE -> {
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 0.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 90.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 180.0))
                    newShape.add(MathHelpers.destinationPoint(c, offsetMeters, 270.0))
                }
            }
            viewModel.onAction(MapAction.SetShapePoints(newShape))
        }
    }
    var draggedShapeNodeIndex by remember { mutableStateOf<Int?>(null) }
    var isDraggingShape by remember { mutableStateOf(false) }
    var isRotatingShape by remember { mutableStateOf(false) }
    var isResizingShape by remember { mutableStateOf(false) }
    var lastDragPoint by remember { mutableStateOf<Point?>(null) }

    LaunchedEffect(is3DMode) {
        if (is3DMode) {
            viewportState.flyTo(
                CameraOptions.Builder()
                    .pitch(60.0)
                    .build()
            )
        } else {
            viewportState.flyTo(
                CameraOptions.Builder()
                    .pitch(0.0)
                    .build()
            )
        }
    }

    val center = viewportState.cameraState?.center
    var connectTargetIndex by remember { mutableStateOf<Int?>(null) }
    var connectTargetPoint by remember { mutableStateOf<Point?>(null) }

    LaunchedEffect(center, pinPoints.size, mapboxMap) {
        if (center != null && pinPoints.size >= 3 && mapboxMap != null) {
            val centerPixel = mapboxMap!!.pixelForCoordinate(center)
            val nearest = pinPoints.withIndex()
                .minByOrNull {
                    val pointPixel = mapboxMap!!.pixelForCoordinate(it.value)
                    val dx = centerPixel.x - pointPixel.x
                    val dy = centerPixel.y - pointPixel.y
                    sqrt(dx * dx + dy * dy)
                }
            if (nearest != null) {
                val pointPixel = mapboxMap!!.pixelForCoordinate(nearest.value)
                val dx = centerPixel.x - pointPixel.x
                val dy = centerPixel.y - pointPixel.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 150.0 && nearest.index < pinPoints.size - 2) {
                    connectTargetIndex = nearest.index + 1
                    connectTargetPoint = nearest.value
                } else {
                    connectTargetIndex = null
                    connectTargetPoint = null
                }
            } else {
                connectTargetIndex = null
                connectTargetPoint = null
            }
        } else {
            connectTargetIndex = null
            connectTargetPoint = null
        }
    }

    val currentIsDrawing by rememberUpdatedState(isDrawing)
    val currentDrawPoints by rememberUpdatedState(drawPoints)
    val currentShapePoints by rememberUpdatedState(shapePoints)
    val currentSelectedShape by rememberUpdatedState(selectedShape)

    Box(modifier = modifier.fillMaxSize()) {

        // --- 1. THE MAP LAYER ---
        MapboxMap(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeMode, isShapeDropped) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: continue

                            if (activeMode == CalcMode.DRAW && currentIsDrawing && mapboxMap != null) {
                                val screenCoordinates = com.mapbox.maps.ScreenCoordinate(
                                    change.position.x.toDouble(), change.position.y.toDouble()
                                )
                                val mapPoint = mapboxMap!!.coordinateForPixel(screenCoordinates)
                                if (change.pressed) {
                                    if (!change.previousPressed) {
                                        viewModel.onAction(
                                            MapAction.CommitDrawPath(currentDrawPoints)
                                        )

                                    }
                                    if (currentDrawPoints.isEmpty() || MathHelpers.distanceInMeters(
                                            currentDrawPoints.last(), mapPoint
                                        ) > 2.0
                                    ) {
                                        viewModel.onAction(MapAction.AddDrawPoint(mapPoint))
                                    }
                                    change.consume()
                                }
                            }

                            if (activeMode == CalcMode.SHAPES && isShapeDropped && mapboxMap != null) {
                                val screenCoordinates = com.mapbox.maps.ScreenCoordinate(
                                    change.position.x.toDouble(), change.position.y.toDouble()
                                )
                                val mapPoint = mapboxMap!!.coordinateForPixel(screenCoordinates)

                                if (change.pressed && !change.previousPressed) {
                                    // ACTION_DOWN
                                    var foundNode = false
                                    for ((i, point) in currentShapePoints.withIndex()) {
                                        val pixel = mapboxMap!!.pixelForCoordinate(point)
                                        val dx = pixel.x - change.position.x
                                        val dy = pixel.y - change.position.y
                                        if (dx * dx + dy * dy < 10000) { // ~100px radius approx
                                            draggedShapeNodeIndex = i
                                            foundNode = true
                                            break
                                        }
                                    }
                                    if (!foundNode) {
                                        if (currentSelectedShape != ShapeType.CIRCLE && currentShapePoints.size >= 3) {
                                            val handle1 = MathHelpers.midPoint(
                                                currentShapePoints[0], currentShapePoints[1]
                                            )
                                            val oppIdx = currentShapePoints.size / 2
                                            val oppNextIdx =
                                                if (oppIdx + 1 < currentShapePoints.size) oppIdx + 1 else 0
                                            val handle2 = MathHelpers.midPoint(
                                                currentShapePoints[oppIdx],
                                                currentShapePoints[oppNextIdx]
                                            )

                                            val p1 = mapboxMap!!.pixelForCoordinate(handle1)
                                            val p2 = mapboxMap!!.pixelForCoordinate(handle2)

                                            val dx1 = p1.x - change.position.x
                                            val dy1 = p1.y - change.position.y
                                            val dx2 = p2.x - change.position.x
                                            val dy2 = p2.y - change.position.y

                                            if (dx1 * dx1 + dy1 * dy1 < 10000) {
                                                isRotatingShape = true
                                                lastDragPoint = mapPoint
                                                foundNode = true
                                            } else if (dx2 * dx2 + dy2 * dy2 < 10000) {
                                                isResizingShape = true
                                                lastDragPoint = mapPoint
                                                foundNode = true
                                            }
                                        }
                                    }

                                    if (!foundNode) {
                                        val center = MathHelpers.calculateCenter(currentShapePoints)
                                        val centerPixel = mapboxMap!!.pixelForCoordinate(center)
                                        val dx = centerPixel.x - change.position.x
                                        val dy = centerPixel.y - change.position.y

                                        if (dx * dx + dy * dy < 10000) { // ~100px radius approx for the red dot
                                            isDraggingShape = true
                                            lastDragPoint = mapPoint
                                        }
                                    }
                                    if (draggedShapeNodeIndex != null || isDraggingShape || isRotatingShape) {
                                        viewModel.onAction(
                                            MapAction.CommitShapePath(currentShapePoints)
                                        )

                                        change.consume()
                                    }
                                } else if (change.pressed) {
                                    // ACTION_MOVE
                                    if (draggedShapeNodeIndex != null) {
                                        if (currentSelectedShape == ShapeType.CIRCLE) {
                                            val center =
                                                MathHelpers.calculateCenter(currentShapePoints)
                                            val radius =
                                                MathHelpers.distanceInMeters(center, mapPoint)
                                            val newShape = currentShapePoints.toMutableList()
                                            newShape[0] =
                                                MathHelpers.destinationPoint(center, radius, 0.0)
                                            newShape[1] =
                                                MathHelpers.destinationPoint(center, radius, 90.0)
                                            newShape[2] =
                                                MathHelpers.destinationPoint(center, radius, 180.0)
                                            newShape[3] =
                                                MathHelpers.destinationPoint(center, radius, 270.0)
                                            viewModel.onAction(MapAction.SetShapePoints(newShape))
                                        } else {
                                            val newShape = currentShapePoints.toMutableList()
                                            newShape[draggedShapeNodeIndex!!] = mapPoint
                                            viewModel.onAction(MapAction.SetShapePoints(newShape))
                                        }
                                        change.consume()
                                    } else if (isDraggingShape) {
                                        if (lastDragPoint != null) {
                                            val dLat =
                                                mapPoint.latitude() - lastDragPoint!!.latitude()
                                            val dLng =
                                                mapPoint.longitude() - lastDragPoint!!.longitude()
                                            val newShape = currentShapePoints.map {
                                                Point.fromLngLat(
                                                    it.longitude() + dLng, it.latitude() + dLat
                                                )
                                            }
                                            viewModel.onAction(MapAction.SetShapePoints(newShape))
                                            lastDragPoint = mapPoint
                                        }
                                        change.consume()
                                    } else if (isRotatingShape) {
                                        if (lastDragPoint != null) {
                                            val center =
                                                MathHelpers.calculateCenter(currentShapePoints)
                                            val oldAngle = MathHelpers.calculateBearing(
                                                center, lastDragPoint!!
                                            )
                                            val newAngle =
                                                MathHelpers.calculateBearing(center, mapPoint)
                                            val diff = newAngle - oldAngle

                                            val newShape = currentShapePoints.map {
                                                MathHelpers.rotatePoint(center, it, diff)
                                            }
                                            viewModel.onAction(MapAction.SetShapePoints(newShape))
                                            lastDragPoint = mapPoint
                                        }
                                        change.consume()
                                    } else if (isResizingShape) {
                                        if (lastDragPoint != null) {
                                            val center =
                                                MathHelpers.calculateCenter(currentShapePoints)
                                            val oldDist = MathHelpers.distanceInMeters(
                                                center, lastDragPoint!!
                                            )
                                            val newDist =
                                                MathHelpers.distanceInMeters(center, mapPoint)
                                            if (oldDist > 0) {
                                                val scale = newDist / oldDist
                                                val newShape = currentShapePoints.map { pt ->
                                                    val dist =
                                                        MathHelpers.distanceInMeters(center, pt)
                                                    val bearing =
                                                        MathHelpers.calculateBearing(center, pt)
                                                    MathHelpers.destinationPoint(
                                                        center, dist * scale, bearing
                                                    )
                                                }
                                                viewModel.onAction(
                                                    MapAction.SetShapePoints(newShape)
                                                )
                                            }
                                            lastDragPoint = mapPoint
                                        }
                                        change.consume()
                                    }
                                } else if (change.previousPressed) {
                                    // ACTION_UP
                                    draggedShapeNodeIndex = null
                                    isDraggingShape = false
                                    isRotatingShape = false
                                    isResizingShape = false
                                    lastDragPoint = null
                                }
                            }

                            if (event.changes.any { it.pressed && !it.isConsumed }) {
                                if (activeMode == null && completedMode == null) {
                                    viewModel.onAction(MapAction.SetCalcExpanded(false))
                                }
                                // Tracking disabled since we only fly once
                                onMapInteract()
                            }
                        }
                    }
                },
            onMapClickListener = {
                when (activeMode) {
                    CalcMode.PINS -> {
                        // Disabled: use targeting button instead
                        false
                    }

                    CalcMode.DRAW -> {
                        false
                    }

                    else -> false
                }
            },
            mapViewportState = viewportState,
            compass = {},
            scaleBar = {}
        ) {

            val context = LocalContext.current
            MapEffect(pinPoints.size, isShapeDropped) { mapView ->
                mapboxMap = mapView.mapboxMap
                mapView.mapboxMap.getStyle { style ->
                    pinPoints.forEachIndexed { index, _ ->
                        val num = index + 1
                        val id = "pin-$num"
                        if (style.getStyleImage(id) == null) {
                            style.addImage(id, createNumberedPinBitmap(context, num))
                        }
                    }
                    if (style.getStyleImage("shape_node_icon") == null) {
                        style.addImage("shape_node_icon", createShapeNodeBitmap())
                    }
                    if (style.getStyleImage("center_node_icon") == null) {
                        style.addImage("center_node_icon", createCenterNodeBitmap())
                    }
                    if (style.getStyleImage("rotate_node_icon") == null) {
                        style.addImage("rotate_node_icon", createRotateNodeBitmap(context))
                    }
                    if (style.getStyleImage("resize_node_icon") == null) {
                        style.addImage("resize_node_icon", createResizeNodeBitmap(context))
                    }
                }
            }

            // --- RENDER PINS ---
            if (pinPoints.isNotEmpty()) {
                PointAnnotationGroup(
                    annotations = pinPoints.mapIndexed { index, point ->
                        PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage("pin-${index + 1}")
                            .withIconAnchor(
                                com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.BOTTOM
                            )
                    }
                )

                if (pinPoints.size >= 2) {
                    PolylineAnnotationGroup(
                        annotations = listOf(
                            PolylineAnnotationOptions()
                                .withGeometry(com.mapbox.geojson.LineString.fromLngLats(pinPoints))
                                .withLineColor(Color.White.toArgb())
                                .withLineWidth(5.0)
                        )
                    )
                }

                if (pinPoints.size >= 3) {
                    // Close the polygon
                    val closedPoints = pinPoints.toList() + pinPoints.first()
                    PolygonAnnotationGroup(
                        annotations = listOf(
                            PolygonAnnotationOptions()
                                .withGeometry(
                                    com.mapbox.geojson.Polygon.fromLngLats(listOf(closedPoints))
                                )
                                .withFillColor(MaterialTheme.colorScheme.primary.toArgb())
                                .withFillOpacity(0.4)
                        )
                    )
                }
            }

            // --- RENDER DRAW ---
            if (drawPoints.isNotEmpty()) {
                if (drawPoints.size >= 2) {
                    PolylineAnnotationGroup(
                        annotations = listOf(
                            PolylineAnnotationOptions()
                                .withGeometry(com.mapbox.geojson.LineString.fromLngLats(drawPoints))
                                .withLineColor(Color.White.toArgb())
                                .withLineWidth(3.0)
                        )
                    )
                }

                if (drawPoints.size >= 3) {
                    val closedDrawPoints = drawPoints.toList() + drawPoints.first()
                    PolygonAnnotationGroup(
                        annotations = listOf(
                            PolygonAnnotationOptions()
                                .withGeometry(
                                    com.mapbox.geojson.Polygon.fromLngLats(listOf(closedDrawPoints))
                                )
                                .withFillColor(MaterialTheme.colorScheme.error.toArgb())
                                .withFillOpacity(0.4)
                        )
                    )
                }
            }

            // --- RENDER SHAPES ---
            if (isShapeDropped && shapePoints.isNotEmpty()) {
                val renderPoints = if (selectedShape == ShapeType.CIRCLE && shapePoints.size == 4) {
                    val center = MathHelpers.calculateCenter(shapePoints)
                    val radius = MathHelpers.distanceInMeters(center, shapePoints[0])
                    MathHelpers.generateCirclePolygon(center, radius)
                } else {
                    shapePoints.toList()
                }

                if (renderPoints.size >= 3) {
                    val closedShapePoints = renderPoints + renderPoints.first()
                    PolygonAnnotationGroup(
                        annotations = listOf(
                            PolygonAnnotationOptions()
                                .withGeometry(
                                    com.mapbox.geojson.Polygon.fromLngLats(
                                        listOf(closedShapePoints)
                                    )
                                )
                                .withFillColor(MaterialTheme.colorScheme.tertiary.toArgb())
                                .withFillOpacity(0.4)
                        )
                    )
                    PolylineAnnotationGroup(
                        annotations = listOf(
                            PolylineAnnotationOptions()
                                .withGeometry(
                                    com.mapbox.geojson.LineString.fromLngLats(closedShapePoints)
                                )
                                .withLineColor(WHITE)
                                .withLineWidth(3.0)
                        )
                    )
                }
                if (activeMode == CalcMode.SHAPES) {
                    val centerPoint = MathHelpers.calculateCenter(shapePoints)
                    var annotations = shapePoints.map {
                        PointAnnotationOptions()
                            .withPoint(it)
                            .withIconImage("shape_node_icon")
                            .withIconAnchor(
                                com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.CENTER
                            )
                    } + PointAnnotationOptions()
                        .withPoint(centerPoint)
                        .withIconImage("center_node_icon")
                        .withIconAnchor(
                            com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.CENTER
                        )

                    if (selectedShape != ShapeType.CIRCLE && shapePoints.size >= 3) {
                        val handle1 = MathHelpers.midPoint(shapePoints[0], shapePoints[1])
                        val oppIdx = shapePoints.size / 2
                        val oppNextIdx = if (oppIdx + 1 < shapePoints.size) oppIdx + 1 else 0
                        val handle2 =
                            MathHelpers.midPoint(shapePoints[oppIdx], shapePoints[oppNextIdx])

                        annotations = annotations + PointAnnotationOptions()
                            .withPoint(handle1)
                            .withIconImage("rotate_node_icon")
                            .withIconAnchor(
                                com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.CENTER
                            ) +
                                PointAnnotationOptions()
                                    .withPoint(handle2)
                                    .withIconImage("resize_node_icon")
                                    .withIconAnchor(
                                        com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.CENTER
                                    )
                    }

                    PointAnnotationGroup(annotations = annotations)
                }
            }

            MapEffect(currentMapStyle, isDarkTheme, is3DMode) { mapView ->
                mapView.mapboxMap.setBounds(
                    CameraBoundsOptions.Builder()
                        .maxZoom(20.0)
                        .build()
                )

                mapView.mapboxMap.loadStyle(currentMapStyle) { style ->
                    // Fix blurry raster satellite tiles at extreme zoom limits by targeting all raster layers
                    style.styleLayers.forEach { layerInfo ->
                        if (layerInfo.type == "raster") {
                            style.setStyleLayerProperty(
                                layerInfo.id, "raster-resampling", Value("nearest")
                            )
                        }
                    }

                    if (currentMapStyle == Style.STANDARD) {
                        style.setStyleImportConfigProperty(
                            "basemap", "showPointOfInterestLabels", Value(true)
                        )
                        style.setStyleImportConfigProperty(
                            "basemap", "show3dBuildings", Value(is3DMode)
                        )
                        val lightPreset = if (isDarkTheme) "night" else "day"
                        style.setStyleImportConfigProperty(
                            "basemap", "lightPreset", Value(lightPreset)
                        )
                    }
                }
            }

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
                } catch (_: Exception) {
                }
            }


            var listenerRegistered by remember { mutableStateOf(false) }
            MapEffect(hasLocationPermission) { mapView ->
                if (hasLocationPermission && !listenerRegistered) {
                    try {
                        mapView.location.addOnIndicatorPositionChangedListener { point ->
                            viewModel.onAction(MapAction.SetCurrentUserLocation(point))
                        }
                        listenerRegistered = true
                    } catch (_: Exception) {
                    }
                }
            }
        }

        // --- TARGET CROSSHAIR (PINS MODE) ---
        AnimatedVisibility(
            visible = activeMode == CalcMode.PINS,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -(bottomPadding / 2))
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = stringResource(id = R.string.cd_target),
                tint = Color.Red,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.icon_xl))
                    .offset(y = (-24).dp)
            )
        }

        val controlsBottomPadding =
            if (isLandscape) dimensionResource(id = R.dimen.spacing_md) else bottomPadding + 16.dp
        val controlModifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(
                bottom = controlsBottomPadding, end = dimensionResource(id = R.dimen.spacing_md)
            )

        // COMPASS BUTTON COMPOSABLE
        @Composable
        fun CompassButton() {
            AnimatedVisibility(
                visible = abs(currentBearing) > 1.0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        viewportState.flyTo(
                            CameraOptions.Builder()
                                .bearing(0.0)
                                .build()
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.button_height))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.compass_logo),
                        contentDescription = stringResource(id = R.string.cd_reset_north),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.spacing_xl))
                            .rotate((-currentBearing).toFloat())
                    )
                }
            }
        }

        @Composable
        fun Toggle3DButton() {
            FloatingActionButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    is3DMode = !is3DMode
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = if (is3DMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
                modifier = Modifier.size(dimensionResource(id = R.dimen.button_height))
            ) {
                Text(if (is3DMode) "3D" else "2D", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (isLandscape) {
            Column(
                modifier = controlModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                CompassButton()
                Toggle3DButton()
                if (hasLocationPermission) {
                    FloatingActionButton(
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            (uiState.currentUserLocation)?.let { loc ->
                                viewportState.flyTo(
                                    CameraOptions.Builder()
                                        .center(loc)
                                        .zoom(16.5)
                                        .build()
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.button_height))
                    ) {
                        Icon(
                            Icons.Outlined.MyLocation, stringResource(id = R.string.cd_my_location)
                        )
                    }
                }
                CalculateAreaOverlay(
                    isExpanded = isCalcExpanded,
                    onExpandToggle = {
                        viewModel.onAction(
                            MapAction.SetCalcExpanded(!isCalcExpanded)
                        )
                    },
                    activeMode = activeMode,
                    onActiveModeChange = { viewModel.onAction(MapAction.SetActiveMode(it)) },
                    completedMode = completedMode,
                    onCompletedModeChange = { viewModel.onAction(MapAction.SetCompletedMode(it)) },
                    onClearAll = handleClearAll,
                    selectedShape = selectedShape,
                    onSelectedShapeChange = { selectedShape = it },
                    isShapeDropped = isShapeDropped,
                    onDropShape = handleDropShape,
                    onClearShape = {
                        viewModel.onAction(MapAction.SetShapeDropped(false))
                        viewModel.onAction(MapAction.SetShapePoints(emptyList()))
                        viewModel.onAction(MapAction.ClearShape)
                    },
                    onUndoShape = { viewModel.onAction(MapAction.UndoShape) },
                    onRedoShape = { viewModel.onAction(MapAction.RedoShape) },
                    canUndoShape = shapePointsHistory.isNotEmpty(),
                    canRedoShape = redoShapePointsHistory.isNotEmpty(),
                    isDrawing = isDrawing,
                    onToggleDrawing = { viewModel.onAction(MapAction.SetIsDrawing(!isDrawing)) },
                    onClearDrawing = {
                        viewModel.onAction(MapAction.ClearDraw)
                    },
                    onUndoDraw = {
                        viewModel.onAction(MapAction.UndoDraw)
                    },
                    onRedoDraw = {
                        viewModel.onAction(MapAction.RedoDraw)
                    },
                    canUndoDraw = drawPointsHistory.isNotEmpty(),
                    canRedoDraw = redoDrawPointsHistory.isNotEmpty(),
                    onClearPins = {
                        viewModel.onAction(MapAction.ClearPins)
                    },
                    hasPins = pinPoints.isNotEmpty(),
                    onUndoPin = { viewModel.onAction(MapAction.UndoPin) },
                    onRedoPin = {
                        viewModel.onAction(MapAction.RedoPin)
                    },
                    onDropPin = {
                        val c = viewportState.cameraState?.center
                        if (c != null) {
                            viewModel.onAction(MapAction.AddPinPoint(c))

                        }
                    },
                    connectTargetIndex = connectTargetIndex,
                    onConnect = {
                        if (connectTargetPoint != null) {
                            viewModel.onAction(MapAction.AddPinPoint(connectTargetPoint!!))

                        }
                    },
                    onBackRequest = handleBackRequest,
                    hasDrawing = drawPoints.isNotEmpty(),
                    isReadOnly = uiState.isReadOnly,
                    onDoneReadOnly = { viewModel.onAction(MapAction.ExitReadOnly) }
                )
            }
        } else {
            Column(
                modifier = controlModifier,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                CompassButton()
                Toggle3DButton()
                if (hasLocationPermission) {
                    FloatingActionButton(
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            (uiState.currentUserLocation)?.let { loc ->
                                viewportState.flyTo(
                                    CameraOptions.Builder()
                                        .center(loc)
                                        .zoom(16.5)
                                        .build()
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.button_height))
                    ) {
                        Icon(
                            Icons.Outlined.MyLocation, stringResource(id = R.string.cd_my_location)
                        )
                    }
                }
                CalculateAreaOverlay(
                    isExpanded = isCalcExpanded,
                    onExpandToggle = {
                        viewModel.onAction(
                            MapAction.SetCalcExpanded(!isCalcExpanded)
                        )
                    },
                    activeMode = activeMode,
                    onActiveModeChange = { viewModel.onAction(MapAction.SetActiveMode(it)) },
                    completedMode = completedMode,
                    onCompletedModeChange = { viewModel.onAction(MapAction.SetCompletedMode(it)) },
                    onClearAll = handleClearAll,
                    selectedShape = selectedShape,
                    onSelectedShapeChange = { selectedShape = it },
                    isShapeDropped = isShapeDropped,
                    onDropShape = handleDropShape,
                    onClearShape = {
                        viewModel.onAction(MapAction.SetShapeDropped(false))
                        viewModel.onAction(MapAction.SetShapePoints(emptyList()))
                        viewModel.onAction(MapAction.ClearShape)
                    },
                    onUndoShape = { viewModel.onAction(MapAction.UndoShape) },
                    onRedoShape = { viewModel.onAction(MapAction.RedoShape) },
                    canUndoShape = shapePointsHistory.isNotEmpty(),
                    canRedoShape = redoShapePointsHistory.isNotEmpty(),
                    isDrawing = isDrawing,
                    onToggleDrawing = { viewModel.onAction(MapAction.SetIsDrawing(!isDrawing)) },
                    onClearDrawing = {
                        viewModel.onAction(MapAction.ClearDraw)
                    },
                    onUndoDraw = {
                        viewModel.onAction(MapAction.UndoDraw)
                    },
                    onRedoDraw = {
                        viewModel.onAction(MapAction.RedoDraw)
                    },
                    canUndoDraw = drawPointsHistory.isNotEmpty(),
                    canRedoDraw = redoDrawPointsHistory.isNotEmpty(),
                    onClearPins = {
                        viewModel.onAction(MapAction.ClearPins)
                    },
                    hasPins = pinPoints.isNotEmpty(),
                    onUndoPin = { viewModel.onAction(MapAction.UndoPin) },
                    onRedoPin = {
                        viewModel.onAction(MapAction.RedoPin)
                    },
                    onDropPin = {
                        val c = viewportState.cameraState?.center
                        if (c != null) {
                            viewModel.onAction(MapAction.AddPinPoint(c))

                        }
                    },
                    connectTargetIndex = connectTargetIndex,
                    onConnect = {
                        if (connectTargetPoint != null) {
                            viewModel.onAction(MapAction.AddPinPoint(connectTargetPoint!!))

                        }
                    },
                    onBackRequest = handleBackRequest,
                    hasDrawing = drawPoints.isNotEmpty(),
                    isReadOnly = uiState.isReadOnly,
                    onDoneReadOnly = { viewModel.onAction(MapAction.ExitReadOnly) }
                )
            }
        }
    }
}