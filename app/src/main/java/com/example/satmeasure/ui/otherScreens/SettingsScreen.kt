package com.example.satmeasure.ui.otherScreens

import android.widget.Toast

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.text.font.FontWeight
import com.example.satmeasure.R
import com.example.satmeasure.data.SettingsManager
import com.example.satmeasure.data.ThemeMode
import com.example.satmeasure.ui.viewmodel.AuthViewModel
import com.example.satmeasure.ui.viewmodel.MapViewModel
import com.example.satmeasure.ui.components.DeleteDataWarningDialog
import com.example.satmeasure.ui.components.DeleteAccountWarningDialog
import com.example.satmeasure.utils.HapticHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    mapViewModel: MapViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }

    val themeMode by settingsManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColor by settingsManager.dynamicColorFlow.collectAsState(initial = false)
    val currentUser by authViewModel.uiState.collectAsState()

    val (showDeleteDataDialog, setShowDeleteDataDialog) = remember { mutableStateOf(false) }
    val (showDeleteAccountDialog, setShowDeleteAccountDialog) = remember { mutableStateOf(false) }

    val currentLocale = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
    val appLanguage = if (currentLocale.contains("hi")) stringResource(
        id = R.string.lang_hindi
    ) else stringResource(id = R.string.lang_english)
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.title_settings),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onBackClick()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = stringResource(id = R.string.cd_back)
                            )
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(dimensionResource(id = R.dimen.spacing_md)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_lg))
        ) {
            // Appearance Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_lg)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_sm))) {
                    // Theme Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.spacing_xxxl))
                            .clip(RoundedCornerShape(50)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // System
                        val systemSelected = themeMode == ThemeMode.SYSTEM
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (systemSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        settingsManager.setThemeMode(
                                            ThemeMode.SYSTEM
                                        )
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(id = R.string.theme_system),
                                tint = if (systemSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_sm))
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_sm_minus)
                                )
                            )
                            Text(
                                stringResource(id = R.string.theme_system),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (systemSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Light
                        val lightSelected = themeMode == ThemeMode.LIGHT
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (lightSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        settingsManager.setThemeMode(
                                            ThemeMode.LIGHT
                                        )
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.LightMode,
                                contentDescription = stringResource(id = R.string.theme_light),
                                tint = if (lightSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_sm))
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_sm_minus)
                                )
                            )
                            Text(
                                stringResource(id = R.string.theme_light),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (lightSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Dark
                        val darkSelected = themeMode == ThemeMode.DARK
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (darkSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        settingsManager.setThemeMode(
                                            ThemeMode.DARK
                                        )
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.DarkMode,
                                contentDescription = stringResource(id = R.string.theme_dark),
                                tint = if (darkSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_sm))
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_sm_minus)
                                )
                            )
                            Text(
                                stringResource(id = R.string.theme_dark),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (darkSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))

                    // Dynamic Color
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.spacing_sm),
                                vertical = dimensionResource(id = R.dimen.spacing_sm)
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_md)
                                )
                            )
                            Column {
                                Text(
                                    stringResource(id = R.string.title_dynamic_color),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    stringResource(id = R.string.desc_dynamic_color),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = {
                                coroutineScope.launch { settingsManager.setDynamicColor(it) }
                            }
                        )
                    }
                }
            }

            // Danger Zone Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_lg)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.spacing_sm)
                    )
                ) {
                    // Delete My Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { setShowDeleteDataDialog(true) }
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.spacing_md),
                                vertical = dimensionResource(id = R.dimen.spacing_md)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Outlined.Delete, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_md)
                                )
                            )
                            Column {
                                Text(
                                    stringResource(id = R.string.title_delete_my_data),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    stringResource(id = R.string.desc_delete_my_data),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.spacing_md)
                        )
                    )

                    // Delete Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { setShowDeleteAccountDialog(true) }
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.spacing_md),
                                vertical = dimensionResource(id = R.dimen.spacing_md)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Outlined.PersonRemove, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_md)
                                )
                            )
                            Column {
                                Text(
                                    stringResource(id = R.string.title_delete_my_account),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    stringResource(id = R.string.desc_delete_my_account),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // App Language Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_lg)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { languageMenuExpanded = true }
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.spacing_md),
                            vertical = dimensionResource(id = R.dimen.spacing_md)
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.title_app_language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                appLanguage, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(id = R.dimen.spacing_xs)
                                )
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.lang_english)) },
                                onClick = {
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags("en")
                                    )
                                    languageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.lang_hindi)) },
                                onClick = {
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags("hi")
                                    )
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val msgDataDeletedSuccessfully = stringResource(id = R.string.msg_data_deleted_successfully)
    if (showDeleteDataDialog) {
        DeleteDataWarningDialog(
            onDismiss = { setShowDeleteDataDialog(false) },
            onConfirm = {
                currentUser.currentUser?.uid?.let { uid ->
                    mapViewModel.deleteAllMeasurements(uid)
                    Toast.makeText(context, msgDataDeletedSuccessfully, Toast.LENGTH_SHORT)
                        .show()
                }
                setShowDeleteDataDialog(false)
            }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountWarningDialog(
            onDismiss = { setShowDeleteAccountDialog(false) },
            onConfirm = {
                authViewModel.deleteAccount { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT)
                        .show()
                    if (success) {
                        onBackClick()
                    }
                }
                setShowDeleteAccountDialog(false)
            }
        )
    }
}