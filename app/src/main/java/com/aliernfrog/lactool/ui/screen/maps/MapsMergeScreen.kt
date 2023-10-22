package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapToMerge
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
import com.aliernfrog.lactool.ui.component.maps.MapToMerge
import com.aliernfrog.lactool.ui.dialog.MergeMapDialog
import com.aliernfrog.lactool.ui.sheet.PickMapSheet
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMergeScreen(
    mapsMergeViewModel: MapsMergeViewModel = getViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        mapsMergeViewModel.loadMaps()
    }

    AppScaffold(
        title = stringResource(R.string.mapsMerge),
        topAppBarState = mapsMergeViewModel.topAppBarState,
        floatingActionButton = {
            AnimatedVisibility(
                visible = mapsMergeViewModel.hasEnoughMaps && !mapsMergeViewModel.mergeMapDialogShown,
                modifier = Modifier.systemBarsPadding(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { mapsMergeViewModel.mergeMapDialogShown = true },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Build,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.mapsMerge_merge))
                }
            }
        },
        onBackClick = {
            onNavigateBackRequest()
        }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsMergeViewModel.scrollState)) {
            PickMapButton {
                scope.launch { mapsMergeViewModel.pickMapSheetState.show() }
            }
            MapsList(
                maps = mapsMergeViewModel.mapMerger.mapsToMerge
            )
            Spacer(Modifier.systemBarsPadding().height(70.dp))
        }
    }

    if (mapsMergeViewModel.mergeMapDialogShown) MergeMapDialog(
        isMerging = mapsMergeViewModel.isMerging,
        onDismissRequest = { mapsMergeViewModel.mergeMapDialogShown = false },
        onConfirm = { newMapName ->
            scope.launch {
                mapsMergeViewModel.mergeMaps(
                    context = context,
                    newMapName = newMapName,
                    onNavigateBackRequest = onNavigateBackRequest
                )
            }
        }
    )

    PickMapSheet(
        sheetState = mapsMergeViewModel.pickMapSheetState,
        onMapPick = {
            scope.launch {
                mapsMergeViewModel.addMap(it, context)
            }
            true
        }
    )
}

@Composable
private fun MapsList(
    maps: List<LACMapToMerge>
) {
    val baseMap = maps.firstOrNull()
    val mapsToMerge = maps.toList().drop(1)
    AnimatedVisibility(baseMap != null) {
        MapButtonWithActions(
            mapToMerge = baseMap ?: LACMapToMerge("-", "-"),
            mapIndex = 0
        )
    }
    AnimatedVisibility(mapsToMerge.isNotEmpty()) {
        ColumnRounded(
            title = stringResource(R.string.mapsMerge_mapsToMerge)
        ) {
            mapsToMerge.forEachIndexed { index, map ->
                MapButtonWithActions(
                    mapToMerge = map,
                    mapIndex = index+1,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
private fun PickMapButton(
    onClick: () -> Unit
) {
    RoundedButtonRow(
        title = stringResource(R.string.mapsMerge_addMap),
        painter = rememberVectorPainter(Icons.Rounded.AddLocationAlt),
        containerColor = MaterialTheme.colorScheme.primary,
        onClick = onClick
    )
}

@Composable
private fun MapButtonWithActions(
    mapsMergeViewModel: MapsMergeViewModel = getViewModel(),
    mapToMerge: LACMapToMerge,
    mapIndex: Int,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val context = LocalContext.current
    val isBase = mapIndex == 0
    val expanded = mapsMergeViewModel.optionsExpandedFor == mapIndex
    MapToMerge(
        mapToMerge = mapToMerge,
        isBaseMap = isBase,
        expanded = expanded || isBase,
        showExpandedIndicator = !isBase,
        containerColor = containerColor,
        onUpdateState = { mapsMergeViewModel.updateMergerState() },
        onMakeBase = { mapsMergeViewModel.makeMapBase(mapIndex, mapToMerge.mapName, context) },
        onRemove = { mapsMergeViewModel.removeMap(mapIndex, mapToMerge.mapName, context) },
        onClick = {
            if (!isBase) {
                mapsMergeViewModel.optionsExpandedFor = if (expanded) 0
                else mapIndex
            }
        }
    )
}