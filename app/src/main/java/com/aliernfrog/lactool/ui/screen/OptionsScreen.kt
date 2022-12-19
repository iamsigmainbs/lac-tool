package com.aliernfrog.lactool.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.Link
import com.aliernfrog.lactool.MainActivity
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.state.OptionsState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.theme.supportsMaterialYou
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.state.TopToastState

private const val experimentalRequiredClicks = 10

@Composable
fun OptionsScreen(config: SharedPreferences, topToastState: TopToastState, optionsState: OptionsState) {
    Column(Modifier.fillMaxSize().verticalScroll(optionsState.scrollState)) {
        AppearanceOptions(optionsState)
        MapsOptions(optionsState)
        AboutApp(topToastState, optionsState)
        if (optionsState.aboutClickCount.value >= experimentalRequiredClicks) ExperimentalOptions(config, optionsState)
    }
}

@Composable
private fun AppearanceOptions(optionsState: OptionsState) {
    val themeOptions = listOf(
        stringResource(R.string.optionsThemeSystem),
        stringResource(R.string.optionsThemeLight),
        stringResource(R.string.optionsThemeDark)
    )
    ColumnDivider(title = stringResource(R.string.options_appearance)) {
        ButtonShapeless(
            title = stringResource(R.string.options_theme),
            description = stringResource(R.string.options_theme_description),
            expanded = optionsState.themeOptionsExpanded.value
        ) {
            optionsState.themeOptionsExpanded.value = !optionsState.themeOptionsExpanded.value
        }
        AnimatedVisibility(
            visible = optionsState.themeOptionsExpanded.value,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                RadioButtons(
                    options = themeOptions,
                    initialIndex = optionsState.theme.value,
                    optionsRounded = true
                ) {
                    optionsState.setTheme(it)
                }
            }
        }
        if (optionsState.forceShowMaterialYouOption.value || supportsMaterialYou) Switch(
            title = stringResource(R.string.optionsThemeMaterialYou),
            description = stringResource(R.string.optionsThemeMaterialYouDescription),
            checked = optionsState.materialYou.value
        ) {
            optionsState.setMaterialYou(it)
        }
    }
}

@Composable
private fun MapsOptions(optionsState: OptionsState) {
    ColumnDivider(title = stringResource(R.string.optionsMaps)) {
        Switch(
            title = stringResource(R.string.optionsMapsShowMapThumbnailsList),
            description = stringResource(R.string.optionsMapsShowMapThumbnailsListDescription),
            checked = optionsState.showMapThumbnailsInList.value
        ) {
            optionsState.setShowMapThumbnailsInList(it)
        }
    }
}

@Composable
private fun AboutApp(topToastState: TopToastState, optionsState: OptionsState) {
    val context = LocalContext.current
    val version = "v${GeneralUtil.getAppVersionName(context)} (${GeneralUtil.getAppVersionCode(context)})"
    ColumnDivider(title = stringResource(R.string.optionsAbout), bottomDivider = false) {
        ButtonShapeless(title = stringResource(R.string.optionsAboutVersion), description = version) {
            optionsState.aboutClickCount.value++
            if (optionsState.aboutClickCount.value == experimentalRequiredClicks) topToastState.showToast(context.getString(R.string.optionsExperimentalEnabled))
        }
        Links(optionsState)
    }
}

@Composable
private fun Links(optionsState: OptionsState) {
    val uriHandler = LocalUriHandler.current
    ButtonShapeless(title = stringResource(R.string.optionsAboutLinks), description = stringResource(R.string.optionsAboutLinksDescription), expanded = optionsState.linksExpanded.value) {
        optionsState.linksExpanded.value = !optionsState.linksExpanded.value
    }
    AnimatedVisibility(
        visible = optionsState.linksExpanded.value,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
            Link.socials.forEach {
                val icon = when(it.url.split("/")[2]) {
                    "discord.gg" -> painterResource(id = R.drawable.discord)
                    "github.com" -> painterResource(id = R.drawable.github)
                    else -> null
                }
                ButtonShapeless(title = it.name, painter = icon, rounded = true, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) { uriHandler.openUri(it.url) }
            }
        }
    }
}

@Composable
private fun ExperimentalOptions(config: SharedPreferences, optionsState: OptionsState) {
    val context = LocalContext.current
    val configEditor = config.edit()
    val prefEdits = listOf(
        PrefEditItem(ConfigKey.KEY_MAPS_DIR, ConfigKey.DEFAULT_MAPS_DIR),
        PrefEditItem(ConfigKey.KEY_MAPS_EXPORT_DIR, ConfigKey.DEFAULT_MAPS_EXPORT_DIR)
    )
    ColumnDivider(title = stringResource(R.string.optionsExperimental), bottomDivider = false, topDivider = true) {
        Text(stringResource(R.string.optionsExperimentalDescription), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        Switch(
            title = stringResource(R.string.optionsExperimentalShowMaterialYouOption),
            checked = optionsState.forceShowMaterialYouOption.value,
            onCheckedChange = {
                optionsState.forceShowMaterialYouOption.value = it
            }
        )
        prefEdits.forEach { prefEdit ->
            val value = remember { mutableStateOf(config.getString(prefEdit.key, prefEdit.default)!!) }
            TextField(
                label = { Text(text = "Prefs: ${prefEdit.key}") },
                value = value.value,
                modifier = Modifier.padding(horizontal = 8.dp),
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                rounded = false,
                onValueChange = {
                    value.value = it
                    configEditor.putString(prefEdit.key, it)
                    configEditor.apply()
                }
            )
        }
        ButtonShapeless(title = stringResource(R.string.optionsExperimentalResetPrefs), contentColor = MaterialTheme.colorScheme.error) {
            prefEdits.forEach {
                configEditor.remove(it.key)
                configEditor.apply()
            }
            restartApp(context)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    (context as Activity).finish()
    context.startActivity(intent)
}