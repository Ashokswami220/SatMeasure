package com.example.satmeasure.ui.components

import com.example.satmeasure.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.satmeasure.R
import androidx.compose.ui.window.DialogProperties
import com.example.satmeasure.ui.map.AreaUnit
import com.example.satmeasure.ui.map.MeasurementConverter
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AreaUnitSelectorSheet(
    currentSelection: AreaUnit,
    onDismiss: () -> Unit,
    onUnitSelected: (AreaUnit) -> Unit
) {
    val categories = listOf(
        stringResource(id = R.string.tab_global),
        stringResource(id = R.string.tab_bigha),
        stringResource(id = R.string.tab_local_units)
    )

    val initialTab = when (currentSelection) {
        is AreaUnit.Bigha -> 1
        is AreaUnit.SquareMeter,
        is AreaUnit.SquareYard,
        is AreaUnit.Acre,
        is AreaUnit.Hectare -> 0

        else -> 2
    }

    val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    CustomAreaUnitSelectorSheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_md))) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))
            Text(
                stringResource(id = R.string.title_select_area_unit),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_sm)))

            // Category Tabs
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage, containerColor = Color.Transparent
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_sm)))

            // Swipeable Pages
            HorizontalPager(
                state = pagerState, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) { page ->
                val unitsToShow = when (page) {
                    0 -> listOf(
                        AreaUnit.SquareMeter, AreaUnit.SquareYard, AreaUnit.Acre, AreaUnit.Hectare
                    )

                    1 -> MeasurementConverter.getAllBighaUnits()
                    2 -> MeasurementConverter.getOtherLocalUnits()
                        .map { it.second }

                    else -> emptyList()
                }

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .simpleVerticalScrollbar(scrollState)
                ) {
                    unitsToShow.forEach { unit ->
                        val isSelected = unit == currentSelection
                        val unitName = if (unit is AreaUnit.Bigha) "${
                            stringResource(
                                id = unit.state.displayNameResId
                            )
                        } ${stringResource(id = unit.displayNameResId)}" else stringResource(
                            id = unit.displayNameResId
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    onUnitSelected(unit)
                                }
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.spacing_sm),
                                    vertical = dimensionResource(id = R.dimen.spacing_md)
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = unitName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(id = R.string.cd_selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_lg)))
                }
            }
        }
    }
}

@Composable
fun CustomAreaUnitSelectorSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        ) {
            MainCustomBottomSheet(
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    )
                    .align(if (isLandscape) Alignment.BottomStart else Alignment.BottomCenter)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                peekHeight = 0.dp,
                expandedHeightRatio = if (isLandscape) 0.8f else 0.55f,
                widthRatio = if (isLandscape) 0.5f else 1f,
                initialExpanded = true,
                onDismissed = onDismiss
            ) { _ ->
                Box(modifier = Modifier.navigationBarsPadding()) {
                    content()
                }
            }
        }
    }
}

fun Modifier.simpleVerticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    thumbHeight: Dp = 40.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f)
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    drawWithContent {
        drawContent()

        val maxScrollOffset = state.maxValue.toFloat()
        val currentScrollOffset = state.value.toFloat()

        if (maxScrollOffset > 0f && alpha > 0f) {
            val scrollbarHeight = thumbHeight.toPx()
            val scrollableRange = size.height - scrollbarHeight
            val progress = currentScrollOffset / maxScrollOffset
            val scrollbarY = progress * scrollableRange

            drawRoundRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx() - 4.dp.toPx(), scrollbarY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha,
                cornerRadius = CornerRadius(width.toPx() / 2f)
            )
        }
    }
}
