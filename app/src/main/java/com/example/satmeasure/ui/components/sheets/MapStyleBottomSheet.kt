package com.example.satmeasure.ui.components.sheets

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.satmeasure.R
import com.example.satmeasure.ui.map.models.MapStyleOption
import com.mapbox.maps.Style

@Composable
fun getAvailableMapStyles() = listOf(
    MapStyleOption(
        "satellite_streets", stringResource(id = R.string.satellite_streets),
        Style.SATELLITE_STREETS,
        Style.SATELLITE_STREETS, R.drawable.satellite_map
    ),
    MapStyleOption(
        "standard", stringResource(id = R.string.standard_navigation), Style.STANDARD,
        Style.STANDARD, R.drawable.standard_map
    ),
    MapStyleOption(
        "outdoors", stringResource(id = R.string.terrain_outdoors), Style.OUTDOORS, Style.OUTDOORS,
        R.drawable.terrain_map
    ),
    MapStyleOption(
        "satellite", stringResource(id = R.string.pure_satellite), Style.STANDARD_SATELLITE,
        Style.SATELLITE,
        R.drawable.satellite_map
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapStyleBottomSheet(
    currentStyleId: String,
    onStyleSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val chunkCount = if (isLandscape) 4 else 3

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.text_lg),
            topEnd = dimensionResource(id = R.dimen.text_lg)
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.text_lg),
                    end = dimensionResource(id = R.dimen.text_lg),
                    bottom = dimensionResource(id = R.dimen.text_lg),
                    top = dimensionResource(id = R.dimen.corner_sm)
                )
                .verticalScroll(rememberScrollState())
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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.map_type),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

            // Grid
            val chunkedStyles = getAvailableMapStyles().chunked(chunkCount)
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.text_lg))
            ) {
                chunkedStyles.forEach { rowStyles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimensionResource(id = R.dimen.icon_lg)
                        )
                    ) {
                        rowStyles.forEach { styleOpt ->
                            val isSelected = currentStyleId == styleOpt.id
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    onClick = { onStyleSelected(styleOpt.id) },
                                    shape = RoundedCornerShape(
                                        dimensionResource(id = R.dimen.text_sm)
                                    ),
                                    color = Color.Transparent,
                                    border = if (isSelected) BorderStroke(
                                        dimensionResource(id = R.dimen.spacing_xxs),
                                        MaterialTheme.colorScheme.primary
                                    ) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimensionResource(id = R.dimen.dimen_130))
                                ) {
                                    Image(
                                        painter = painterResource(id = styleOpt.imageRes),
                                        contentDescription = styleOpt.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(
                                    modifier = Modifier.height(
                                        dimensionResource(id = R.dimen.corner_sm)
                                    )
                                )
                                Text(
                                    text = styleOpt.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        repeat(chunkCount - rowStyles.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier.height(
                    if (isLandscape) dimensionResource(
                        id = R.dimen.spacing_xs
                    ) else dimensionResource(id = R.dimen.text_xxxl)
                )
            )
        }
    }
}
