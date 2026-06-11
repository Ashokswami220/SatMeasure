package com.example.satmeasure.ui.main

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.satmeasure.R
import com.example.satmeasure.model.MeasurementRecord
import com.example.satmeasure.ui.viewmodel.AuthViewModel
import com.example.satmeasure.ui.viewmodel.MapViewModel
import com.example.satmeasure.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
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
            Column {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.title_saved_measurements), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(id = R.string.cd_back))
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { 
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                topMenuExpanded = true 
                            }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.cd_menu))
                            }
                            DropdownMenu(
                                expanded = topMenuExpanded,
                                onDismissRequest = { topMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.action_remove_all), color = MaterialTheme.colorScheme.error) },
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
                HorizontalDivider()
            }
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
                    text = stringResource(id = R.string.msg_no_saved_measurements),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val shareMessageTemplate = stringResource(id = R.string.share_message)
                LazyColumn(
                    contentPadding = PaddingValues(dimensionResource(id = R.dimen.spacing_md)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md_minus))
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
                            onShare = {
                                authState.currentUser?.uid?.let { uid ->
                                    val shareUrl = "https://satmeasure.web.app/share?plotId=${record.id}&ownerId=$uid"
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareMessageTemplate.format(shareUrl))
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                }
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
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var cardMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_md)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.spacing_xxs))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_md_minus))) {
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
                
                Row {
                    IconButton(onClick = { 
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onShare() 
                    }, modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_xl))) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(id = R.string.menu_share), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Box {
                        IconButton(onClick = { 
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            cardMenuExpanded = true 
                        }, modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_xl))) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.cd_menu), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(
                            expanded = cardMenuExpanded,
                            onDismissRequest = { cardMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                                    cardMenuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xs)))

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
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_sm))
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_xs)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                        onEdit()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.cd_edit), tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                            onOpen()
                        },
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_sm)),
                        contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.spacing_md), vertical = dimensionResource(id = R.dimen.spacing_sm))
                    ) {
                        Text(stringResource(id = R.string.action_open))
                    }
                }
            }
        }
    }
}