package com.example.satmeasure.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import com.example.satmeasure.ui.map.SatMapComponent
import com.example.satmeasure.ui.navigation.SatMesRoutes
import com.example.satmeasure.ui.navigation.AppSidebar
import com.example.satmeasure.ui.navigation.MainTopControls
import com.example.satmeasure.ui.components.MainCustomBottomSheet
import com.example.satmeasure.ui.navigation.MapStyleBottomSheet
import com.example.satmeasure.ui.components.MainBottomSheet
import com.example.satmeasure.ui.components.ExportPdfDialog
import com.example.satmeasure.ui.navigation.getAvailableMapStyles
import com.example.satmeasure.ui.viewmodel.AuthViewModel
import com.example.satmeasure.ui.viewmodel.MapViewModel
import com.example.satmeasure.ui.viewmodel.MapAction
import com.example.satmeasure.ui.map.models.CalcMode
import com.example.satmeasure.model.PointData
import androidx.compose.runtime.collectAsState
import com.example.satmeasure.ui.components.SaveMeasurementDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.satmeasure.utils.PdfGenerator
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.satmeasure.R
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    mapViewModel: MapViewModel,
    authViewModel: AuthViewModel,
    portraitPeekHeight: Dp = dimensionResource(id = R.dimen.dimen_120),
    portraitExpandedHeightRatio: Float = 0.5f,
    landscapePeekHeight: Dp = dimensionResource(id = R.dimen.dimen_100),
    landscapeExpandedHeightRatio: Float = 0.93f
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    val mapUiState by mapViewModel.uiState.collectAsState()

    val msgPdfExportedSuccessfully = stringResource(R.string.msg_pdf_exported_successfully)
    val msgFailedToSavePdf = stringResource(R.string.msg_failed_to_save_pdf)
    val msgGeneratingPdf = stringResource(R.string.msg_generating_pdf)
    val msgSignInToSave = stringResource(R.string.msg_sign_in_to_save)

    val (showSaveDialog, setShowSaveDialog) = remember { mutableStateOf(false) }
    val (showExportDialog, setShowExportDialog) = remember { mutableStateOf(false) }

    var currentArea by remember { mutableDoubleStateOf(0.0) }
    var currentPerimeter by remember { mutableDoubleStateOf(0.0) }

    var exportName by remember { mutableStateOf("") }
    var pendingPdfFile by remember { mutableStateOf<File?>(null) }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null && pendingPdfFile != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)
                        ?.use { out ->
                            pendingPdfFile!!.inputStream()
                                .use { input ->
                                    input.copyTo(out)
                                }
                        }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, msgPdfExportedSuccessfully, Toast.LENGTH_SHORT).show()
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, msgFailedToSavePdf, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    var currentMapStyleId by rememberSaveable { mutableStateOf("satellite_streets") }
    var showStyleSheet by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val currentStyleOption =
        getAvailableMapStyles().find { it.id == currentMapStyleId } ?: getAvailableMapStyles().first()
    val currentStyleUri =
        if (isDarkTheme) currentStyleOption.darkStyleUri else currentStyleOption.lightStyleUri

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val insetsModifier = if (isLandscape) {
        Modifier.windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
    } else {
        Modifier
    }

    // --- Expand More Timer Logic ---
    var isTopMenuExpanded by remember { mutableStateOf(true) }
    val (lastInteractionTime, setLastInteractionTime) = remember {
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    LaunchedEffect(lastInteractionTime) {
        delay(5000.milliseconds)
        isTopMenuExpanded = true
    }

    val handleMapInteract = {
        setLastInteractionTime(System.currentTimeMillis())
        isTopMenuExpanded = false
    }

    if (showSaveDialog) {
        SaveMeasurementDialog(
            initialName = mapUiState.loadedMeasurementName ?: "",
            onDismiss = { setShowSaveDialog(false) },
            onSave = { name ->
                setShowSaveDialog(false)
                val userId = authState.currentUser?.uid ?: return@SaveMeasurementDialog
                mapViewModel.saveMeasurement(
                    name = name,
                    userId = userId,
                    areaSqMeters = currentArea,
                    perimeterMeters = currentPerimeter
                ) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }

    if (showExportDialog) {
        val currentOptions = mapUiState.pdfExportOptions
        val initialName = mapUiState.loadedMeasurementName ?: currentOptions.name

        ExportPdfDialog(
            initialOptions = currentOptions.copy(name = initialName),
            onDismiss = { setShowExportDialog(false) },
            onExport = { options ->
                mapViewModel.onAction(MapAction.SetPdfExportOptions(options))
                setShowExportDialog(false)
                exportName = options.name
                Toast.makeText(context, msgGeneratingPdf, Toast.LENGTH_SHORT).show()
                scope.launch(Dispatchers.IO) {
                    val pointsToMeasure = when (mapUiState.completedMode ?: mapUiState.activeMode) {
                        CalcMode.PINS -> mapUiState.pinPoints
                        CalcMode.DRAW -> mapUiState.drawPoints
                        CalcMode.SHAPES -> mapUiState.shapePoints
                        else -> emptyList()
                    }
                    val pointDataList =
                        pointsToMeasure.map { PointData(it.latitude(), it.longitude()) }
                    var centerLat = 28.5355
                    var centerLng = 77.3910
                    if (pointsToMeasure.isNotEmpty()) {
                        centerLat = pointsToMeasure.map { it.latitude() }
                            .average()
                        centerLng = pointsToMeasure.map { it.longitude() }
                            .average()
                    } else if (mapViewModel.uiState.value.currentUserLocation != null) {
                        centerLat = mapViewModel.uiState.value.currentUserLocation!!.latitude()
                        centerLng = mapViewModel.uiState.value.currentUserLocation!!.longitude()
                    }

                    val file = PdfGenerator.generatePdf(
                        context = context,
                        options = options,
                        areaMeters = currentArea,
                        perimeterMeters = currentPerimeter,
                        points = pointDataList,
                        centerLat = centerLat,
                        centerLng = centerLng,
                        zoom = 14.0,
                        mapStyle = currentStyleUri
                    )
                    pendingPdfFile = file
                    withContext(Dispatchers.Main) {
                        exportPdfLauncher.launch("${exportName.replace(' ', '_')}.pdf")
                    }
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppSidebar(
                currentRoute = currentRoute,
                authViewModel = authViewModel,
                onMenuSelect = { route ->
                    scope.launch {
                        drawerState.close()
                        onNavigate(route)
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (showStyleSheet) {
                MapStyleBottomSheet(
                    currentStyleId = currentMapStyleId,
                    onStyleSelected = { id ->
                        currentMapStyleId = id
                        showStyleSheet = false
                    },
                    onDismiss = { showStyleSheet = false }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(insetsModifier)
            ) {
                if (isLandscape) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SatMapComponent(
                            modifier = Modifier.fillMaxSize(),
                            currentMapStyle = currentStyleUri,
                            bottomPadding = dimensionResource(id = R.dimen.dimen_50),
                            viewModel = mapViewModel,
                            onMapInteract = handleMapInteract,
                            onMeasurementsUpdated = { area, perimeter ->
                                currentArea = area
                                currentPerimeter = perimeter
                            }
                        )

                        MainTopControls(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onStyleToggle = { showStyleSheet = true },
                            onSearchClick = {
                                onNavigate(SatMesRoutes.SEARCH)
                            },
                            expanded = isTopMenuExpanded,
                            onExpandedChange = { expanded ->
                                isTopMenuExpanded = expanded
                                if (!expanded) {
                                    setLastInteractionTime(System.currentTimeMillis())
                                }
                            }
                        )

                        MainCustomBottomSheet(
                            modifier = Modifier.align(Alignment.BottomStart),
                            peekHeight = landscapePeekHeight,
                            expandedHeightRatio = landscapeExpandedHeightRatio,
                            widthRatio = 0.5f
                        ) { isAtPeekHeight ->
                            MainBottomSheet(
                                modifier = Modifier.fillMaxSize(),
                                areaMeters = currentArea,
                                perimeterMeters = currentPerimeter,
                                isAtPeekHeight = isAtPeekHeight,
                                onSaveClick = {
                                    if (authState.currentUser == null) {
                                        Toast.makeText(context, msgSignInToSave, Toast.LENGTH_SHORT).show()
                                    } else {
                                        setShowSaveDialog(true)
                                    }
                                },
                                onExportClick = {
                                    setShowExportDialog(true)
                                }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SatMapComponent(
                            modifier = Modifier.fillMaxSize(),
                            currentMapStyle = currentStyleUri,
                            bottomPadding = portraitPeekHeight,
                            viewModel = mapViewModel,
                            onMapInteract = handleMapInteract,
                            onMeasurementsUpdated = { area, perimeter ->
                                currentArea = area
                                currentPerimeter = perimeter
                            }
                        )
                        MainTopControls(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onStyleToggle = { showStyleSheet = true },
                            onSearchClick = {
                                onNavigate(SatMesRoutes.SEARCH)
                            },
                            expanded = isTopMenuExpanded,
                            onExpandedChange = { expanded ->
                                isTopMenuExpanded = expanded
                                if (!expanded) {
                                    setLastInteractionTime(System.currentTimeMillis())
                                }
                            }
                        )

                        MainCustomBottomSheet(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            peekHeight = portraitPeekHeight,
                            expandedHeightRatio = portraitExpandedHeightRatio,
                            widthRatio = 1f
                        ) { isAtPeekHeight ->
                            MainBottomSheet(
                                modifier = Modifier.fillMaxSize(),
                                areaMeters = currentArea,
                                perimeterMeters = currentPerimeter,
                                isAtPeekHeight = isAtPeekHeight,
                                onSaveClick = {
                                    if (authState.currentUser == null) {
                                        Toast.makeText(context, msgSignInToSave, Toast.LENGTH_SHORT).show()
                                    } else {
                                        setShowSaveDialog(true)
                                    }
                                },
                                onExportClick = {
                                    setShowExportDialog(true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}