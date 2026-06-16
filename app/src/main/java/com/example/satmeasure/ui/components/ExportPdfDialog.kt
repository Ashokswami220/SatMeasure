package com.example.satmeasure.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.satmeasure.R
import com.example.satmeasure.ui.map.AreaUnit
import com.example.satmeasure.ui.map.MeasurementConverter
import kotlinx.coroutines.launch

data class PdfExportOptions(
    val name: String = "",
    val includePerimeter: Boolean = true,
    val includeCoordinates: Boolean = true,
    val selectedUnits: Set<AreaUnit> = setOf(AreaUnit.SquareMeter, AreaUnit.Acre, AreaUnit.Hectare)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExportPdfDialog(
    initialOptions: PdfExportOptions,
    onDismiss: () -> Unit,
    onExport: (PdfExportOptions) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var name by remember { mutableStateOf(initialOptions.name) }
    var incPerimeter by remember { mutableStateOf(initialOptions.includePerimeter) }
    var incCoordinates by remember { mutableStateOf(initialOptions.includeCoordinates) }
    var selectedUnits by remember { mutableStateOf(initialOptions.selectedUnits) }

    var showAdvancedOptions by remember { mutableStateOf(false) }

    val globalUnits =
        listOf(AreaUnit.SquareMeter, AreaUnit.SquareYard, AreaUnit.Acre, AreaUnit.Hectare)
    val bighaUnits = MeasurementConverter.getAllBighaUnits()
    val localUnits = MeasurementConverter.getOtherLocalUnits()
        .map { it.second }

    val pages = listOf(
        stringResource(id = R.string.tab_global),
        stringResource(id = R.string.tab_bigha),
        stringResource(id = R.string.tab_local)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_lg)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = dimensionResource(id = R.dimen.spacing_sm),
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.75f else 0.95f)
                .then(
                    if (showAdvancedOptions || isLandscape) Modifier.fillMaxHeight(
                        if (isLandscape) 0.9f else 0.6f
                    )
                    else Modifier.wrapContentHeight()
                )
                .animateContentSize()
        ) {
            if (!showAdvancedOptions) {
                // Main View
                Column(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.spacing_md))
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_title_export_pdf),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { showAdvancedOptions = true },
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(
                                horizontal = dimensionResource(id = R.dimen.spacing_md_minus),
                                vertical = dimensionResource(id = R.dimen.spacing_xs)
                            ),
                            modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl))
                        ) {
                            Text(
                                stringResource(id = R.string.action_advanced_select_unit),
                                fontSize = 12.sp
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_xs)
                                )
                            )

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(
                                    id = R.string.cd_expand_options
                                ),
                                modifier = Modifier.size(
                                    dimensionResource(id = R.dimen.spacing_md)
                                ) // Scaled down to match the 12.sp text!
                            )

                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.label_document_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_md)),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CheckboxRow(
                            stringResource(id = R.string.checkbox_perimeter_details), incPerimeter,
                            modifier = Modifier.weight(1f)
                        ) { incPerimeter = it }
                        CheckboxRow(
                            stringResource(id = R.string.checkbox_coordinates_list), incCoordinates,
                            modifier = Modifier.weight(1f)
                        ) { incCoordinates = it }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))

                    // Advanced Options button moved to top

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_lg)))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                stringResource(id = R.string.action_cancel)
                            )
                        }
                        Spacer(
                            modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_sm))
                        )
                        Button(
                            onClick = {
                                onExport(
                                    PdfExportOptions(
                                        name = name,
                                        includePerimeter = incPerimeter,
                                        includeCoordinates = incCoordinates,
                                        selectedUnits = selectedUnits
                                    )
                                )
                            },
                            enabled = name.isNotBlank() && selectedUnits.isNotEmpty(),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_xl))
                        ) {
                            Text(stringResource(id = R.string.action_export))
                        }
                    }
                }
            } else {
                // Advanced Options View
                Column(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.spacing_md_minus))
                        .fillMaxSize()
                ) {
                    if (!isLandscape) {
                        Text(
                            text = stringResource(id = R.string.title_select_units_to_export),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                bottom = dimensionResource(id = R.dimen.spacing_sm)
                            )
                        )
                    }

                    val pagerContent = @Composable { modifier: Modifier ->
                        HorizontalPager(
                            state = pagerState,
                            verticalAlignment = Alignment.Top,
                            modifier = modifier
                        ) { page ->
                            val unitsForPage = when (page) {
                                0 -> globalUnits
                                1 -> bighaUnits
                                else -> localUnits
                            }
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimensionResource(id = R.dimen.spacing_xl)),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val allSelected =
                                        unitsForPage.all { selectedUnits.contains(it) }
                                    Text(
                                        text = if (allSelected) stringResource(
                                            id = R.string.action_deselect_all
                                        ) else stringResource(id = R.string.action_select_all),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .clickable {
                                                selectedUnits = if (allSelected) {
                                                    selectedUnits - unitsForPage.toSet()
                                                } else {
                                                    selectedUnits + unitsForPage.toSet()
                                                }
                                            }
                                            .padding(
                                                horizontal = dimensionResource(
                                                    id = R.dimen.spacing_sm
                                                ), vertical = dimensionResource(
                                                    id = R.dimen.spacing_xs
                                                )
                                            )
                                    )
                                }
                                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(
                                        if (isLandscape) 3 else 2
                                    ),
                                    contentPadding = PaddingValues(bottom = dimensionResource(id = R.dimen.corner_sm)),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(unitsForPage.size) { i ->
                                        val unit = unitsForPage[i]
                                        val displayName = when (unit) {
                                            is AreaUnit.Bigha -> "${
                                                stringResource(
                                                    id = R.string.unit_bigha
                                                )
                                            } (${stringResource(id = unit.state.displayNameResId)})"

                                            else -> stringResource(id = unit.displayNameResId)
                                        }
                                        CheckboxRow(
                                            label = displayName,
                                            checked = selectedUnits.contains(unit),
                                            onCheckedChange = { isChecked ->
                                                selectedUnits = if (isChecked) {
                                                    selectedUnits + unit
                                                } else {
                                                    selectedUnits - unit
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(0.35f)
                                    .padding(top = dimensionResource(id = R.dimen.spacing_sm))
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.title_select_units_to_export
                                    ),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(
                                        bottom = dimensionResource(id = R.dimen.spacing_md),
                                        start = dimensionResource(id = R.dimen.spacing_sm)
                                    )
                                )
                                pages.forEachIndexed { index, title ->
                                    val isSelected = pagerState.currentPage == index
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(
                                                    index
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                            VerticalDivider(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.corner_sm)))
                            pagerContent(Modifier.weight(0.65f))
                        }
                    } else {
                        SecondaryTabRow(
                            selectedTabIndex = pagerState.currentPage
                        ) {
                            pages.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                index
                                            )
                                        }
                                    },
                                    text = {
                                        Text(
                                            title, fontWeight = FontWeight.Bold, fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                        pagerContent(
                            Modifier
                                .weight(1f)
                                .padding(top = dimensionResource(id = R.dimen.corner_sm))
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md_minus))
                    )
                    Button(
                        onClick = { showAdvancedOptions = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.spacing_xxl)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_md))
                    ) {
                        Text(
                            stringResource(id = R.string.action_take_me_back), fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacing_xxs)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_xs)))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            lineHeight = 16.sp
        )
    }
}
