package com.example.satmeasure.ui.otherScreens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.satmeasure.R

// Hoisted Links
private const val GITHUB_LINK = "https://github.com/ashokswami"
private const val TWITTER_LINK = "https://x.com/ashok_swami"
private const val INSTAGRAM_LINK = "https://instagram.com/ashok_swami"
private const val LINKEDIN_LINK = "https://linkedin.com/in/ashok-swami"
private const val FEEDBACK_EMAIL = "mailto:ashokswami@example.com"

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
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                pointerOffset = change.position
                            } else {
                                pointerOffset = Offset.Unspecified
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
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Developed & Designed By Ashok Swami",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val glassButtonColors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                val glassBorder = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                // Social Buttons
                OutlinedButton(
                    onClick = { openLink(GITHUB_LINK) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                            "GitHub", fontSize = 16.sp, modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { openLink(TWITTER_LINK) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                            "Twitter / X", fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { openLink(INSTAGRAM_LINK) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                            "Instagram", fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { openLink(LINKEDIN_LINK) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                            "LinkedIn", fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback Button
                OutlinedButton(
                    onClick = { openLink(FEEDBACK_EMAIL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
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
                            text = "Send Feedback",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Custom Circular Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "Back",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}