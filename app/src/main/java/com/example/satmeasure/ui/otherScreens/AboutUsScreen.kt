package com.example.satmeasure.ui.otherScreens

import android.content.Intent
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.*
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.satmeasure.R
import com.example.satmeasure.utils.HapticHelper

// Hoisted Links
private const val GITHUB_LINK = "https://github.com/Ashokswami220"
private const val TWITTER_LINK = "https://x.com/AshokSwami22"
private const val INSTAGRAM_LINK = "https://instagram.com/swamiashok220"
private const val LINKEDIN_LINK = "https://www.linkedin.com/in/swamiashok220"
private const val FEEDBACK_EMAIL = "mailto:Swamiashok2228@gmail.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val openLink = { url: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    Scaffold { paddingValues ->
        var pointerOffset by remember { mutableStateOf(Offset.Unspecified) }
        val primaryColor = MaterialTheme.colorScheme.primary
        val backgroundColor = MaterialTheme.colorScheme.background
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull()
                            pointerOffset = if (change != null && change.pressed) {
                                change.position
                            } else {
                                Offset.Unspecified
                            }
                        }
                    }
                }
                .drawBehind {
                    val dotRadius = 0.8.dp.toPx()
                    val spacing = 16.dp.toPx()
                    val effectRadius = 150.dp.toPx()
                    val baseAlpha = 0.15f
                    val highlightAlpha = 0.8f

                    val columns = (size.width / spacing).toInt() + 1
                    val rows = (size.height / spacing).toInt() + 1

                    for (i in 0..columns) {
                        for (j in 0..rows) {
                            val center = Offset(i * spacing, j * spacing)
                            val distance = if (pointerOffset.isSpecified) {
                                (center - pointerOffset).getDistance()
                            } else {
                                Float.MAX_VALUE
                            }

                            val factor = (1f - (distance / effectRadius)).coerceIn(0f, 1f)

                            val currentRadius = dotRadius + (factor * dotRadius * 1.5f)
                            val alpha = baseAlpha + (factor * (highlightAlpha - baseAlpha))

                            drawCircle(
                                color = primaryColor.copy(alpha = alpha),
                                radius = currentRadius,
                                center = center
                            )
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.5f else 1f)
                        .padding(
                            top = paddingValues.calculateTopPadding() + dimensionResource(id = R.dimen.text_lg),
                            bottom = paddingValues.calculateBottomPadding() + dimensionResource(id = R.dimen.icon_lg),
                            start = dimensionResource(id = R.dimen.text_lg),
                            end = dimensionResource(id = R.dimen.text_lg)
                        )
                        .drawBehind {
                            val paint = Paint().apply {
                                color = backgroundColor.copy(alpha = 0.85f)
                                asFrameworkPaint().maskFilter = BlurMaskFilter(
                                    150f,
                                    BlurMaskFilter.Blur.NORMAL
                                )
                            }

                            drawIntoCanvas { canvas ->
                                canvas.drawRoundRect(
                                    left = 0f,
                                    top = 0f,
                                    right = size.width,
                                    bottom = size.height,
                                    radiusX = 100f,
                                    radiusY = 100f,
                                    paint = paint
                                )
                            }
                        }
                        .padding(start = dimensionResource(id = R.dimen.text_xxxl), end = dimensionResource(id = R.dimen.text_xxxl), bottom = dimensionResource(id = R.dimen.text_xxxl), top = dimensionResource(id = R.dimen.text_xxxl)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.spacing_xs)
                            ) // 24dp (from parent) + 4dp = 28dp total inner padding
                            .padding(bottom = dimensionResource(id = R.dimen.icon_lg)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.developed_and),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dimen_40))
                                )
                                Text(
                                    text = stringResource(id = R.string.designed_by),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dimen_120))
                                )
                                Text(
                                    text = stringResource(id = R.string.ashok_swami),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dimen_40))
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.dimen_100))
                                    .align(Alignment.CenterEnd)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .border(dimensionResource(id = R.dimen.spacing_xxs), MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.profile),
                                    contentDescription = stringResource(id = R.string.profile_photo),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    val glassButtonColors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                    val glassBorder = BorderStroke(
                        width = dimensionResource(id = R.dimen.dimen_1),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )

                    // Social Buttons
                    OutlinedButton(
                        onClick = { openLink(GITHUB_LINK) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.icon_xl)),
                        shape = CircleShape,
                        colors = glassButtonColors,
                        border = glassBorder
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(
                                stringResource(id = R.string.github), fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

                    OutlinedButton(
                        onClick = { openLink(TWITTER_LINK) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.icon_xl)),
                        shape = CircleShape,
                        colors = glassButtonColors,
                        border = glassBorder
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_twitter_x),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(
                                stringResource(id = R.string.twitter_x), fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

                    OutlinedButton(
                        onClick = { openLink(INSTAGRAM_LINK) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.icon_xl)),
                        shape = CircleShape,
                        colors = glassButtonColors,
                        border = glassBorder
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(
                                stringResource(id = R.string.instagram), fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

                    OutlinedButton(
                        onClick = { openLink(LINKEDIN_LINK) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.icon_xl)),
                        shape = CircleShape,
                        colors = glassButtonColors,
                        border = glassBorder
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_linkedin),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(
                                stringResource(id = R.string.linkedin), fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_lg)))

                    // Feedback Button
                    OutlinedButton(
                        onClick = { openLink(FEEDBACK_EMAIL) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.icon_xl)),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(
                            width = dimensionResource(id = R.dimen.dimen_1),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(
                                text = stringResource(id = R.string.send_feedback),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            } // Close inner scrollable Box

            // Custom Circular Back Button
            IconButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onBackClick()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(dimensionResource(id = R.dimen.text_lg))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = stringResource(id = R.string.cd_back),
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.corner_sm))
                )
            }
        }
    }
}