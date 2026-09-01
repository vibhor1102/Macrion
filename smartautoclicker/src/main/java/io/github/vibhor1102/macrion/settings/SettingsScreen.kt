/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.settings

import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vibhor1102.macrion.R

@Composable
internal fun SettingsRoute(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onShowPrivacySettings: () -> Unit,
    onShowPurchase: () -> Unit,
    onShowTroubleshooting: () -> Unit,
) {
    val isScenarioFiltersEnabled by viewModel.isScenarioFiltersUiEnabled.collectAsStateWithLifecycle(false)
    val isScenarioSwitcherEnabled by viewModel.isScenarioSwitcherEnabled.collectAsStateWithLifecycle(false)
    val isLegacyActionUiEnabled by viewModel.isLegacyActionUiEnabled.collectAsStateWithLifecycle(false)
    val isLegacyNotificationUiEnabled by viewModel.isLegacyNotificationUiEnabled.collectAsStateWithLifecycle(false)
    val isEntireScreenCaptureForced by viewModel.isEntireScreenCaptureForced.collectAsStateWithLifecycle(false)
    val isInputWorkaroundEnabled by viewModel.isInputWorkaroundEnabled.collectAsStateWithLifecycle(false)
    val shouldShowEntireScreenCapture by viewModel.shouldShowEntireScreenCapture.collectAsStateWithLifecycle(false)
    val shouldShowInputBlockWorkaround by viewModel.shouldShowInputBlockWorkaround.collectAsStateWithLifecycle(false)
    val shouldShowPrivacySettings by viewModel.shouldShowPrivacySettings.collectAsStateWithLifecycle(false)
    val shouldShowPurchase by viewModel.shouldShowPurchase.collectAsStateWithLifecycle(false)

    MacrionComposeTheme {
        SettingsScreen(
            items = buildList {
                add(SettingsItem.Switch(R.string.field_show_scenario_filters_ui_title, R.string.field_show_scenario_filters_ui_desc, isScenarioFiltersEnabled, viewModel::toggleScenarioFiltersUi))
                add(SettingsItem.Switch(R.string.field_scenario_switcher_title, R.string.field_scenario_switcher_desc, isScenarioSwitcherEnabled, viewModel::toggleScenarioSwitcher))
                add(SettingsItem.Switch(R.string.field_legacy_action_ui_title, R.string.field_legacy_action_ui_desc, isLegacyActionUiEnabled, viewModel::toggleLegacyActionUi))
                add(SettingsItem.Switch(R.string.field_legacy_notification_ui_title, R.string.field_legacy_notification_ui_desc, isLegacyNotificationUiEnabled, viewModel::toggleLegacyNotificationUi))
                if (shouldShowEntireScreenCapture) add(SettingsItem.Switch(R.string.field_force_entire_screen_title, R.string.field_force_entire_screen_desc, isEntireScreenCaptureForced, viewModel::toggleForceEntireScreenCapture))
                if (shouldShowInputBlockWorkaround) add(SettingsItem.Switch(R.string.field_input_block_workaround_title, R.string.field_input_block_workaround_desc, isInputWorkaroundEnabled, viewModel::toggleInputBlockWorkaround))
                if (shouldShowPrivacySettings) add(SettingsItem.Action(R.string.field_privacy, onShowPrivacySettings))
                if (shouldShowPurchase) add(SettingsItem.Action(R.string.field_remove_ads, onShowPurchase))
                add(SettingsItem.Action(R.string.field_troubleshooting, onShowTroubleshooting))
            },
            onNavigateBack = onNavigateBack,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsScreen(items: List<SettingsItem>, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activity_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.content_desc_back),
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            items(items) { item ->
                SettingsRow(item)
                if (item !== items.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = DividerDefaults.color,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    val interactionModifier = when (item) {
        is SettingsItem.Switch -> Modifier.toggleable(
            value = item.checked,
            role = Role.Switch,
            onValueChange = { item.onClick() },
        )
        is SettingsItem.Action -> Modifier.clickable(
            role = Role.Button,
            onClick = item.onClick,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(item.title),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (item is SettingsItem.Switch) {
                Text(
                    text = stringResource(item.description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        when (item) {
            is SettingsItem.Switch -> Switch(
                checked = item.checked,
                onCheckedChange = null,
            )
            is SettingsItem.Action -> Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
            )
        }
    }
}

private sealed interface SettingsItem {
    @get:StringRes val title: Int
    val onClick: () -> Unit

    data class Switch(
        @param:StringRes override val title: Int,
        @param:StringRes val description: Int,
        val checked: Boolean,
        override val onClick: () -> Unit,
    ) : SettingsItem

    data class Action(
        @param:StringRes override val title: Int,
        override val onClick: () -> Unit,
    ) : SettingsItem
}

@Composable
private fun MacrionComposeTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
            dynamicDarkColorScheme(LocalContext.current)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicLightColorScheme(LocalContext.current)
        darkTheme -> macrionDarkColorScheme()
        else -> macrionLightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
private fun macrionDarkColorScheme() =
        darkColorScheme(
            primary = composeColor(R.color.md_theme_dark_primary),
            onPrimary = composeColor(R.color.md_theme_dark_onPrimary),
            primaryContainer = composeColor(R.color.md_theme_dark_primaryContainer),
            onPrimaryContainer = composeColor(R.color.md_theme_dark_onPrimaryContainer),
            secondary = composeColor(R.color.md_theme_dark_secondary),
            onSecondary = composeColor(R.color.md_theme_dark_onSecondary),
            secondaryContainer = composeColor(R.color.md_theme_dark_secondaryContainer),
            onSecondaryContainer = composeColor(R.color.md_theme_dark_onSecondaryContainer),
            background = composeColor(R.color.md_theme_dark_background),
            onBackground = composeColor(R.color.md_theme_dark_onBackground),
            surface = composeColor(R.color.md_theme_dark_surface),
            onSurface = composeColor(R.color.md_theme_dark_onSurface),
            surfaceVariant = composeColor(R.color.md_theme_dark_surfaceVariant),
            onSurfaceVariant = composeColor(R.color.md_theme_dark_onSurfaceVariant),
            outline = composeColor(R.color.md_theme_dark_outline),
        )

@Composable
private fun macrionLightColorScheme() =
        lightColorScheme(
            primary = composeColor(R.color.md_theme_light_primary),
            onPrimary = composeColor(R.color.md_theme_light_onPrimary),
            primaryContainer = composeColor(R.color.md_theme_light_primaryContainer),
            onPrimaryContainer = composeColor(R.color.md_theme_light_onPrimaryContainer),
            secondary = composeColor(R.color.md_theme_light_secondary),
            onSecondary = composeColor(R.color.md_theme_light_onSecondary),
            secondaryContainer = composeColor(R.color.md_theme_light_secondaryContainer),
            onSecondaryContainer = composeColor(R.color.md_theme_light_onSecondaryContainer),
            background = composeColor(R.color.md_theme_light_background),
            onBackground = composeColor(R.color.md_theme_light_onBackground),
            surface = composeColor(R.color.md_theme_light_surface),
            onSurface = composeColor(R.color.md_theme_light_onSurface),
            surfaceVariant = composeColor(R.color.md_theme_light_surfaceVariant),
            onSurfaceVariant = composeColor(R.color.md_theme_light_onSurfaceVariant),
            outline = composeColor(R.color.md_theme_light_outline),
        )

@Composable
private fun composeColor(@ColorRes color: Int): Color = colorResource(color)
