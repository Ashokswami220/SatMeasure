package com.example.satmeasure.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.satmeasure.ui.map.SatMapComponent
import com.example.satmeasure.ui.navigation.SatMesRoutes
import com.example.satmeasure.ui.navigation.SatMesSidebar
import com.example.satmeasure.ui.navigation.SatMesTopControls
import com.mapbox.maps.Style
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentMapStyle by remember { mutableStateOf(Style.SATELLITE_STREETS) }

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
        Box(modifier = Modifier.fillMaxSize()) {
            SatMapComponent(
                modifier = Modifier.fillMaxSize(),
                currentMapStyle = currentMapStyle
            )

            SatMesTopControls(
                onMenuClick = { scope.launch { drawerState.open() } },
                onStyleToggle = {
                    currentMapStyle = if (currentMapStyle == Style.SATELLITE_STREETS) {
                        Style.STANDARD
                    } else {
                        Style.SATELLITE_STREETS
                    }
                },
                onSearchClick = {
                    onNavigate(SatMesRoutes.SEARCH)
                }
            )
        }
    }
}