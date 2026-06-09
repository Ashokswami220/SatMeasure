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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    val globalUnits = listOf(AreaUnit.SquareMeter, AreaUnit.SquareYard, AreaUnit.Acre, AreaUnit.Hectare)
    val bighaUnits = MeasurementConverter.getAllBighaUnits()
    val localUnits = MeasurementConverter.getOtherLocalUnits().map { it.second }

    val pages = listOf("Global", "Bigha", "Local")
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.75f else 0.95f)
                .then(
                    if (showAdvancedOptions || isLandscape) Modifier.fillMaxHeight(if (isLandscape) 0.9f else 0.6f)
                    else Modifier.wrapContentHeight()
                )
                .animateContentSize()
        ) {
            if (!showAdvancedOptions) {
                // Main View
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Export PDF",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { showAdvancedOptions = true },
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Advanced ( Select Unit )", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand options",
                                modifier = Modifier.size(16.dp) // Scaled down to match the 12.sp text!
                            )

                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Document Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CheckboxRow("Perimeter Details", incPerimeter, modifier = Modifier.weight(1f)) { incPerimeter = it }
                        CheckboxRow("Coordinates List", incCoordinates, modifier = Modifier.weight(1f)) { incCoordinates = it }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Advanced Options button moved to top

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { 
                                onExport(PdfExportOptions(
                                    name = name, 
                                    includePerimeter = incPerimeter, 
                                    includeCoordinates = incCoordinates,
                                    selectedUnits = selectedUnits
                                ))
                            },
                            enabled = name.isNotBlank() && selectedUnits.isNotEmpty(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Export")
                        }
                    }
                }
            } else {
                // Advanced Options View
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                ) {
                    if (!isLandscape) {
                        Text(
                            text = "Select Units to Export",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
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
                                    modifier = Modifier.fillMaxWidth().height(32.dp), 
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val allSelected = unitsForPage.all { selectedUnits.contains(it) }
                                    Text(
                                        text = if (allSelected) "Deselect All" else "Select All",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.clickable {
                                            selectedUnits = if (allSelected) {
                                                selectedUnits - unitsForPage.toSet()
                                            } else {
                                                selectedUnits + unitsForPage.toSet()
                                            }
                                        }.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(if (isLandscape) 3 else 2),
                                    contentPadding = PaddingValues(bottom = 8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(unitsForPage.size) { i ->
                                        val unit = unitsForPage[i]
                                        val displayName = when(unit) {
                                            is AreaUnit.Bigha -> "Bigha (${unit.state.displayName})"
                                            else -> unit.displayName
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
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Column(modifier = Modifier.weight(0.35f).padding(top = 8.dp)) {
                                Text(
                                    text = "Select Units to Export",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                                )
                                pages.forEachIndexed { index, title ->
                                    val isSelected = pagerState.currentPage == index
                                    TextButton(
                                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                            VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            pagerContent(Modifier.weight(0.65f))
                        }
                    } else {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                )
                            }
                        ) {
                            pages.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                )
                            }
                        }
                        pagerContent(Modifier.weight(1f).padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showAdvancedOptions = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Take Me Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            lineHeight = 16.sp
        )
    }
}
