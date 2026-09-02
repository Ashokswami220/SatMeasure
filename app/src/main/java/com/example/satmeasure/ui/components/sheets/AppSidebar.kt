package com.example.satmeasure.ui.components.sheets

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.satmeasure.R
import com.example.satmeasure.ui.auth.AuthViewModel
import com.example.satmeasure.ui.navigation.SatMesRoutes
import com.example.satmeasure.utils.HapticHelper

@Composable
fun AppSidebar(
    currentRoute: String,
    authViewModel: AuthViewModel,
    onMenuSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG)
                .show()
            authViewModel.clearError()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val widthFraction = if (isLandscape) 0.4f else 0.65f

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(widthFraction)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            val itemColors = NavigationDrawerItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(
                    modifier = Modifier.height(
                        if (isLandscape) dimensionResource(
                            id = R.dimen.spacing_xs
                        ) else dimensionResource(id = R.dimen.text_lg)
                    )
                )

                val titlePadding = if (isLandscape) {
                    Modifier.padding(
                        start = dimensionResource(id = R.dimen.dimen_22),
                        end = dimensionResource(id = R.dimen.dimen_22),
                        top = dimensionResource(id = R.dimen.dimen_10),
                        bottom = dimensionResource(id = R.dimen.dimen_10)
                    )
                } else {
                    Modifier.padding(
                        start = dimensionResource(id = R.dimen.dimen_22),
                        end = dimensionResource(id = R.dimen.dimen_22),
                        top = dimensionResource(id = R.dimen.dimen_0),
                        bottom = dimensionResource(id = R.dimen.text_lg)
                    )
                }

                Text(
                    text = "SatMeasure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = titlePadding
                )

                HorizontalDivider()

                // Profile Section (Alive and Premium)
                val profilePadding = if (isLandscape) {
                    Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.text_lg),
                        vertical = dimensionResource(id = R.dimen.corner_sm)
                    )
                } else {
                    Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.text_lg),
                        vertical = dimensionResource(id = R.dimen.text_lg)
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(profilePadding)
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.text_lg))),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_lg))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.text_lg),
                                vertical = dimensionResource(id = R.dimen.text_sm)
                            )
                    ) {
                        // Sleek Row Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.icon_xl))
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (authState.currentUser?.photoUrl != null) {
                                    AsyncImage(
                                        model = authState.currentUser?.photoUrl,
                                        contentDescription = stringResource(
                                            id = R.string.profile_picture
                                        ),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (authState.currentUser != null) {
                                    val name = authState.currentUser?.displayName
                                        ?: authState.currentUser?.email ?: "U"
                                    val initial = name.take(1)
                                        .uppercase()
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = stringResource(
                                            id = R.string.profile_picture
                                        ),
                                        modifier = Modifier.size(
                                            dimensionResource(id = R.dimen.text_xxxl)
                                        ),
                                        tint = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                            Spacer(
                                modifier = Modifier.width(dimensionResource(id = R.dimen.text_lg))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                if (authState.currentUser != null) {
                                    Text(
                                        text = authState.currentUser?.displayName ?: "User",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(
                                        modifier = Modifier.height(
                                            dimensionResource(id = R.dimen.spacing_xxs)
                                        )
                                    )
                                    Text(
                                        text = authState.currentUser?.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        text = "••••••••••",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))

                        // Auth Button
                        OutlinedButton(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                if (authState.currentUser == null) {
                                    authViewModel.signInWithGoogle(context)
                                } else {
                                    authViewModel.signOut()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(id = R.dimen.dimen_40)),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.text_sm)),
                            border = BorderStroke(
                                dimensionResource(id = R.dimen.dimen_1),
                                MaterialTheme.colorScheme.tertiary
                            ),
                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.dimen_0)),
                            colors = outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(
                                        dimensionResource(id = R.dimen.text_xxl)
                                    ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    strokeWidth = dimensionResource(id = R.dimen.spacing_xxs)
                                )
                            } else if (authState.currentUser == null) {
                                Text(stringResource(id = R.string.action_sign_in_with_google))
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.text_xl))
                                        .padding(end = dimensionResource(id = R.dimen.spacing_xs))
                                )
                                Text(stringResource(id = R.string.action_logout))
                            }
                        }
                    }
                }

                HorizontalDivider()
                Spacer(
                    modifier = Modifier.height(
                        if (isLandscape) dimensionResource(
                            id = R.dimen.spacing_xs
                        ) else dimensionResource(id = R.dimen.corner_sm)
                    )
                )

                val itemPadding = if (isLandscape) {
                    Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.corner_sm),
                        vertical = dimensionResource(id = R.dimen.dimen_0)
                    )
                } else {
                    Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.corner_sm),
                        vertical = dimensionResource(id = R.dimen.spacing_xxs)
                    )
                }

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_saved_areas),
                            modifier = Modifier.padding(
                                start = dimensionResource(id = R.dimen.text_sm)
                            )
                        )
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.HISTORY,
                    onClick = { onMenuSelect(SatMesRoutes.HISTORY) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_tutorial),
                            modifier = Modifier.padding(
                                start = dimensionResource(id = R.dimen.text_sm)
                            )
                        )
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.TUTORIAL,
                    onClick = { onMenuSelect(SatMesRoutes.TUTORIAL) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_about_us),
                            modifier = Modifier.padding(
                                start = dimensionResource(id = R.dimen.text_sm)
                            )
                        )
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = currentRoute == SatMesRoutes.ABOUT_US,
                    onClick = { onMenuSelect(SatMesRoutes.ABOUT_US) }, // Smooth navigation applied
                    colors = itemColors,
                    modifier = itemPadding
                )

                val shareMessage =
                    stringResource(id = R.string.share_message, "https://satmeasure.web.app/share")
                NavigationDrawerItem(
                    label = {
                        Text(
                            stringResource(id = R.string.menu_share),
                            modifier = Modifier.padding(
                                start = dimensionResource(id = R.dimen.text_sm)
                            )
                        )
                    },
                    icon = { Icon(Icons.Default.Share, contentDescription = null) },
                    selected = false,
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    colors = itemColors,
                    modifier = itemPadding
                )
            }

            HorizontalDivider()
            Spacer(
                modifier = Modifier.height(
                    if (isLandscape) dimensionResource(
                        id = R.dimen.spacing_xs
                    ) else dimensionResource(id = R.dimen.corner_sm)
                )
            )

            NavigationDrawerItem(
                label = {
                    Text(
                        stringResource(id = R.string.title_settings),
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_sm))
                    )
                },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = currentRoute == SatMesRoutes.SETTINGS,
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                    onMenuSelect(SatMesRoutes.SETTINGS)
                }, // Smooth navigation applied
                colors = itemColors,
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.corner_sm),
                    end = dimensionResource(id = R.dimen.corner_sm),
                    bottom = if (isLandscape) dimensionResource(
                        id = R.dimen.spacing_xs
                    ) else dimensionResource(id = R.dimen.text_lg),
                    top = if (isLandscape) dimensionResource(
                        id = R.dimen.dimen_0
                    ) else dimensionResource(id = R.dimen.spacing_xs)
                )
            )
        }
    }
}
