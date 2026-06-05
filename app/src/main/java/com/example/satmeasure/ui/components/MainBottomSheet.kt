package com.example.satmeasure.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.satmeasure.ui.map.AreaUnit
import com.example.satmeasure.ui.map.MeasurementConverter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSheet(
    modifier: Modifier = Modifier,
    areaMeters: Double = 0.0,
    perimeterMeters: Double = 0.0
) {
    val areaSqFt = areaMeters * 10.7639

    var areaUnit1 by remember { mutableStateOf<AreaUnit>(AreaUnit.SquareMeter) }
    var areaUnit2 by remember { mutableStateOf<AreaUnit>(AreaUnit.Acre) }
    
    var perimeterUnit1 by remember { mutableStateOf("m") }
    var perimeterUnit2 by remember { mutableStateOf("km") }

    var showAreaUnitSelectorFor by remember { mutableStateOf<Int?>(null) } // 1 or 2
    var showPerimeterUnitSelectorFor by remember { mutableStateOf<Int?>(null) } // 1 or 2

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
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Calculation Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Top Headers ---
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text(text = "Area", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(text = "Perimeter", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                // --- 2x2 Grid Section ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MeasurementCard(
                        modifier = Modifier.weight(1f),
                        value = formatValue(MeasurementConverter.convertArea(areaSqFt, areaUnit1)),
                        unit = areaUnit1.displayName,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { showAreaUnitSelectorFor = 1 }
                    )
                    
                    val p1 = when (perimeterUnit1) {
                        "m" -> perimeterMeters
                        "km" -> perimeterMeters / 1000.0
                        "ft" -> perimeterMeters * 3.28084
                        "mi" -> perimeterMeters * 0.000621371
                        else -> perimeterMeters
                    }
                    MeasurementCard(
                        modifier = Modifier.weight(1f),
                        value = formatValue(p1),
                        unit = perimeterUnit1,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { showPerimeterUnitSelectorFor = 1 }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MeasurementCard(
                        modifier = Modifier.weight(1f),
                        value = formatValue(MeasurementConverter.convertArea(areaSqFt, areaUnit2)),
                        unit = areaUnit2.displayName,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { showAreaUnitSelectorFor = 2 }
                    )
                    
                    val p2 = when (perimeterUnit2) {
                        "m" -> perimeterMeters
                        "km" -> perimeterMeters / 1000.0
                        "ft" -> perimeterMeters * 3.28084
                        "mi" -> perimeterMeters * 0.000621371
                        else -> perimeterMeters
                    }
                    MeasurementCard(
                        modifier = Modifier.weight(1f),
                        value = formatValue(p2),
                        unit = perimeterUnit2,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { showPerimeterUnitSelectorFor = 2 }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // --- Bigha Section ---
                Text(
                    text = "Bigha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val bighas = MeasurementConverter.getAllBighaUnits()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bighas.forEach { bigha ->
                        val converted = MeasurementConverter.convertArea(areaSqFt, bigha)
                        ConversionRow3(modifier = Modifier.fillMaxWidth(), stateName = bigha.state.displayName, unitName = "Bigha", value = converted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // --- Other Local Units Section ---
                Text(
                    text = "Other local units",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val locals = MeasurementConverter.getOtherLocalUnits()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    locals.forEach { local ->
                        val converted = MeasurementConverter.convertArea(areaSqFt, local.second)
                        ConversionRow3(modifier = Modifier.fillMaxWidth(), stateName = local.first, unitName = local.second.displayName, value = converted)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Unit Selection Dialogs/Sheets ---
    if (showAreaUnitSelectorFor != null) {
        val currentAreaUnit = if (showAreaUnitSelectorFor == 1) areaUnit1 else areaUnit2
        AreaUnitSelectorSheet(
            currentSelection = currentAreaUnit,
            onDismiss = { showAreaUnitSelectorFor = null },
            onUnitSelected = { selected ->
                if (showAreaUnitSelectorFor == 1) areaUnit1 = selected else areaUnit2 = selected
                showAreaUnitSelectorFor = null
            }
        )
    }

    if (showPerimeterUnitSelectorFor != null) {
        val currentPerimeterUnit = if (showPerimeterUnitSelectorFor == 1) perimeterUnit1 else perimeterUnit2
        CustomAreaUnitSelectorSheet(onDismiss = { showPerimeterUnitSelectorFor = null }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)).align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Perimeter Unit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val perimeterUnits = listOf("m" to "Meter", "km" to "Kilometer", "ft" to "Feet", "mi" to "Miles")
                
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState).simpleVerticalScrollbar(scrollState)) {
                    perimeterUnits.forEach { (symbol, name) ->
                        val isSelected = currentPerimeterUnit == symbol
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (showPerimeterUnitSelectorFor == 1) perimeterUnit1 = symbol else perimeterUnit2 = symbol
                                    showPerimeterUnitSelectorFor = null
                                }
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MainCustomBottomSheet(
    modifier: Modifier = Modifier,
    peekHeight: Dp = 110.dp,
    expandedHeightRatio: Float = 0.93f,
    widthRatio: Float = 1f,
    initialExpanded: Boolean = false,
    onDismissed: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val expandedHeight = screenHeight * expandedHeightRatio

    val density = LocalDensity.current
    val peekHeightPx = with(density) { peekHeight.toPx() }
    val expandedHeightPx = with(density) { expandedHeight.toPx() }

    val initialHeightPx = if (initialExpanded) expandedHeightPx else peekHeightPx
    val heightAnimatable = remember { Animatable(initialHeightPx) }
    val coroutineScope = rememberCoroutineScope()

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            val newHeight = heightAnimatable.value - delta
            heightAnimatable.snapTo(newHeight.coerceIn(peekHeightPx, expandedHeightPx))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(widthRatio)
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
                        if (targetHeight == peekHeightPx) {
                            onDismissed?.invoke()
                        }
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

@Composable
fun MeasurementCard(modifier: Modifier = Modifier, value: String, unit: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = color.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Change Unit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp).padding(bottom = 2.dp)
                )
            }
        }
    }
}


@Composable
fun ConversionRow3(modifier: Modifier = Modifier, stateName: String, unitName: String, value: Double) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stateName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(text = unitName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(text = formatValue(value), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

fun formatValue(value: Double): String {
    if (value == 0.0) return "--"
    return String.format("%.2f", value)
}
