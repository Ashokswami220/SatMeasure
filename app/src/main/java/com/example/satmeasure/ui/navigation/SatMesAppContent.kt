package com.example.satmeasure.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.rotate
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.unit.Dp


@Composable
fun SatMesTopControls(
    onMenuClick: () -> Unit,
    onStyleToggle: () -> Unit,
    onSearchClick: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {

    // Smoothly rotates ExpandMore to point UP when opened
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        // --- LEFT: Hamburger Menu ---
        FloatingActionButton(
            onClick = onMenuClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(52.dp)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
        }

        // --- RIGHT: Expandable Action Button ---
        Column(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = { onExpandedChange(!expanded) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = "Expand Options",
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            onExpandedChange(false)
                            onSearchClick()
                        },
                        shape = RoundedCornerShape(10.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search Location")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SmallFloatingActionButton(
                        onClick = {
                            onExpandedChange(false)
                            onStyleToggle()
                        },
                        shape = RoundedCornerShape(10.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Toggle Style")
                    }
                }
            }
        }
    }
}

@Composable
fun SatMesSidebar(
    currentRoute: String,
    onMenuSelect: (String) -> Unit
) {
    var loginState by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(loginState) {
        if (loginState == 1) {
            kotlinx.coroutines.delay(1500)
            loginState = 2
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val widthFraction = if (isLandscape) 0.4f else 0.65f

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(widthFraction)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            val itemColors = NavigationDrawerItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 16.dp))

                val titlePadding = if (isLandscape) {
                    Modifier.padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 10.dp)
                } else {
                    Modifier.padding(start = 22.dp, end = 22.dp, top = 0.dp, bottom = 16.dp)
                }

                Text(
                    text = "SatMeasure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = titlePadding
                )

                HorizontalDivider()

                // Profile Section (Alive and Premium)
                val profilePadding = if (isLandscape) {
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                } else {
                    Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(profilePadding)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Sleek Row Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (loginState == 2) {
                                    Text(
                                        text = "Ashok Swami",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "developer@gmail.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                } else {
                                    Text(
                                        text = "••••••••••",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Auth Button
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                if (loginState == 0) loginState = 1
                                else if (loginState == 2) loginState = 0
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            when (loginState) {
                                0 -> {
                                    Text("G", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                    Text("Sign in with Google")
                                }
                                1 -> {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.tertiary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Loading...")
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Log Out",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Log Out")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

                val itemPadding = if (isLandscape) {
                    Modifier.padding(horizontal = 8.dp, vertical = 0.dp)
                } else {
                    Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                }

                NavigationDrawerItem(
                    label = { Text("Saved Areas", modifier = Modifier.padding(start = 12.dp)) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.HISTORY,
                    onClick = { onMenuSelect(SatMesRoutes.HISTORY) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                NavigationDrawerItem(
                    label = { Text("Tutorial", modifier = Modifier.padding(start = 12.dp)) },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.TUTORIAL,
                    onClick = { onMenuSelect(SatMesRoutes.TUTORIAL) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                NavigationDrawerItem(
                    label = { Text("About Us", modifier = Modifier.padding(start = 12.dp)) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.ABOUT_US,
                    onClick = { onMenuSelect(SatMesRoutes.ABOUT_US) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

            NavigationDrawerItem(
                label = { Text("Settings", modifier = Modifier.padding(start = 12.dp)) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = currentRoute == SatMesRoutes.SETTINGS,
                onClick = { onMenuSelect(SatMesRoutes.SETTINGS) }, // Smooth navigation applied
                colors = itemColors,
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = if (isLandscape) 4.dp else 16.dp,
                    top = if (isLandscape) 0.dp else 4.dp
                )
            )
        }
    }
}

@Composable
fun SatMesBottomSheet(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Map Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                Modifier,
                thickness = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun SatMesBottomSheetLandscape(
    modifier: Modifier = Modifier,
    peekHeight: Dp = 110.dp,
    expandedHeightRatio: Float = 0.93f,
    content: @Composable () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val expandedHeight = screenHeight * expandedHeightRatio
    
    val density = LocalDensity.current
    val peekHeightPx = with(density) { peekHeight.toPx() }
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    
    val heightAnimatable = remember { Animatable(peekHeightPx) }
    val coroutineScope = rememberCoroutineScope()

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            val newHeight = heightAnimatable.value - delta
            heightAnimatable.snapTo(newHeight.coerceIn(peekHeightPx, expandedHeightPx))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.5f)
            .height(with(density) { heightAnimatable.value.toDp() })
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val targetHeight = if (velocity < -500f) {
                            expandedHeightPx
                        } else if (velocity > 500f) {
                            peekHeightPx
                        } else {
                            val midPoint = (peekHeightPx + expandedHeightPx) / 2
                            if (heightAnimatable.value > midPoint) expandedHeightPx else peekHeightPx
                        }
                        
                        heightAnimatable.animateTo(
                            targetValue = targetHeight,
                            initialVelocity = -velocity,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sheet Content
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}