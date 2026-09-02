package com.example.satmeasure.ui.components.bars

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.satmeasure.R
import com.example.satmeasure.utils.HapticHelper

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
