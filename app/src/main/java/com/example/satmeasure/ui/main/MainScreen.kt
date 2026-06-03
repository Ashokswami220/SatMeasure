package com.example.satmeasure.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import com.example.satmeasure.ui.map.SatMapComponent
import com.example.satmeasure.ui.navigation.SatMesRoutes
import com.example.satmeasure.ui.navigation.SatMesSidebar
import com.example.satmeasure.ui.navigation.SatMesTopControls
import com.example.satmeasure.ui.navigation.SatMesBottomSheet
import com.example.satmeasure.ui.navigation.SatMesBottomSheetLandscape
import com.mapbox.maps.Style
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    portraitPeekHeight: Dp = 120.dp,
    landscapePeekHeight: Dp = 100.dp,
    landscapeExpandedHeightRatio: Float = 0.93f
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentMapStyle by remember { mutableStateOf(Style.SATELLITE_STREETS) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val insetsModifier = if (isLandscape) {
        Modifier.windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
    } else {
        Modifier
    }

    // --- Expand More Timer Logic ---
    var isTopMenuExpanded by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastInteractionTime) {
        kotlinx.coroutines.delay(5000)
        isTopMenuExpanded = true
    }

    val handleMapInteract = {
        lastInteractionTime = System.currentTimeMillis()
        isTopMenuExpanded = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            SatMesSidebar(
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(insetsModifier)
            ) {
                if (isLandscape) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SatMapComponent(
                            modifier = Modifier.fillMaxSize(),
                            currentMapStyle = currentMapStyle,
                            onMapInteract = handleMapInteract
                        )

                        SatMesTopControls(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onStyleToggle = {
                                currentMapStyle =
                                    if (currentMapStyle == Style.SATELLITE_STREETS) Style.STANDARD else Style.SATELLITE_STREETS
                            },
                            onSearchClick = {
                                onNavigate(SatMesRoutes.SEARCH)
                            },
                            expanded = isTopMenuExpanded,
                            onExpandedChange = { expanded ->
                                isTopMenuExpanded = expanded
                                if (!expanded) {
                                    lastInteractionTime = System.currentTimeMillis()
                                }
                            }
                        )

                        // Custom Floating "Bottom Sheet" on the Left
                        SatMesBottomSheetLandscape(
                            modifier = Modifier.align(Alignment.BottomStart),
                            peekHeight = landscapePeekHeight,
                            expandedHeightRatio = landscapeExpandedHeightRatio
                        ) {
                            SatMesBottomSheet(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    val sheetState = rememberStandardBottomSheetState(
                        initialValue = SheetValue.PartiallyExpanded
                    )
                    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = portraitPeekHeight,
                        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                        sheetContainerColor = MaterialTheme.colorScheme.surface,
                        sheetDragHandle = null,
                        sheetContent = {
                            SatMesBottomSheet(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.5f)
                            )
                        }
                    ) { _ ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            SatMapComponent(
                                modifier = Modifier.fillMaxSize(),
                                currentMapStyle = currentMapStyle,
                                bottomPadding = portraitPeekHeight,
                                onMapInteract = handleMapInteract
                            )
                            SatMesTopControls(
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onStyleToggle = {
                                    currentMapStyle =
                                        if (currentMapStyle == Style.SATELLITE_STREETS) {
                                            Style.STANDARD
                                        } else {
                                            Style.SATELLITE_STREETS
                                        }
                                },
                                onSearchClick = {
                                    onNavigate(SatMesRoutes.SEARCH)
                                },
                                expanded = isTopMenuExpanded,
                                onExpandedChange = { expanded ->
                                    isTopMenuExpanded = expanded
                                    if (!expanded) {
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}