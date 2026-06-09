package com.example.satmeasure.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satmeasure.model.MeasurementRecord
import com.example.satmeasure.ui.viewmodel.AuthViewModel
import com.example.satmeasure.ui.viewmodel.MapViewModel
import com.example.satmeasure.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    authViewModel: AuthViewModel,
    mapViewModel: MapViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val savedMeasurements by mapViewModel.savedMeasurements.collectAsState()
    
    LaunchedEffect(authState.currentUser) {
        authState.currentUser?.uid?.let { uid ->
            mapViewModel.loadMeasurements(uid)
        }
    }

    val context = LocalContext.current
    var topMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Measurements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { 
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            topMenuExpanded = true 
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove All", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                                    topMenuExpanded = false
                                    authState.currentUser?.uid?.let { mapViewModel.deleteAllMeasurements(it) }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (savedMeasurements.isEmpty()) {
                Text(
                    text = "No saved measurements yet.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedMeasurements.reversed()) { record ->
                        MeasurementCard(
                            record = record,
                            onOpen = {
                                mapViewModel.loadRecordIntoMap(record, isEditable = false)
                                onNavigateToMap()
                            },
                            onEdit = {
                                mapViewModel.loadRecordIntoMap(record, isEditable = true)
                                onNavigateToMap()
                            },
                            onDelete = {
                                authState.currentUser?.uid?.let { 
                                    mapViewModel.deleteMeasurement(it, record.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementCard(
    record: MeasurementRecord,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var cardMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                
                Box {
                    IconButton(onClick = { 
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        cardMenuExpanded = true 
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = cardMenuExpanded,
                        onDismissRequest = { cardMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                                cardMenuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(record.timestamp))
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                        onEdit()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            onOpen()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Open")
                    }
                }
            }
        }
    }
}