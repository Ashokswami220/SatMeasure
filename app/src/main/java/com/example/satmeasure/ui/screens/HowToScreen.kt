package com.example.satmeasure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.satmeasure.R
import com.example.satmeasure.utils.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToCoordinatesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_find_coordinates)) },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                        onBackClick()
                    }) {
                        // iOS Style Back Arrow
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = stringResource(id = R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.text_lg)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.text_lg))
        ) {
            Text(
                text = stringResource(R.string.desc_how_to_find_coordinates),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.corner_sm))
            )

            StepCard(
                stepNum = 1,
                icon = Icons.Default.Map,
                title = stringResource(R.string.step_open_maps_title),
                desc = stringResource(R.string.step_open_maps_desc)
            )

            StepCard(
                stepNum = 2,
                icon = Icons.Default.TouchApp,
                title = stringResource(R.string.step_drop_pin_title),
                desc = stringResource(R.string.step_drop_pin_desc)
            )

            StepCard(
                stepNum = 3,
                icon = Icons.Default.ContentCopy,
                title = stringResource(R.string.step_copy_coords_title),
                desc = stringResource(R.string.step_copy_coords_desc)
            )

            StepCard(
                stepNum = 4,
                icon = Icons.Default.ContentPaste,
                title = stringResource(R.string.step_paste_go_title),
                desc = stringResource(R.string.step_paste_go_desc)
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_xxxl)))

            Button(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.button_height)),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm))
            ) {
                Text(
                    stringResource(R.string.action_got_it_take_me_back),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun StepCard(stepNum: Int, icon: ImageVector, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.text_lg)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.icon_xl))
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.text_lg)))
            Column {
                Text(
                    stringResource(R.string.format_step_title, stepNum, title),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xs)))
                Text(desc, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}