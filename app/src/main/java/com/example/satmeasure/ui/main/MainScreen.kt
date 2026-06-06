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
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import com.example.satmeasure.ui.map.SatMapComponent
import com.example.satmeasure.ui.navigation.SatMesRoutes
import com.example.satmeasure.ui.navigation.AppSidebar
import com.example.satmeasure.ui.navigation.MainTopControls
import com.example.satmeasure.ui.components.MainCustomBottomSheet
import com.example.satmeasure.ui.navigation.MapStyleBottomSheet
import com.example.satmeasure.ui.components.MainBottomSheet
import com.example.satmeasure.ui.navigation.availableMapStyles
import com.example.satmeasure.ui.viewmodel.MapViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    mapViewModel: MapViewModel,
    portraitPeekHeight: Dp = 120.dp,
    portraitExpandedHeightRatio: Float = 0.5f,
    landscapePeekHeight: Dp = 100.dp,
    landscapeExpandedHeightRatio: Float = 0.93f
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentArea by remember { mutableDoubleStateOf(0.0) }
    var currentPerimeter by remember { mutableDoubleStateOf(0.0) }

    var currentMapStyleId by rememberSaveable { mutableStateOf("satellite_streets") }
    var showStyleSheet by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val currentStyleOption = availableMapStyles.find { it.id == currentMapStyleId } ?: availableMapStyles.first()
    val currentStyleUri = if (isDarkTheme) currentStyleOption.darkStyleUri else currentStyleOption.lightStyleUri

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val insetsModifier = if (isLandscape) {
        Modifier.windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
    } else {
        Modifier
    }

    // --- Expand More Timer Logic ---
    var isTopMenuExpanded by remember { mutableStateOf(true) }
    val (lastInteractionTime, setLastInteractionTime) = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastInteractionTime) {
        delay(5000)
        isTopMenuExpanded = true
    }

    val handleMapInteract = {
        setLastInteractionTime(System.currentTimeMillis())
        isTopMenuExpanded = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppSidebar(
                currentRoute = currentRoute,
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
            // Map Style Bottom Sheet
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
                            bottomPadding = 50.dp,
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

                        // Custom Floating "Bottom Sheet" on the Left
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
                                isAtPeekHeight = isAtPeekHeight
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
                                isAtPeekHeight = isAtPeekHeight
                            )
                        }
                    }
                }
            }
        }
    }
}