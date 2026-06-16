package com.example.satmeasure.ui.otherScreens

import android.widget.Toast

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat.forLanguageTags
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
    val hapticsEnabled by settingsManager.hapticsFlow.collectAsState(initial = true)
    val currentUser by authViewModel.uiState.collectAsState()

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val (showDeleteDataDialog, setShowDeleteDataDialog) = remember { mutableStateOf(false) }
    val (showDeleteAccountDialog, setShowDeleteAccountDialog) = remember { mutableStateOf(false) }


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isLandscape) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.title_settings),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
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
            }
        }
    ) { paddingValues ->
        val settingsContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.then(
                        if (isLandscape) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()
                    )
                ) {

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_sm_minus)))
                    
                    AppLanguageCard(
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_sm_minus))
                    )

                    HapticFeedbackCard(
                        isDarkTheme = isDarkTheme,
                        hapticsEnabled = hapticsEnabled,
                        onHapticsChange = {
                            coroutineScope.launch {
                                settingsManager.setHaptics(it)
                            }
                        },
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_sm_minus))
                    )

                    // Remove Ads
                    RemoveAdsCard(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.text_sm)), isDarkTheme = isDarkTheme)

                    AppearanceCard(
                        dynamicColor = dynamicColor,
                        themeMode = themeMode,
                        isDarkTheme = isDarkTheme,
                        onDynamicColorChange = {
                            coroutineScope.launch {
                                settingsManager.setDynamicColor(
                                    it
                                )
                            }
                        },
                        onThemeModeChange = { mode ->
                            coroutineScope.launch {
                                settingsManager.setThemeMode(
                                    mode
                                )
                            }
                        },
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_sm_minus))
                    )



                    // --- ACCOUNT CATEGORY ---
                    AccountCard(
                        onDeleteDataClick = { setShowDeleteDataDialog(true) },
                        onDeleteAccountClick = { setShowDeleteAccountDialog(true) },
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_xs))
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            settingsContent()

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.spacing_xs), vertical = dimensionResource(id = R.dimen.corner_sm))
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = stringResource(id = R.string.cd_back)
                        )
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.corner_sm)))
                    Text(
                        text = stringResource(id = R.string.title_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
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

@Composable
fun AppearanceCard(
    dynamicColor: Boolean,
    themeMode: ThemeMode,
    isDarkTheme: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_lg)
            ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_36)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.appearance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_lg), top = dimensionResource(id = R.dimen.text_sm), bottom = dimensionResource(id = R.dimen.corner_sm))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(id = R.dimen.spacing_sm_minus), end = dimensionResource(id = R.dimen.spacing_sm_minus), bottom = dimensionResource(id = R.dimen.spacing_sm_minus)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.corner_sm))
            ) {
                SettingsToggleCard(
                    title = stringResource(id = R.string.theme_mode),
                    subtitle = when (themeMode) {
                        ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
                        ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
                        ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
                    },
                    icon = Icons.Outlined.DarkMode,
                    checked = true,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    actionContent = {
                        var themeMenuExpanded by remember { mutableStateOf(false) }
                        var pillSize by remember { mutableStateOf(IntSize.Zero) }

                        Box(
                            modifier = Modifier
                                .padding(end = dimensionResource(id = R.dimen.dimen_7), bottom = dimensionResource(id = R.dimen.dimen_7))
                                .onGloballyPositioned { pillSize = it.size }
                        ) {
                            if (!themeMenuExpanded) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable { 
                                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                            themeMenuExpanded = true 
                                        }
                                        .padding(horizontal = dimensionResource(id = R.dimen.text_lg), vertical = dimensionResource(id = R.dimen.spacing_sm_minus)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (themeMode) {
                                            ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
                                            ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
                                            ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { pillSize.width.toDp() })
                                        .height(
                                            with(LocalDensity.current) { pillSize.height.toDp() })
                                )
                            }

                            if (themeMenuExpanded) {
                                Popup(
                                    alignment = Alignment.BottomCenter,
                                    onDismissRequest = { themeMenuExpanded = false },
                                    properties = PopupProperties(focusable = true)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(
                                                with(
                                                    LocalDensity.current
                                                ) { pillSize.width.toDp() })
                                            .border(
                                                dimensionResource(id = R.dimen.dimen_1), MaterialTheme.colorScheme.onSurfaceVariant,
                                                RoundedCornerShape(dimensionResource(id = R.dimen.text_lg))
                                            )
                                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.text_lg)))
                                            .background(
                                                MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                            .padding(dimensionResource(id = R.dimen.spacing_xs)),
                                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_xs))
                                    ) {
                                        val options = listOf(
                                            ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM
                                        )
                                        val labels = listOf(stringResource(id = R.string.theme_light), stringResource(id = R.string.theme_dark), stringResource(id = R.string.theme_system))
                                        options.forEachIndexed { index, mode ->
                                            val isSelected = themeMode == mode
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        HapticHelper.trigger(
                                                            context, HapticHelper.Type.LIGHT
                                                        )
                                                        onThemeModeChange(mode)
                                                        themeMenuExpanded = false
                                                    }
                                                    .then(
                                                        if (isSelected) Modifier.background(
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                        else Modifier
                                                    )
                                                    .padding(vertical = dimensionResource(id = R.dimen.spacing_sm_minus)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = labels[index],
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                SettingsToggleCard(
                    title = stringResource(id = R.string.title_dynamic_color),
                    subtitle = stringResource(id = R.string.desc_dynamic_color),
                    icon = Icons.Default.Palette,
                    checked = dynamicColor,
                    isDarkTheme = isDarkTheme,
                    onCheckedChange = onDynamicColorChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SettingsToggleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean = false,
    isDarkTheme: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    actionContent: @Composable () -> Unit = {
        val context = LocalContext.current
        if (onCheckedChange != null) {
            Switch(
                checked = checked,
                onCheckedChange = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onCheckedChange(it)
                },
                modifier = Modifier.scale(0.85f)
            )
        }
    }
) {

    val iconBgColor =
        if (checked) {
            if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.copy(
                alpha = 0.9f
            )
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    val iconColor =
        if (checked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_36)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .padding(start = dimensionResource(id = R.dimen.spacing_sm_minus), top = dimensionResource(id = R.dimen.spacing_sm_minus))
                        .size(dimensionResource(id = R.dimen.dimen_60))
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_lg))
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.text_sm)))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.text_sm))
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xxs)))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.text_sm))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(id = R.dimen.spacing_sm_minus), end = dimensionResource(id = R.dimen.spacing_sm_minus), bottom = dimensionResource(id = R.dimen.spacing_sm_minus)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                actionContent()
            }
        }
    }
}

@Composable
fun AppLanguageCard(isDarkTheme: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentLocale = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
    val appLanguage = if (currentLocale.contains("hi")) stringResource(
        id = R.string.lang_hindi
    ) else stringResource(id = R.string.lang_english)
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_lg)
            ),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.text_sm), vertical = dimensionResource(id = R.dimen.corner_sm)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val iconBgColor =
                    if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.copy(
                        alpha = 0.9f
                    )
                val iconColor = MaterialTheme.colorScheme.surface

                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.dimen_42))
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxl))
                    )
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.text_sm)))

                Text(
                    text = stringResource(id = R.string.title_app_language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            var languagePillSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .padding(end = dimensionResource(id = R.dimen.spacing_xs))
                    .onGloballyPositioned { languagePillSize = it.size }
            ) {
                if (!languageMenuExpanded) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(dimensionResource(id = R.dimen.dimen_1), MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { 
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                languageMenuExpanded = true 
                            }
                            .padding(horizontal = dimensionResource(id = R.dimen.text_lg), vertical = dimensionResource(id = R.dimen.spacing_sm_minus)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appLanguage,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(
                        modifier = Modifier
                            .width(with(LocalDensity.current) { languagePillSize.width.toDp() })
                            .height(with(LocalDensity.current) { languagePillSize.height.toDp() })
                    )
                }

                if (languageMenuExpanded) {
                    Popup(
                        alignment = Alignment.BottomCenter,
                        onDismissRequest = { languageMenuExpanded = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(
                                    with(
                                        LocalDensity.current
                                    ) { languagePillSize.width.toDp() }.let { if (it < dimensionResource(id = R.dimen.dimen_100)) dimensionResource(id = R.dimen.dimen_100) else it })
                                .border(
                                    dimensionResource(id = R.dimen.dimen_1), MaterialTheme.colorScheme.onSurfaceVariant,
                                    RoundedCornerShape(dimensionResource(id = R.dimen.text_lg))
                                )
                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.text_lg)))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(dimensionResource(id = R.dimen.spacing_xs)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_xs))
                        ) {
                            val options = listOf("en", "hi")
                            val labels = listOf(
                                stringResource(id = R.string.lang_english),
                                stringResource(id = R.string.lang_hindi)
                            )

                            options.forEachIndexed { index, langTag ->
                                val isSelected = currentLocale.contains(
                                    langTag
                                ) || (langTag == "en" && !currentLocale.contains("hi"))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CircleShape)
                                        .clickable {
                                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                            AppCompatDelegate.setApplicationLocales(
                                                forLanguageTags(langTag)
                                            )
                                            languageMenuExpanded = false
                                        }
                                        .then(
                                            if (isSelected) Modifier.background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                            else Modifier
                                        )
                                        .padding(vertical = dimensionResource(id = R.dimen.spacing_sm_minus)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = labels[index],
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HapticFeedbackCard(
    isDarkTheme: Boolean,
    hapticsEnabled: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_lg)
            ),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.text_sm), vertical = dimensionResource(id = R.dimen.corner_sm)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val iconBgColor = if (hapticsEnabled) {
                    if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.copy(
                        alpha = 0.9f
                    )
                } else {
                    MaterialTheme.colorScheme.background
                }
                val iconColor =
                    if (hapticsEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.dimen_42))
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Vibration,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxl))
                    )
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.text_sm)))

                Text(
                    text = stringResource(id = R.string.haptic_feedback),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val context = LocalContext.current
            Switch(
                checked = hapticsEnabled,
                onCheckedChange = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onHapticsChange(it)
                },
                modifier = Modifier
                    .padding(end = dimensionResource(id = R.dimen.spacing_xs))
                    .scale(0.85f)
            )
        }
    }
}
data class WavyShape(
    private val periodDp: Float = 80f,
    private val amplitudeDp: Float = 2.5f,
    private val cornerRadiusDp: Float = 24f,
    private val phaseOffset: Float = 0f
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val period = periodDp * density.density
        val amplitude = amplitudeDp * density.density
        val r = cornerRadiusDp * density.density
        val path = Path()
        
        val w = size.width
        val h = size.height
        
        val actualR = kotlin.math.min(r, kotlin.math.min(w / 2, h / 2))
        
        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(0f, 0f, 2 * actualR, 2 * actualR),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        
        val steps = 40
        
        val topLen = w - 2 * actualR
        val wavesX = kotlin.math.max(1, kotlin.math.round(topLen / period).toInt())
        for (i in 1..(wavesX * steps)) {
            val frac = i / (wavesX * steps).toFloat()
            val x = actualR + frac * topLen
            val env = kotlin.math.sin(frac * kotlin.math.PI).toFloat()
            val currentAmp = env * amplitude
            val y = currentAmp - currentAmp * kotlin.math.cos((frac * wavesX - phaseOffset) * 2 * kotlin.math.PI).toFloat()
            path.lineTo(x, y)
        }
        
        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(w - 2 * actualR, 0f, w, 2 * actualR),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        
        val rightLen = h - 2 * actualR
        val wavesY = kotlin.math.max(1, kotlin.math.round(rightLen / period).toInt())
        for (i in 1..(wavesY * steps)) {
            val frac = i / (wavesY * steps).toFloat()
            val y = actualR + frac * rightLen
            val env = kotlin.math.sin(frac * kotlin.math.PI).toFloat()
            val currentAmp = env * amplitude
            val x = w - currentAmp + currentAmp * kotlin.math.cos((frac * wavesY - phaseOffset) * 2 * kotlin.math.PI).toFloat()
            path.lineTo(x, y)
        }
        
        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(w - 2 * actualR, h - 2 * actualR, w, h),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        
        for (i in 1..(wavesX * steps)) {
            val frac = i / (wavesX * steps).toFloat()
            val x = w - actualR - frac * topLen
            val env = kotlin.math.sin(frac * kotlin.math.PI).toFloat()
            val currentAmp = env * amplitude
            val y = h - currentAmp + currentAmp * kotlin.math.cos((frac * wavesX - phaseOffset) * 2 * kotlin.math.PI).toFloat()
            path.lineTo(x, y)
        }
        
        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(0f, h - 2 * actualR, 2 * actualR, h),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        
        for (i in 1..(wavesY * steps)) {
            val frac = i / (wavesY * steps).toFloat()
            val y = h - actualR - frac * rightLen
            val env = kotlin.math.sin(frac * kotlin.math.PI).toFloat()
            val currentAmp = env * amplitude
            val x = currentAmp - currentAmp * kotlin.math.cos((frac * wavesY - phaseOffset) * 2 * kotlin.math.PI).toFloat()
            path.lineTo(x, y)
        }
        
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun RemoveAdsCard(modifier: Modifier = Modifier, isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.dimen_120)) // roughly 2x AppLanguageCard height
            .padding(horizontal = dimensionResource(id = R.dimen.spacing_lg)),
        shape = WavyShape(phaseOffset = phase),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(dimensionResource(id = R.dimen.spacing_xs), MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { /* TODO: Implement Remove Ads */ }
                .padding(dimensionResource(id = R.dimen.text_lg)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_xl))
                        .clip(CircleShape)
                        .background(if (isDarkTheme) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.copy(
                            alpha = 0.9f
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Block, contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxxl))
                    )
                }
                Spacer(
                    modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_md))
                )
                Column {
                    Text(
                        stringResource(id = R.string.remove_ads),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xxs)))
                    Text(
                        "Enjoy an ad-free premium experience",
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

@Composable
fun AccountCard(
    onDeleteDataClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_lg)
            ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_36)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_sm_minus))
        ) {
            Text(
                text = stringResource(id = R.string.account),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.text_lg), top = dimensionResource(id = R.dimen.text_sm), bottom = dimensionResource(id = R.dimen.corner_sm))
            )

            Card(
                onClick = onDeleteDataClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.dimen_70))
                    .padding(horizontal = dimensionResource(id = R.dimen.corner_sm)),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensionResource(id = R.dimen.corner_sm)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.icon_xl))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Delete, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxxl))
                        )
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_md)))
                    Column(modifier = Modifier.weight(1f)) {
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
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.corner_sm)))

            Card(
                onClick = onDeleteAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.dimen_70))
                    .padding(horizontal = dimensionResource(id = R.dimen.corner_sm)),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensionResource(id = R.dimen.corner_sm)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.icon_xl))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.PersonRemove, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.text_xxxl))
                        )
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_md)))
                    Column(modifier = Modifier.weight(1f)) {
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
            }
        }
    }
}
