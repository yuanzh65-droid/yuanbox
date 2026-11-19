package com.yuanbox.yuanbox.ui.page

import com.yuanbox.yuanbox.api.AppBootstrap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.data.VodCollect
import com.yuanbox.yuanbox.ui.components.AppTopBarScaffold
import com.yuanbox.yuanbox.ui.components.CapsuleSegmentedButton
import com.yuanbox.yuanbox.ui.components.ProvideTmdbPosterLookup
import com.yuanbox.yuanbox.ui.components.SegmentOption
import com.yuanbox.yuanbox.ui.components.SegmentStyle

private enum class RecordsMode {
    Collect,
    Following,
}

private sealed interface RecordsDialog {
    data object ClearCollect : RecordsDialog
    data object DeleteSelectedCollect : RecordsDialog
    data object DeleteFollowing : RecordsDialog
    data class DeleteCollectItem(val item: VodCollect) : RecordsDialog
}

@Composable
fun RecordsPage(
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val collectVm: CollectViewModel = viewModel()
    val followingVm: FollowingViewModel = viewModel()

    val navStart = contentPadding.calculateStartPadding(LocalLayoutDirection.current)
    val navBottom = contentPadding.calculateBottomPadding()

    val collectItems by collectVm.items.collectAsStateWithLifecycle()
    val followingItems by followingVm.items.collectAsStateWithLifecycle()

    var mode by rememberSaveable { mutableStateOf(RecordsMode.Collect) }
    val collectEdit by collectVm.editMode.collectAsStateWithLifecycle()
    val collectSelected by collectVm.selected.collectAsStateWithLifecycle()
    val followingEdit by followingVm.editMode.collectAsStateWithLifecycle()
    val followingSelected by followingVm.selected.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<RecordsDialog?>(null) }

    val collectListState = rememberLazyGridState()

    val isCollect = mode == RecordsMode.Collect
    val editing = if (isCollect) collectEdit else followingEdit
    val canEdit = if (isCollect) collectItems.isNotEmpty() else followingItems.isNotEmpty()
    val selectedCount = if (isCollect) collectSelected.size else followingSelected.size

    fun exitEdit() {
        collectVm.exitEdit()
        followingVm.exitEdit()
    }

    BackHandler(enabled = editing) { exitEdit() }

    LaunchedEffect(mode) {
        exitEdit()
        dialog = null
    }

    LaunchedEffect(collectItems) {
        collectVm.pruneSelection(collectItems.map { it.id }.toSet())
    }

    LaunchedEffect(followingItems) {
        followingVm.pruneSelection(followingItems.map { it.follow.id }.toSet())
    }

    LaunchedEffect(Unit) {
        AppBootstrap.state.collect { boot ->
            if (boot is AppBootstrap.Boot.Ready) {
                followingVm.refresh()
            }
        }
    }

    AppTopBarScaffold(
        topBarStartInset = navStart,
        titleContent = {
            Text(
                text = stringResource(R.string.tab_records),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        actions = {
            AnimatedContent(
                targetState = editing && canEdit,
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                        scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                        .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                },
                label = "recordsTopAction",
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
                            enabled = selectedCount > 0,
                            onClick = {
                                dialog = if (isCollect) {
                                    RecordsDialog.DeleteSelectedCollect
                                } else {
                                    RecordsDialog.DeleteFollowing
                                }
                            },
                        )
                    } else {
                        if (canEdit) {
                            ManageActionIcon(
                                iconRes = R.drawable.ic_edit,
                                contentDescription = stringResource(R.string.common_edit),
                                onClick = {
                                    if (isCollect) collectVm.enterEdit() else followingVm.enterEdit()
                                },
                            )
                        }
                        ManageActionIcon(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = if (isCollect) {
                                stringResource(R.string.collect_clear)
                            } else {
                                stringResource(R.string.common_delete_selected)
                            },
                            onClick = {
                                dialog = if (isCollect) {
                                    RecordsDialog.ClearCollect
                                } else {
                                    RecordsDialog.DeleteFollowing
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { topPad, _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            CapsuleSegmentedButton(
                options = listOf(
                    SegmentOption(
                        label = stringResource(R.string.common_collect),
                        value = RecordsMode.Collect,
                        iconPainter = painterResource(R.drawable.ic_tab_collect),
                    ),
                    SegmentOption(
                        label = stringResource(R.string.tab_following),
                        value = RecordsMode.Following,
                        iconPainter = painterResource(R.drawable.ic_tab_following),
                    ),
                ),
                selectedValue = mode,
                onOptionSelected = { mode = it },
                style = SegmentStyle.Track,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                pressMorph = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp + navStart, end = 16.dp, top = topPad + 8.dp),
            )
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    val toRight = targetState == RecordsMode.Following
                    (
                        slideInHorizontally(spring(stiffness = Spring.StiffnessMedium)) { full ->
                            if (toRight) full / 4 else -full / 4
                        } + fadeIn(spring(stiffness = Spring.StiffnessMedium))
                        ).togetherWith(
                        slideOutHorizontally(spring(stiffness = Spring.StiffnessMedium)) { full ->
                            if (toRight) -full / 4 else full / 4
                        } + fadeOut(spring(stiffness = Spring.StiffnessMedium))
                    )
                },
                label = "recordsSegment",
            ) { m ->
                when (m) {
                    RecordsMode.Collect -> ProvideTmdbPosterLookup(collectVm) {
                        CollectTab(
                            vm = collectVm,
                            listState = collectListState,
                            editMode = collectEdit,
                            selected = collectSelected,
                            onToggleSelected = { collectVm.toggleSelected(it) },
                            onRequestDelete = { dialog = RecordsDialog.DeleteCollectItem(it) },
                            navStart = navStart,
                            navBottom = navBottom,
                        )
                    }

                    RecordsMode.Following -> FollowingTab(
                        vm = followingVm,
                        navStart = navStart,
                        navBottom = navBottom,
                    )
                }
            }
        }
    }

    when (val current = dialog) {
        null -> Unit

        RecordsDialog.ClearCollect -> ConfirmDeleteDialog(
            title = stringResource(R.string.collect_clear),
            text = stringResource(R.string.collect_clear_message),
            onConfirm = { collectVm.deleteAll() },
            onDismiss = { dialog = null },
        )

        RecordsDialog.DeleteSelectedCollect -> ConfirmDeleteDialog(
            title = stringResource(R.string.common_delete_selected),
            text = stringResource(R.string.collect_delete_selected_message, collectSelected.size),
            onConfirm = {
                val targets = collectItems.filter { it.id in collectSelected }
                collectVm.deleteSelected(targets)
                exitEdit()
            },
            onDismiss = { dialog = null },
        )

        RecordsDialog.DeleteFollowing -> ConfirmDeleteDialog(
            title = stringResource(R.string.common_delete_selected),
            text = stringResource(R.string.following_delete_selected_message, followingSelected.size),
            onConfirm = { followingVm.deleteSelected() },
            onDismiss = { dialog = null },
        )

        is RecordsDialog.DeleteCollectItem -> ConfirmDeleteDialog(
            title = stringResource(R.string.detail_uncollect),
            text = stringResource(
                R.string.collect_uncollect_message,
                current.item.name ?: stringResource(R.string.common_unnamed),
            ),
            onConfirm = { collectVm.deleteSelected(listOf(current.item)) },
            onDismiss = { dialog = null },
        )
    }
}
