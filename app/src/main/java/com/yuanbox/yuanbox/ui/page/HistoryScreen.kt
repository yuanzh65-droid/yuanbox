@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.yuanbox.yuanbox.ui.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.api.AppBootstrap
import com.yuanbox.yuanbox.bean.VodInfo
import com.yuanbox.yuanbox.ui.components.AppTopBarScaffold
import com.yuanbox.yuanbox.ui.components.ProvideTmdbPosterLookup

private sealed interface HistoryScreenDialog {
    data object ClearHistory : HistoryScreenDialog
    data object DeleteSelected : HistoryScreenDialog
    data class DeleteItem(val item: VodInfo) : HistoryScreenDialog
}

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val vm: HistoryViewModel = viewModel()
    val navStart = contentPadding.calculateStartPadding(LocalLayoutDirection.current)
    val navBottom = contentPadding.calculateBottomPadding()

    val items by vm.items.collectAsStateWithLifecycle()
    val incognito by vm.incognito.collectAsStateWithLifecycle()
    val editMode by vm.editMode.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<HistoryScreenDialog?>(null) }

    val listState = rememberLazyGridState()
    val canEdit = !incognito && items.isNotEmpty()

    fun exitEdit() = vm.exitEdit()

    BackHandler(enabled = editMode) { exitEdit() }

    LaunchedEffect(items) {
        vm.pruneSelection(items.map { HistoryViewModel.key(it) }.toSet())
    }

    LaunchedEffect(Unit) {
        AppBootstrap.state.collect { boot ->
            if (boot is AppBootstrap.Boot.Ready) vm.resolveSourceNames()
        }
    }

    AppTopBarScaffold(
        topBarStartInset = navStart,
        titleContent = {
            Text(
                text = stringResource(R.string.tab_history),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        actions = {
            AnimatedContent(
                targetState = editMode && canEdit,
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                        scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                        .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                },
                label = "historyTopAction",
            ) { managing ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (managing) {
                        ManageActionIcon(
                            iconRes = R.drawable.ic_check,
                            contentDescription = stringResource(R.string.common_done),
                            onClick = { exitEdit() },
                        )
                        ManageActionIcon(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = stringResource(R.string.common_delete_selected),
                            enabled = selected.isNotEmpty(),
                            onClick = { dialog = HistoryScreenDialog.DeleteSelected },
                        )
                    } else {
                        if (canEdit) {
                            ManageActionIcon(
                                iconRes = R.drawable.ic_edit,
                                contentDescription = stringResource(R.string.common_edit),
                                onClick = { vm.enterEdit() },
                            )
                        }
                        ManageActionIcon(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = stringResource(R.string.history_clear),
                            onClick = { dialog = HistoryScreenDialog.ClearHistory },
                        )
                    }
                }
            }
        },
    ) { topPad, _ ->
        Column(modifier = Modifier.fillMaxSize().padding(top = topPad)) {
            ProvideTmdbPosterLookup(vm) {
                HistoryTab(
                    vm = vm,
                    listState = listState,
                    editMode = editMode,
                    selected = selected,
                    onToggleSelected = { vm.toggleSelected(it) },
                    onRequestDelete = { dialog = HistoryScreenDialog.DeleteItem(it) },
                    navStart = navStart,
                    navBottom = navBottom,
                )
            }
        }
    }

    when (val current = dialog) {
        null -> Unit

        HistoryScreenDialog.ClearHistory -> ConfirmDeleteDialog(
            title = stringResource(R.string.history_clear),
            text = stringResource(R.string.history_clear_message),
            onConfirm = { vm.deleteAll() },
            onDismiss = { dialog = null },
        )

        HistoryScreenDialog.DeleteSelected -> ConfirmDeleteDialog(
            title = stringResource(R.string.common_delete_selected),
            text = stringResource(R.string.history_delete_selected_message, selected.size),
            onConfirm = {
                val targets = items.filter { HistoryViewModel.key(it) in selected }
                vm.deleteSelected(targets)
                exitEdit()
            },
            onDismiss = { dialog = null },
        )

        is HistoryScreenDialog.DeleteItem -> ConfirmDeleteDialog(
            title = stringResource(R.string.history_delete_title),
            text = stringResource(
                R.string.history_delete_message,
                current.item.name ?: stringResource(R.string.common_unnamed),
            ),
            onConfirm = { vm.deleteSelected(listOf(current.item)) },
            onDismiss = { dialog = null },
        )
    }
}
