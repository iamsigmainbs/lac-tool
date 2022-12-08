package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.ui.composable.LACToolButtonRounded
import com.aliernfrog.lactool.ui.composable.LACToolTextField
import com.aliernfrog.lactool.ui.dialog.DeleteMapDialog
import com.aliernfrog.lactool.util.staticutil.FileUtil
import kotlinx.coroutines.launch

@Composable
fun MapsScreen(mapsState: MapsState, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { mapsState.getMapsFile(context); mapsState.getImportedMaps(); mapsState.getExportedMaps() }
    Column(Modifier.fillMaxSize().verticalScroll(mapsState.scrollState)) {
        PickMapFileButton(mapsState)
        MapActions(mapsState, navController)
    }
    if (mapsState.mapDeleteDialogShown.value) DeleteMapDialog(
        mapName = mapsState.lastMapName.value,
        onDismissRequest = { mapsState.mapDeleteDialogShown.value = false },
        onConfirmDelete = {
            scope.launch {
                mapsState.deleteChosenMap(context)
                mapsState.mapDeleteDialogShown.value = false
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PickMapFileButton(mapsState: MapsState) {
    val scope = rememberCoroutineScope()
    LACToolButtonRounded(
        title = stringResource(R.string.manageMapsPickMap),
        painter = rememberVectorPainter(Icons.Default.PinDrop),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        scope.launch { mapsState.pickMapSheetState.show() }
    }
}

@Composable
private fun MapActions(mapsState: MapsState, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapChosen = mapsState.chosenMap.value != null
    val isImported = mapsState.chosenMap.value?.filePath?.startsWith(mapsState.mapsDir) ?: false
    val isExported = mapsState.chosenMap.value?.filePath?.startsWith(mapsState.mapsExportDir) ?: false
    val mapNameUpdated = mapsState.getMapNameEdit(false) != mapsState.chosenMap.value?.mapName
    MapActionVisibility(visible = mapChosen) {
        Column {
            LACToolTextField(
                value = mapsState.mapNameEdit.value,
                onValueChange = { mapsState.mapNameEdit.value = it },
                label = { Text(stringResource(R.string.manageMapsMapName)) },
                placeholder = { Text(mapsState.chosenMap.value!!.mapName) },
                leadingIcon = rememberVectorPainter(Icons.Rounded.TextFields),
                singleLine = true,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                doneIcon = rememberVectorPainter(Icons.Default.Edit),
                doneIconShown = isImported && mapNameUpdated,
                onDone = {
                    scope.launch { mapsState.renameChosenMap(context) }
                }
            )
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).alpha(0.7f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
    MapActionVisibility(visible = mapChosen && !isImported) {
        LACToolButtonRounded(
            title = stringResource(R.string.manageMapsImport),
            painter = rememberVectorPainter(Icons.Default.Download),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            scope.launch { mapsState.importChosenMap(context) }
        }
    }
    MapActionVisibility(visible = mapChosen && isImported) {
        LACToolButtonRounded(
            title = stringResource(R.string.manageMapsExport),
            painter = rememberVectorPainter(Icons.Default.Upload)
        ) {
            scope.launch { mapsState.exportChosenMap(context) }
        }
    }
    MapActionVisibility(visible = mapChosen) {
        LACToolButtonRounded(
            title = stringResource(R.string.manageMapsShare),
            painter = rememberVectorPainter(Icons.Default.IosShare)
        ) {
            FileUtil.shareFile(mapsState.chosenMap.value!!.filePath, "text/plain", context)
        }
    }
    MapActionVisibility(visible = mapChosen) {
        LACToolButtonRounded(
            title = stringResource(R.string.manageMapsEdit),
            description = stringResource(R.string.manageMapsEditDescription),
            painter = rememberVectorPainter(Icons.Default.Edit)
        ) {
            scope.launch { mapsState.editChosenMap(context, navController) }
        }
    }
    MapActionVisibility(visible = mapChosen && (isImported || isExported)) {
        LACToolButtonRounded(
            title = stringResource(R.string.manageMapsDelete),
            painter = rememberVectorPainter(Icons.Default.Delete),
            containerColor = MaterialTheme.colorScheme.error
        ) {
            mapsState.mapDeleteDialogShown.value = true
        }
    }
}

@Composable
private fun MapActionVisibility(visible: Boolean, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    return AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        content = content
    )
}