package com.example.satmeasure.ui.navigation

import com.example.satmeasure.utils.HapticHelper

import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import android.content.Intent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.satmeasure.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import com.mapbox.maps.Style
import com.example.satmeasure.ui.map.models.MapStyleOption
import com.example.satmeasure.ui.viewmodel.AuthViewModel
import coil.compose.AsyncImage

@Composable
fun getAvailableMapStyles() = listOf(
    MapStyleOption(
        "satellite_streets", stringResource(id = R.string.satellite_streets), Style.SATELLITE_STREETS,
        Style.SATELLITE_STREETS, R.drawable.satellite_map
    ),
    MapStyleOption(
        "standard", stringResource(id = R.string.standard_navigation), Style.STANDARD, Style.STANDARD, R.drawable.standard_map
    ),
    MapStyleOption(
        "outdoors", stringResource(id = R.string.terrain_outdoors), Style.OUTDOORS, Style.OUTDOORS, R.drawable.terrain_map
    ),
    MapStyleOption(
        "satellite", stringResource(id = R.string.pure_satellite), Style.STANDARD_SATELLITE, Style.SATELLITE,
        R.drawable.satellite_map
    )
)


@Composable
fun MainTopControls(
    onMenuClick: () -> Unit,
    onStyleToggle: () -> Unit,
    onSearchClick: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Smoothly rotates ExpandMore based on orientation and state
    val context = LocalContext.current
    val targetRotation = if (isLandscape) {
        if (expanded) 270f else 90f
    } else {
        if (expanded) 180f else 0f
    }

    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(dimensionResource(id = R.dimen.text_lg))
    ) {
        // --- LEFT: Hamburger Menu ---
        FloatingActionButton(
            onClick = {
                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                onMenuClick()
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(dimensionResource(id = R.dimen.button_height))
        ) {
            Icon(Icons.Default.Menu, contentDescription = stringResource(id = R.string.open_menu))
        }

        // --- RIGHT: Expandable Action Button ---
        if (isLandscape) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = expanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.text_sm))
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                onExpandedChange(false)
                                onSearchClick()
                            },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_10)),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.search_location))
                        }

                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.text_sm)))

                        SmallFloatingActionButton(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                onExpandedChange(false)
                                onStyleToggle()
                            },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_10)),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Default.Map, contentDescription = stringResource(id = R.string.toggle_style))
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onExpandedChange(!expanded)
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.button_height))
                ) {
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = stringResource(id = R.string.expand_options),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onExpandedChange(!expanded)
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.button_height))
                ) {
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = stringResource(id = R.string.expand_options),
                        modifier = Modifier.rotate(rotation)
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.text_sm))
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                onExpandedChange(false)
                                onSearchClick()
                            },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_10)),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.search_location))
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))

                        SmallFloatingActionButton(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                onExpandedChange(false)
                                onStyleToggle()
                            },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_10)),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Default.Map, contentDescription = stringResource(id = R.string.toggle_style))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppSidebar(
    currentRoute: String,
    authViewModel: AuthViewModel,
    onMenuSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG)
                .show()
            authViewModel.clearError()
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
                Spacer(modifier = Modifier.height(if (isLandscape) dimensionResource(id = R.dimen.spacing_xs) else dimensionResource(id = R.dimen.text_lg)))

                val titlePadding = if (isLandscape) {
                    Modifier.padding(start = dimensionResource(id = R.dimen.dimen_22), end = dimensionResource(id = R.dimen.dimen_22), top = dimensionResource(id = R.dimen.dimen_10), bottom = dimensionResource(id = R.dimen.dimen_10))
                } else {
                    Modifier.padding(start = dimensionResource(id = R.dimen.dimen_22), end = dimensionResource(id = R.dimen.dimen_22), top = dimensionResource(id = R.dimen.dimen_0), bottom = dimensionResource(id = R.dimen.text_lg))
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
                    Modifier.padding(horizontal = dimensionResource(id = R.dimen.text_lg), vertical = dimensionResource(id = R.dimen.corner_sm))
                } else {
                    Modifier.padding(horizontal = dimensionResource(id = R.dimen.text_lg), vertical = dimensionResource(id = R.dimen.text_lg))
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(profilePadding)
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.text_lg))),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_lg))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(id = R.dimen.text_lg), vertical = dimensionResource(id = R.dimen.text_sm))
                    ) {
                        // Sleek Row Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.icon_xl))
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (authState.currentUser?.photoUrl != null) {
                                    AsyncImage(
                                        model = authState.currentUser?.photoUrl,
                                        contentDescription = stringResource(id = R.string.profile_picture),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (authState.currentUser != null) {
                                    val name = authState.currentUser?.displayName
                                        ?: authState.currentUser?.email ?: "U"
                                    val initial = name.take(1)
                                        .uppercase()
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = stringResource(id = R.string.profile_picture),
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxxl)),
                                        tint = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.text_lg)))
                            Column(modifier = Modifier.weight(1f)) {
                                if (authState.currentUser != null) {
                                    Text(
                                        text = authState.currentUser?.displayName ?: "User",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xxs)))
                                    Text(
                                        text = authState.currentUser?.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        maxLines = 1
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

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))

                        // Auth Button
                        OutlinedButton(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                if (authState.currentUser == null) {
                                    authViewModel.signInWithGoogle(context)
                                } else {
                                    authViewModel.signOut()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(id = R.dimen.dimen_40)),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                            border = BorderStroke(dimensionResource(id = R.dimen.dimen_1), MaterialTheme.colorScheme.tertiary),
                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0)),
                            colors = outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxl)),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    strokeWidth = dimensionResource(id = R.dimen.spacing_xxs)
                                )
                            } else if (authState.currentUser == null) {
                                Text(
                                    "G", fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.corner_sm))
                                )
                                Text(stringResource(id = R.string.action_sign_in_with_google))
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.text_xl))
                                        .padding(end = dimensionResource(id = R.dimen.spacing_xs))
                                )
                                Text(stringResource(id = R.string.action_logout))
                            }
                        }
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(if (isLandscape) dimensionResource(id = R.dimen.spacing_xs) else dimensionResource(id = R.dimen.corner_sm)))

                val itemPadding = if (isLandscape) {
                    Modifier.padding(horizontal = dimensionResource(id = R.dimen.corner_sm), vertical = dimensionResource(id = R.dimen.dimen_0))
                } else {
                    Modifier.padding(horizontal = dimensionResource(id = R.dimen.corner_sm), vertical = dimensionResource(id = R.dimen.spacing_xxs))
                }

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_saved_areas),
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_sm))
                        )
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.HISTORY,
                    onClick = { onMenuSelect(SatMesRoutes.HISTORY) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_tutorial),
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_sm))
                        )
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.TUTORIAL,
                    onClick = { onMenuSelect(SatMesRoutes.TUTORIAL) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_about_us),
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_sm))
                        )
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.ABOUT_US,
                    onClick = { onMenuSelect(SatMesRoutes.ABOUT_US) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                val shareMessage =
                    stringResource(id = R.string.share_message, "https://satmeasure.web.app/share")
                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_share),
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_sm))
                        )
                    },
                    icon = { Icon(Icons.Default.Share, contentDescription = null) },
                    selected = false,
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    colors = itemColors,
                    modifier = itemPadding
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(if (isLandscape) dimensionResource(id = R.dimen.spacing_xs) else dimensionResource(id = R.dimen.corner_sm)))

            NavigationDrawerItem(
                label = {
                    Text(
                        stringResource(id = R.string.title_settings),
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_sm))
                    )
                },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = currentRoute == SatMesRoutes.SETTINGS,
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                    onMenuSelect(SatMesRoutes.SETTINGS)
                }, // Smooth navigation applied
                colors = itemColors,
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.corner_sm),
                    end = dimensionResource(id = R.dimen.corner_sm),
                    bottom = if (isLandscape) dimensionResource(id = R.dimen.spacing_xs) else dimensionResource(id = R.dimen.text_lg),
                    top = if (isLandscape) dimensionResource(id = R.dimen.dimen_0) else dimensionResource(id = R.dimen.spacing_xs)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapStyleBottomSheet(
    currentStyleId: String,
    onStyleSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val chunkCount = if (isLandscape) 4 else 3

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = dimensionResource(id = R.dimen.text_lg), topEnd = dimensionResource(id = R.dimen.text_lg)),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dimensionResource(id = R.dimen.text_lg), end = dimensionResource(id = R.dimen.text_lg), bottom = dimensionResource(id = R.dimen.text_lg), top = dimensionResource(id = R.dimen.corner_sm))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xs)))
            Box(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.dimen_40))
                    .height(dimensionResource(id = R.dimen.spacing_xs))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.map_type),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

            // Grid
            val chunkedStyles = getAvailableMapStyles().chunked(chunkCount)
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.text_lg))) {
                chunkedStyles.forEach { rowStyles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.icon_lg))
                    ) {
                        rowStyles.forEach { styleOpt ->
                            val isSelected = currentStyleId == styleOpt.id
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    onClick = { onStyleSelected(styleOpt.id) },
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                                    color = Color.Transparent,
                                    border = if (isSelected) BorderStroke(
                                        dimensionResource(id = R.dimen.spacing_xxs), MaterialTheme.colorScheme.primary
                                    ) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimensionResource(id = R.dimen.dimen_130))
                                ) {
                                    Image(
                                        painter = painterResource(id = styleOpt.imageRes),
                                        contentDescription = styleOpt.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.corner_sm)))
                                Text(
                                    text = styleOpt.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        repeat(chunkCount - rowStyles.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(if (isLandscape) dimensionResource(id = R.dimen.spacing_xs) else dimensionResource(id = R.dimen.text_xxxl)))
        }
    }
}
