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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.satmeasure.ui.map.models.CalcMode
import com.example.satmeasure.ui.map.models.ShapeType
import com.example.satmeasure.ui.components.WipeWarningDialog

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
    onBackRequest: () -> Unit
) {
    val currentState = when {
        activeMode != null -> OverlayState.EXPANDED
        completedMode != null -> OverlayState.COMPLETED
        isExpanded -> OverlayState.EXPANDED
        else -> OverlayState.COLLAPSED
    }

    var showWipeDialog by remember { mutableStateOf(false) }
    var wipeAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    if (showWipeDialog) {
        WipeWarningDialog(
            onDismiss = { showWipeDialog = false },
            onConfirm = { 
                wipeAction?.invoke()
                showWipeDialog = false 
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
                    onClick = onExpandToggle,
                    icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
                    text = { Text("Calculate Area", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 170.dp, height = 56.dp)
                )
            }
            OverlayState.COMPLETED -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(width = 170.dp, height = 56.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onActiveModeChange(completedMode) },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Edit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        VerticalDivider(Modifier.height(24.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.2f))

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onClearAll() },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text("Clear", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            OverlayState.EXPANDED -> {
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        val modes = listOf(
                                            Triple(CalcMode.SHAPES, Icons.Default.CropSquare, "Shapes"),
                                            Triple(CalcMode.DRAW, Icons.Default.Draw, "Draw"),
                                            Triple(CalcMode.PINS, Icons.Default.LocationOn, "Pins")
                                        )
                                        modes.forEach { (m, icon, label) ->
                                            Surface(
                                                onClick = { onActiveModeChange(m) },
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
                                        onActiveModeChange(null)
                                        onExpandToggle()
                                    }, modifier = Modifier.size(48.dp)) {
                                        Icon(Icons.Default.Clear, "Close")
                                    }
                                } else {
                                    // LEVEL 2: SUB-MENU
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        when (mode) {
                                            CalcMode.PINS -> {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Button(
                                                    onClick = { if (connectTargetIndex != null) onConnect() },
                                                    enabled = connectTargetIndex != null,
                                                    modifier = Modifier.height(64.dp).widthIn(min = 48.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)),

                                                    ) {
                                                    if (connectTargetIndex != null) {
                                                        Text(buildAnnotatedString {
                                                            append("Connect\n")
                                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)) {
                                                                append("Pin $connectTargetIndex")
                                                            }
                                                        }, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                                    } else {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Text("Connect", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                                                            Text("Pins", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                                                        }
                                                    }
                                                }

                                                Button(
                                                    onClick = { onDropPin() },
                                                    modifier = Modifier.height(64.dp).width(54.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(24.dp))
                                                        Text("Drop", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                                                    }
                                                }

                                                VerticalDivider(Modifier.height(48.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    OutlinedButton(
                                                        onClick = { onUndoPin() },
                                                        modifier = Modifier.height(64.dp).width(45.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(24.dp))
                                                            Text("Undo", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = { onRedoPin() },
                                                        modifier = Modifier.height(64.dp).width(45.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.AutoMirrored.Filled.Redo, null, modifier = Modifier.size(24.dp))
                                                            Text("Redo", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                    
                                                    OutlinedButton(
                                                        onClick = { wipeAction = onClearPins; showWipeDialog = true },
                                                        enabled = hasPins,
                                                        modifier = Modifier.height(64.dp).width(45.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.Default.Delete, null, tint = if (hasPins) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), modifier = Modifier.size(24.dp))
                                                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = { 
                                                            onCompletedModeChange(mode)
                                                            onActiveModeChange(null)
                                                        },
                                                        enabled = hasPins,
                                                        modifier = Modifier.height(64.dp).width(45.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(24.dp))
                                                            Text("Done", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        CalcMode.SHAPES -> {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (!isShapeDropped) {
                                                    ShapeIconButton(Icons.Default.Hexagon, selectedShape == ShapeType.HEXAGON, modifier = Modifier.height(64.dp).width(45.dp)) { onSelectedShapeChange(ShapeType.HEXAGON) }
                                                    ShapeIconButton(Icons.Default.CropSquare, selectedShape == ShapeType.SQUARE, modifier = Modifier.height(64.dp).width(45.dp)) { onSelectedShapeChange(ShapeType.SQUARE) }
                                                    ShapeIconButton(Icons.Default.Circle, selectedShape == ShapeType.CIRCLE, modifier = Modifier.height(64.dp).width(45.dp)) { onSelectedShapeChange(ShapeType.CIRCLE) }
                                                    ShapeIconButton(Icons.Default.ChangeHistory, selectedShape == ShapeType.TRIANGLE, modifier = Modifier.height(64.dp).width(45.dp)) { onSelectedShapeChange(ShapeType.TRIANGLE) }
                                                    
                                                    VerticalDivider(Modifier.height(48.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    
                                                    Button(
                                                        onClick = { onDropShape() },
                                                        modifier = Modifier.height(64.dp).width(56.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.Default.AddLocation, null, modifier = Modifier.size(24.dp))
                                                            Text("Drop", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                } else {
                                                    // Shape Dropped State
                                                    OutlinedButton(
                                                        onClick = { onUndoShape() },
                                                        enabled = canUndoShape,
                                                        modifier = Modifier.height(64.dp).width(45.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(24.dp))
                                                            Text("Undo", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }

                                                    OutlinedButton(
                                                        onClick = { onRedoShape() },
                                                        enabled = canRedoShape,
                                                        modifier = Modifier.height(64.dp).width(45.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.AutoMirrored.Filled.Redo, null, modifier = Modifier.size(24.dp))
                                                            Text("Redo", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                    OutlinedButton(
                                                        onClick = { wipeAction = onClearShape; showWipeDialog = true },
                                                        enabled = isShapeDropped,
                                                        modifier = Modifier.height(64.dp).width(52.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.Default.Delete, null, tint = if (isShapeDropped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), modifier = Modifier.size(24.dp))
                                                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                    
                                                    OutlinedButton(
                                                        onClick = { 
                                                            onCompletedModeChange(mode)
                                                            onActiveModeChange(null)
                                                        },
                                                        enabled = isShapeDropped,
                                                        modifier = Modifier.height(64.dp).width(52.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(24.dp))
                                                            Text("Done", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        CalcMode.DRAW -> {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { onToggleDrawing() },
                                                    modifier = Modifier.height(64.dp).width(75.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isDrawing) MaterialTheme.colorScheme.errorContainer else ButtonDefaults.buttonColors().containerColor,
                                                        contentColor = if (isDrawing) MaterialTheme.colorScheme.onErrorContainer else ButtonDefaults.buttonColors().contentColor
                                                    ),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(if (isDrawing) Icons.Default.Lock else Icons.Default.LockOpen, null, modifier = Modifier.size(24.dp))
                                                        Text(if (isDrawing) "Locked" else "Lock to Draw", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { onUndoDraw() },
                                                    enabled = canUndoDraw,
                                                    modifier = Modifier.height(64.dp).width(45.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(24.dp))
                                                        Text("Undo", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { onRedoDraw() },
                                                    enabled = canRedoDraw,
                                                    modifier = Modifier.height(64.dp).width(45.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.AutoMirrored.Filled.Redo, null, modifier = Modifier.size(24.dp))
                                                        Text("Redo", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { wipeAction = onClearDrawing; showWipeDialog = true },
                                                    enabled = hasDrawing,
                                                    modifier = Modifier.height(64.dp).width(52.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.Delete, null, tint = if (hasDrawing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), modifier = Modifier.size(24.dp))
                                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                
                                                OutlinedButton(
                                                        onClick = { 
                                                            if (isDrawing) onToggleDrawing()
                                                            onCompletedModeChange(mode)
                                                            onActiveModeChange(null)
                                                        },
                                                    enabled = hasDrawing,
                                                    modifier = Modifier.height(64.dp).width(52.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(24.dp))
                                                        Text("Done", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    VerticalDivider(Modifier.height(48.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    IconButton(
                                        onClick = onBackRequest,
                                        modifier = Modifier.size(48.dp)
                                    ) {
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

