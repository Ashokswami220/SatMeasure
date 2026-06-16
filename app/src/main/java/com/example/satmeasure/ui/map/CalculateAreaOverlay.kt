package com.example.satmeasure.ui.map

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.satmeasure.R
import com.example.satmeasure.ui.map.models.CalcMode
import com.example.satmeasure.ui.map.models.ShapeType
import com.example.satmeasure.ui.components.WipeWarningDialog
import com.example.satmeasure.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext

enum class OverlayState {
    COLLAPSED,
    COMPLETED,
    EXPANDED
}

@Composable
fun CalculateAreaOverlay(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    activeMode: CalcMode?,
    onActiveModeChange: (CalcMode?) -> Unit,
    completedMode: CalcMode?,
    onCompletedModeChange: (CalcMode?) -> Unit,
    onClearAll: () -> Unit,
    selectedShape: ShapeType,
    onSelectedShapeChange: (ShapeType) -> Unit,
    isShapeDropped: Boolean,
    onDropShape: () -> Unit,
    onClearShape: () -> Unit,
    isDrawing: Boolean,
    onToggleDrawing: () -> Unit,
    onClearDrawing: () -> Unit,
    onUndoPin: () -> Unit,
    onRedoPin: () -> Unit,
    onDropPin: () -> Unit,
    onClearPins: () -> Unit = {},
    hasPins: Boolean = false,
    onUndoShape: () -> Unit = {},
    onRedoShape: () -> Unit = {},
    canUndoShape: Boolean = false,
    canRedoShape: Boolean = false,
    onUndoDraw: () -> Unit = {},
    onRedoDraw: () -> Unit = {},
    canUndoDraw: Boolean = false,
    canRedoDraw: Boolean = false,
    connectTargetIndex: Int?,
    onConnect: () -> Unit,
    hasDrawing: Boolean = false,
    onBackRequest: () -> Unit,
    isReadOnly: Boolean = false,
    onDoneReadOnly: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentState = when {
        activeMode != null -> OverlayState.EXPANDED
        completedMode != null -> OverlayState.COMPLETED
        isExpanded -> OverlayState.EXPANDED
        else -> OverlayState.COLLAPSED
    }

    val (showWipeDialog, setShowWipeDialog) = remember { mutableStateOf(false) }
    val (wipeAction, setWipeAction) = remember { mutableStateOf<(() -> Unit)?>(null) }

    if (showWipeDialog) {
        WipeWarningDialog(
            onDismiss = { setShowWipeDialog(false) },
            onConfirm = {
                wipeAction?.invoke()
                setShowWipeDialog(false)
            }
        )
    }

    AnimatedContent(
        targetState = currentState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Calculate UI Transition"
    ) { state ->
        when (state) {
            OverlayState.COLLAPSED -> {
                ExtendedFloatingActionButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                        onExpandToggle()
                    },
                    icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
                    text = {
                        Text(
                            stringResource(id = R.string.action_calculate_area),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_md)),
                    modifier = Modifier.size(width = dimensionResource(id = R.dimen.dimen_170), height = dimensionResource(id = R.dimen.spacing_xxxl))
                )
            }

            OverlayState.COMPLETED -> {
                if (isReadOnly) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            onDoneReadOnly()
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                stringResource(id = R.string.action_done),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_md)),
                        modifier = Modifier.size(width = dimensionResource(id = R.dimen.dimen_170), height = dimensionResource(id = R.dimen.spacing_xxxl))
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shadowElevation = dimensionResource(id = R.dimen.spacing_sm_minus),
                        modifier = Modifier.size(width = dimensionResource(id = R.dimen.dimen_170), height = dimensionResource(id = R.dimen.spacing_xxxl))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = dimensionResource(id = R.dimen.spacing_xs)),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                        onActiveModeChange(completedMode)
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(id = R.string.action_edit),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(dimensionResource(id = R.dimen.spacing_sm)))
                                Text(
                                    stringResource(id = R.string.action_edit),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            VerticalDivider(
                                Modifier.height(dimensionResource(id = R.dimen.text_xxxl)),
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.2f
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                        onClearAll()
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete, contentDescription = stringResource(
                                        id = R.string.action_clear_all
                                    ), tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(dimensionResource(id = R.dimen.spacing_sm)))
                                Text(
                                    stringResource(id = R.string.action_clear_all),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            OverlayState.EXPANDED -> {
                Card(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_lg)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.spacing_sm_minus))
                ) {
                    Row(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.corner_sm)),
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
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            dimensionResource(id = R.dimen.spacing_md_minus)
                                        )
                                    ) {
                                        val modes = listOf(
                                            Triple(
                                                CalcMode.SHAPES, Icons.Default.CropSquare,
                                                stringResource(id = R.string.label_shapes)
                                            ),
                                            Triple(
                                                CalcMode.DRAW, Icons.Default.Draw,
                                                stringResource(id = R.string.label_draw)
                                            ),
                                            Triple(
                                                CalcMode.PINS, Icons.Default.LocationOn,
                                                stringResource(id = R.string.label_pins)
                                            )
                                        )
                                        modes.forEach { (m, icon, label) ->
                                            Surface(
                                                onClick = {
                                                    HapticHelper.trigger(
                                                        context, HapticHelper.Type.MEDIUM
                                                    )
                                                    onActiveModeChange(m)
                                                },
                                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                                                color = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_huge))
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        icon, contentDescription = label,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(dimensionResource(id = R.dimen.spacing_xs)))
                                                    Text(
                                                        label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.text_lg)))
                                    VerticalDivider(
                                        Modifier.height(dimensionResource(id = R.dimen.icon_xl)), DividerDefaults.Thickness,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.corner_sm)))

                                    // CLOSE BUTTON AT RIGHT
                                    IconButton(
                                        onClick = {
                                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                            onActiveModeChange(null)
                                            onExpandToggle()
                                        }, modifier = Modifier.size(
                                            dimensionResource(id = R.dimen.icon_xl)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            stringResource(id = R.string.cd_close)
                                        )
                                    }
                                } else {
                                    // LEVEL 2: SUB-MENU
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        when (mode) {
                                            CalcMode.PINS -> {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        dimensionResource(id = R.dimen.corner_sm)
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            if (connectTargetIndex != null) onConnect()
                                                        },
                                                        enabled = connectTargetIndex != null,
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .widthIn(min = dimensionResource(id = R.dimen.icon_xl)),
                                                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                                        ),
                                                        contentPadding = PaddingValues(
                                                            horizontal = dimensionResource(id = R.dimen.spacing_xs)
                                                        ),
                                                        border = BorderStroke(
                                                            dimensionResource(id = R.dimen.dimen_1),
                                                            MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.38f
                                                            )
                                                        ),

                                                        ) {
                                                        if (connectTargetIndex != null) {
                                                            Text(
                                                                buildAnnotatedString {
                                                                    append(
                                                                        stringResource(
                                                                            id = R.string.action_connect
                                                                        ) + "\n"
                                                                    )
                                                                    withStyle(
                                                                        SpanStyle(
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = MaterialTheme.colorScheme.onTertiary
                                                                        )
                                                                    ) {
                                                                        append(
                                                                            stringResource(
                                                                                id = R.string.label_pin_prefix
                                                                            ) + "$connectTargetIndex"
                                                                        )
                                                                    }
                                                                },
                                                                style = MaterialTheme.typography.labelSmall,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        } else {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_connect
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    textAlign = TextAlign.Center,
                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.label_pins
                                                                    ),
                                                                    style = MaterialTheme.typography.labelMedium,
                                                                    textAlign = TextAlign.Center,
                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Button(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            onDropPin()
                                                        },
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .width(dimensionResource(id = R.dimen.dimen_54)),
                                                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                                                        contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.PushPin, null,
                                                                modifier = Modifier.size(
                                                                    dimensionResource(
                                                                        id = R.dimen.icon_md
                                                                    )
                                                                )
                                                            )
                                                            Text(
                                                                stringResource(
                                                                    id = R.string.action_drop
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                    }

                                                    VerticalDivider(
                                                        Modifier.height(dimensionResource(id = R.dimen.icon_xl)),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.2f
                                                        )
                                                    )

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            dimensionResource(
                                                                id = R.dimen.spacing_xs
                                                            )
                                                        )
                                                    ) {
                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onUndoPin()
                                                            },
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.dimen_45)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.AutoMirrored.Filled.Undo,
                                                                    null, modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_undo
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onRedoPin()
                                                            },
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.dimen_45)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.AutoMirrored.Filled.Redo,
                                                                    null, modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_redo
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                setWipeAction(onClearPins)
                                                                setShowWipeDialog(true)
                                                            },
                                                            enabled = hasPins,
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.dimen_45)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Delete, null,
                                                                    tint = if (hasPins) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                                                        alpha = 0.38f
                                                                    ), modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_clear_all
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onCompletedModeChange(mode)
                                                                onActiveModeChange(null)
                                                            },
                                                            enabled = hasPins,
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.dimen_45)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Check, null,
                                                                    modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_done
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            CalcMode.SHAPES -> {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        dimensionResource(id = R.dimen.text_sm)
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (!isShapeDropped) {
                                                        ShapeIconButton(
                                                            Icons.Default.Hexagon,
                                                            selectedShape == ShapeType.HEXAGON,
                                                            modifier = Modifier
                                                                .height(
                                                                    dimensionResource(id = R.dimen.spacing_huge)
                                                                )
                                                                .width(dimensionResource(id = R.dimen.dimen_45))
                                                        ) {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.LIGHT
                                                            ); onSelectedShapeChange(
                                                            ShapeType.HEXAGON
                                                        )
                                                        }
                                                        ShapeIconButton(
                                                            Icons.Default.CropSquare,
                                                            selectedShape == ShapeType.SQUARE,
                                                            modifier = Modifier
                                                                .height(
                                                                    dimensionResource(id = R.dimen.spacing_huge)
                                                                )
                                                                .width(dimensionResource(id = R.dimen.dimen_45))
                                                        ) {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.LIGHT
                                                            ); onSelectedShapeChange(
                                                            ShapeType.SQUARE
                                                        )
                                                        }
                                                        ShapeIconButton(
                                                            Icons.Default.Circle,
                                                            selectedShape == ShapeType.CIRCLE,
                                                            modifier = Modifier
                                                                .height(
                                                                    dimensionResource(id = R.dimen.spacing_huge)
                                                                )
                                                                .width(dimensionResource(id = R.dimen.dimen_45))
                                                        ) {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.LIGHT
                                                            ); onSelectedShapeChange(
                                                            ShapeType.CIRCLE
                                                        )
                                                        }
                                                        ShapeIconButton(
                                                            Icons.Default.ChangeHistory,
                                                            selectedShape == ShapeType.TRIANGLE,
                                                            modifier = Modifier
                                                                .height(
                                                                    dimensionResource(id = R.dimen.spacing_huge)
                                                                )
                                                                .width(dimensionResource(id = R.dimen.dimen_45))
                                                        ) {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.LIGHT
                                                            ); onSelectedShapeChange(
                                                            ShapeType.TRIANGLE
                                                        )
                                                        }

                                                        VerticalDivider(
                                                            Modifier.height(dimensionResource(id = R.dimen.icon_xl)),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                                alpha = 0.2f
                                                            )
                                                        )

                                                        Button(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onDropShape()
                                                            },
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.spacing_xxxl)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.AddLocation, null,
                                                                    modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_drop
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        // Shape Dropped State
                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onUndoShape()
                                                            },
                                                            enabled = canUndoShape,
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.dimen_45)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.AutoMirrored.Filled.Undo,
                                                                    null, modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_undo
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onRedoShape()
                                                            },
                                                            enabled = canRedoShape,
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.dimen_45)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.AutoMirrored.Filled.Redo,
                                                                    null, modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_redo
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }
                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                setWipeAction(onClearShape)
                                                                setShowWipeDialog(true)
                                                            },
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.button_height)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Delete, null,
                                                                    tint = MaterialTheme.colorScheme.error,
                                                                    modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_clear_all
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                HapticHelper.trigger(
                                                                    context,
                                                                    HapticHelper.Type.MEDIUM
                                                                )
                                                                onCompletedModeChange(mode)
                                                                onActiveModeChange(null)
                                                            },
                                                            modifier = Modifier
                                                                .height(dimensionResource(id = R.dimen.spacing_huge))
                                                                .width(dimensionResource(id = R.dimen.button_height)),
                                                            shape = RoundedCornerShape(
                                                                dimensionResource(
                                                                    id = R.dimen.corner_md
                                                                )
                                                            ),
                                                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                        ) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.Center
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Check, null,
                                                                    modifier = Modifier.size(
                                                                        dimensionResource(
                                                                            id = R.dimen.icon_md
                                                                        )
                                                                    )
                                                                )
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.action_done
                                                                    ),
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            CalcMode.DRAW -> {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        dimensionResource(id = R.dimen.corner_sm)
                                                    )
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            onToggleDrawing()
                                                        },
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .width(dimensionResource(id = R.dimen.dimen_75)),
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.corner_md
                                                            )
                                                        ),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isDrawing) MaterialTheme.colorScheme.errorContainer else ButtonDefaults.buttonColors().containerColor,
                                                            contentColor = if (isDrawing) MaterialTheme.colorScheme.onErrorContainer else ButtonDefaults.buttonColors().contentColor
                                                        ),
                                                        contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                if (isDrawing) Icons.Default.Lock else Icons.Default.LockOpen,
                                                                null, modifier = Modifier.size(
                                                                    dimensionResource(
                                                                        id = R.dimen.icon_md
                                                                    )
                                                                )
                                                            )
                                                            Text(
                                                                if (isDrawing) stringResource(
                                                                    id = R.string.status_locked
                                                                ) else stringResource(
                                                                    id = R.string.status_lock_to_draw
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            onUndoDraw()
                                                        },
                                                        enabled = canUndoDraw,
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .width(dimensionResource(id = R.dimen.dimen_45)),
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.corner_md
                                                            )
                                                        ),
                                                        contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.AutoMirrored.Filled.Undo,
                                                                null, modifier = Modifier.size(
                                                                    dimensionResource(
                                                                        id = R.dimen.icon_md
                                                                    )
                                                                )
                                                            )
                                                            Text(
                                                                stringResource(
                                                                    id = R.string.action_undo
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            onRedoDraw()
                                                        },
                                                        enabled = canRedoDraw,
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .width(dimensionResource(id = R.dimen.dimen_45)),
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.corner_md
                                                            )
                                                        ),
                                                        contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.AutoMirrored.Filled.Redo,
                                                                null, modifier = Modifier.size(
                                                                    dimensionResource(
                                                                        id = R.dimen.icon_md
                                                                    )
                                                                )
                                                            )
                                                            Text(
                                                                stringResource(
                                                                    id = R.string.action_redo
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            setWipeAction(onClearDrawing)
                                                            setShowWipeDialog(true)
                                                        },
                                                        enabled = hasDrawing,
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .width(dimensionResource(id = R.dimen.button_height)),
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.corner_md
                                                            )
                                                        ),
                                                        contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Delete, null,
                                                                tint = if (hasDrawing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.38f
                                                                ), modifier = Modifier.size(
                                                                    dimensionResource(
                                                                        id = R.dimen.icon_md
                                                                    )
                                                                )
                                                            )
                                                            Text(
                                                                stringResource(
                                                                    id = R.string.action_clear_all
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            HapticHelper.trigger(
                                                                context, HapticHelper.Type.MEDIUM
                                                            )
                                                            if (isDrawing) onToggleDrawing()
                                                            onCompletedModeChange(mode)
                                                            onActiveModeChange(null)
                                                        },
                                                        enabled = hasDrawing,
                                                        modifier = Modifier
                                                            .height(dimensionResource(id = R.dimen.spacing_huge))
                                                            .width(dimensionResource(id = R.dimen.button_height)),
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.corner_md
                                                            )
                                                        ),
                                                        contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0))
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Check, null,
                                                                modifier = Modifier.size(
                                                                    dimensionResource(
                                                                        id = R.dimen.icon_md
                                                                    )
                                                                )
                                                            )
                                                            Text(
                                                                stringResource(
                                                                    id = R.string.action_done
                                                                ),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.corner_sm)))
                                        VerticalDivider(
                                            Modifier.height(dimensionResource(id = R.dimen.icon_xl)),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.2f
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_xs)))

                                        IconButton(
                                            onClick = onBackRequest,
                                            modifier = Modifier.size(
                                                dimensionResource(id = R.dimen.icon_xl)
                                            )
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                stringResource(id = R.string.cd_back)
                                            )
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
}

@Composable
fun ShapeIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val iconColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_md)))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = null, tint = iconColor,
            modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_xl))
        )
    }
}