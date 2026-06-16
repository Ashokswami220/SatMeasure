package com.example.satmeasure.ui.components

import com.example.satmeasure.utils.HapticHelper

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import android.widget.Toast
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.satmeasure.ui.map.AreaUnit
import com.example.satmeasure.ui.map.MeasurementConverter
import kotlinx.coroutines.launch
import com.example.satmeasure.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSheet(
    modifier: Modifier = Modifier,
    areaMeters: Double = 0.0,
    perimeterMeters: Double = 0.0,
    isAtPeekHeight: Boolean = false,
    onSaveClick: () -> Unit = {},
    onExportClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val areaSqFt = areaMeters * 10.7639

    var areaUnit1 by remember { mutableStateOf<AreaUnit>(AreaUnit.SquareMeter) }
    var areaUnit2 by remember { mutableStateOf<AreaUnit>(AreaUnit.Acre) }

    var perimeterUnit1 by remember { mutableStateOf("m") }
    var perimeterUnit2 by remember { mutableStateOf("km") }

    val showAreaUnitSelectorFor = remember { mutableStateOf<Int?>(null) } // 1 or 2
    val showPerimeterUnitSelectorFor = remember { mutableStateOf<Int?>(null) } // 1 or 2

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = dimensionResource(id = R.dimen.text_xl), topEnd = dimensionResource(id = R.dimen.text_xl))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = dimensionResource(id = R.dimen.text_lg), end = dimensionResource(id = R.dimen.text_lg), bottom = dimensionResource(id = R.dimen.text_lg), top = dimensionResource(id = R.dimen.dimen_10))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.title_calculation_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (areaMeters > 0.0 || perimeterMeters > 0.0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.height(dimensionResource(id = R.dimen.dimen_28)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    onSaveClick()
                                },
                                contentPadding = PaddingValues(
                                    horizontal = dimensionResource(id = R.dimen.spacing_md_minus),
                                    vertical = dimensionResource(id = R.dimen.dimen_0)
                                )
                            ) {
                                Text(
                                    stringResource(id = R.string.action_save),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            VerticalDivider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = dimensionResource(id = R.dimen.spacing_sm_minus)),
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.2f
                                )
                            )
                            TextButton(
                                onClick = {
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    onExportClick()
                                },
                                contentPadding = PaddingValues(
                                    horizontal = dimensionResource(id = R.dimen.spacing_md_minus),
                                    vertical = dimensionResource(id = R.dimen.dimen_0)
                                )
                            ) {
                                Text(
                                    stringResource(id = R.string.action_export_pdf),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.corner_sm)))
            HorizontalDivider(
                thickness = dimensionResource(id = R.dimen.dimen_1),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))

            val scrollState = rememberScrollState()

            LaunchedEffect(isAtPeekHeight) {
                if (isAtPeekHeight) {
                    scrollState.scrollTo(0)
                }
            }

            val configuration = LocalConfiguration.current
            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            val scrollEnabled = !(isPortrait && isAtPeekHeight)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState, enabled = scrollEnabled)
            ) {
                // --- Top Headers ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen.spacing_xs))
                ) {
                    Text(
                        text = stringResource(id = R.string.label_area),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(id = R.string.label_perimeter),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_sm)))

                // --- 2x2 Grid Section ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.text_sm))
                ) {
                    MeasurementCard(
                        modifier = Modifier.weight(1f),
                        value = formatValue(MeasurementConverter.convertArea(areaSqFt, areaUnit1)),
                        unit = getFullAreaUnitName(areaUnit1),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            showAreaUnitSelectorFor.value = 1
                        }
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
                        unit = getFullPerimeterUnitName(perimeterUnit1),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            showPerimeterUnitSelectorFor.value = 1
                        }
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.text_sm))
                ) {
                    MeasurementCard(
                        modifier = Modifier.weight(1f),
                        value = formatValue(MeasurementConverter.convertArea(areaSqFt, areaUnit2)),
                        unit = getFullAreaUnitName(areaUnit2),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            showAreaUnitSelectorFor.value = 2
                        }
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
                        unit = getFullPerimeterUnitName(perimeterUnit2),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            showPerimeterUnitSelectorFor.value = 2
                        }
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.icon_lg)))
                HorizontalDivider(
                    thickness = dimensionResource(id = R.dimen.dimen_1), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.2f
                    )
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))

                // --- Bigha Section ---
                Text(
                    text = stringResource(id = R.string.label_bigha),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_sm)))
                val bighas = MeasurementConverter.getAllBighaUnits()
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.corner_sm))) {
                    bighas.forEach { bigha ->
                        val converted = MeasurementConverter.convertArea(areaSqFt, bigha)
                        ConversionRow3(
                            modifier = Modifier.fillMaxWidth(), stateName = stringResource(
                                id = bigha.state.displayNameResId
                            ), unitName = stringResource(id = R.string.unit_bigha),
                            value = converted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))
                HorizontalDivider(
                    thickness = dimensionResource(id = R.dimen.dimen_1), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.2f
                    )
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))

                // --- Other Local Units Section ---
                Text(
                    text = stringResource(id = R.string.label_other_local_units),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_sm)))
                val locals = MeasurementConverter.getOtherLocalUnits()
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.corner_sm))) {
                    locals.forEach { local ->
                        val converted = MeasurementConverter.convertArea(areaSqFt, local.second)
                        ConversionRow3(
                            modifier = Modifier.fillMaxWidth(),
                            stateName = stringResource(id = local.first), unitName = stringResource(
                                id = local.second.displayNameResId
                            ), value = converted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_xxxl)))
            }
        }
    }

    // --- Unit Selection Dialogs/Sheets ---
    if (showAreaUnitSelectorFor.value != null) {
        val currentAreaUnit = if (showAreaUnitSelectorFor.value == 1) areaUnit1 else areaUnit2
        AreaUnitSelectorSheet(
            currentSelection = currentAreaUnit,
            onDismiss = { 
                showAreaUnitSelectorFor.value = null
            },
            onUnitSelected = { selected ->
                if (showAreaUnitSelectorFor.value == 1) areaUnit1 = selected else areaUnit2 = selected
                showAreaUnitSelectorFor.value = null
            }
        )
    }

    if (showPerimeterUnitSelectorFor.value != null) {
        val currentPerimeterUnit =
            if (showPerimeterUnitSelectorFor.value == 1) perimeterUnit1 else perimeterUnit2
        CustomAreaUnitSelectorSheet(onDismiss = { 
            showPerimeterUnitSelectorFor.value = null
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.spacing_md))
            ) {
                Text(
                    stringResource(id = R.string.title_select_perimeter_unit),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))

                val options = listOf(
                    "m" to stringResource(id = R.string.unit_meters),
                    "km" to stringResource(id = R.string.unit_kilometers),
                    "ft" to stringResource(id = R.string.unit_feet),
                    "mi" to stringResource(id = R.string.unit_miles)
                )
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .simpleVerticalScrollbar(scrollState)
                ) {
                    options.forEach { (symbol, name) ->
                        val isSelected = currentPerimeterUnit == symbol
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    if (showPerimeterUnitSelectorFor.value == 1) perimeterUnit1 =
                                        symbol else perimeterUnit2 = symbol
                                    showPerimeterUnitSelectorFor.value = null
                                }
                                .padding(horizontal = dimensionResource(id = R.dimen.corner_sm), vertical = dimensionResource(id = R.dimen.text_lg)),
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
                                Icon(
                                    Icons.Default.Check, contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_xxxl)))
                }
            }
        }
    }
}

@Composable
fun MainCustomBottomSheet(
    modifier: Modifier = Modifier,
    peekHeight: Dp = dimensionResource(id = R.dimen.dimen_110),
    expandedHeightRatio: Float = 0.93f,
    widthRatio: Float = 1f,
    initialExpanded: Boolean = false,
    onDismissed: (() -> Unit)? = null,
    content: @Composable (isAtPeekHeight: Boolean) -> Unit
) {
    val density = LocalDensity.current
    val containerHeightPx =
        androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.height.toFloat()
    val expandedHeightPx = containerHeightPx * expandedHeightRatio
    val peekHeightPx = with(density) { peekHeight.toPx() }

    val initialHeightPx = if (initialExpanded) expandedHeightPx else peekHeightPx
    val heightAnimatable = remember { Animatable(initialHeightPx) }
    val coroutineScope = rememberCoroutineScope()

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            val newHeight = heightAnimatable.value - delta
            heightAnimatable.snapTo(newHeight.coerceIn(peekHeightPx, expandedHeightPx))
        }
    }

    val isAtPeekHeight by remember {
        derivedStateOf {
            heightAnimatable.value <= peekHeightPx + 5f
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
        shape = RoundedCornerShape(topStart = dimensionResource(id = R.dimen.text_xxl), topEnd = dimensionResource(id = R.dimen.text_xxl)),
        shadowElevation = dimensionResource(id = R.dimen.corner_sm)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sheet Content
            Box(modifier = Modifier.fillMaxSize()) {
                content(isAtPeekHeight)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasurementCard(
    modifier: Modifier = Modifier, value: String, unit: String, color: Color, onClick: () -> Unit
) {
    val clipboardManager = LocalContext.current.getSystemService(
        android.content.Context.CLIPBOARD_SERVICE
    ) as android.content.ClipboardManager
    val context = LocalContext.current

    Surface(
        modifier = modifier.combinedClickable(
            onClick = {
                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                onClick()
            },
            onLongClick = {
                HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                val textToCopy = "$unit: $value"
                val clip = android.content.ClipData.newPlainText("Copied Text", textToCopy)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            }
        ),
        color = color.copy(alpha = 0.3f),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm))
    ) {
        Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.text_sm), vertical = dimensionResource(id = R.dimen.spacing_xs))) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Change Unit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxl))
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversionRow3(
    modifier: Modifier = Modifier, stateName: String, unitName: String, value: Double
) {
    val context = LocalContext.current
    val formattedValue = formatValue(value)

    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(dimensionResource(id = R.dimen.corner_sm))
            )
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_sm)))
            .combinedClickable(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                },
                onLongClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                    val textToCopy = "$stateName $unitName: $formattedValue"
                    val clipboardManager = context.getSystemService(
                        android.content.Context.CLIPBOARD_SERVICE
                    ) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Copied Text", textToCopy)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                        .show()
                }
            )
            .padding(horizontal = dimensionResource(id = R.dimen.text_sm), vertical = dimensionResource(id = R.dimen.dimen_10)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stateName, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)
        )
        Text(
            text = unitName, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f), textAlign = TextAlign.Center
        )
        Text(
            text = formattedValue, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f), textAlign = TextAlign.End
        )
    }
}

fun formatValue(value: Double): String {
    if (value == 0.0) return "--"
    return String.format(java.util.Locale.getDefault(), "%.2f", value)
}

@Composable
fun getFullAreaUnitName(unit: AreaUnit): String {
    return when (unit) {
        is AreaUnit.Bigha -> "${stringResource(id = unit.state.displayNameResId)} ${
            stringResource(
                id = R.string.unit_bigha
            )
        }"

        is AreaUnit.Biswa -> "${stringResource(id = unit.state.displayNameResId)} ${
            stringResource(
                id = R.string.unit_biswa
            )
        }"

        else -> stringResource(id = unit.displayNameResId)
    }
}

fun getFullPerimeterUnitName(unit: String): String {
    return when (unit) {
        "m" -> "Meters"
        "km" -> "Kilometers"
        "ft" -> "Feet"
        "mi" -> "Miles"
        else -> unit
    }
}
