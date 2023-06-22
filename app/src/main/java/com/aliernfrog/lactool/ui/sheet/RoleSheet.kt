package com.aliernfrog.lactool.ui.sheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.maps.MapRole
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RoleSheet(
    role: String,
    state: ModalBottomSheetState,
    topToastState: TopToastState? = null,
    onDeleteRole: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    AppModalBottomSheet(sheetState = state) {
        FormSection(null) {
            MapRole(role, true) {}
        }
        FormSection(null, bottomDivider = false) {
            ButtonRow(
                title = stringResource(R.string.mapsRoles_copyRoleName),
                painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
            ) {
                clipboardManager.setText(AnnotatedString(role.removeHtml()))
                topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
                scope.launch { state.hide() }
            }
            ButtonRow(
                title = stringResource(R.string.mapsRoles_copyRoleRaw),
                painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
            ) {
                clipboardManager.setText(AnnotatedString(role))
                topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
                scope.launch { state.hide() }
            }
            ButtonRow(
                title = stringResource(R.string.mapsRoles_deleteRole),
                painter = rememberVectorPainter(Icons.Rounded.Delete),
                contentColor = MaterialTheme.colorScheme.error
            ) {
                onDeleteRole(role)
                scope.launch { state.hide() }
            }
        }
    }
}