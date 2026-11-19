@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.yuanbox.yuanbox.ui.page

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yuanbox.yuanbox.R
import com.yuanbox.yuanbox.bean.Movie
import com.yuanbox.yuanbox.bean.MovieSort
import com.yuanbox.yuanbox.ui.WindowSize
import com.yuanbox.yuanbox.ui.components.AppTopBarScaffold
import com.yuanbox.yuanbox.ui.components.LoadState
import com.yuanbox.yuanbox.ui.components.LoadStateBox
import com.yuanbox.yuanbox.ui.components.ProvideTmdbPosterLookup
import com.yuanbox.yuanbox.ui.components.VodCard
import com.yuanbox.yuanbox.ui.components.VodCardStyle
import com.yuanbox.yuanbox.ui.components.animateItemEntrance
import com.yuanbox.yuanbox.ui.components.rememberGridEntranceMap

/**
 * 短剧模式界面：识别当前视频源中的短剧分类，以竖版卡片入口展示。
 * 首页向右滑动进入本界面。
 */
@Composable
fun ShortDramaScreen(
    vm: HomeViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val navStart = contentPadding.calculateStartPadding(LocalLayoutDirection.current)
    val navBottom = contentPadding.calculateBottomPadding()

    val sorts by vm.sorts.collectAsStateWithLifecycle()
    val partitions by vm.partitions.collectAsStateWithLifecycle()

    val shortDramaSorts = remember(sorts) { ShortDramaDetector.filterShortDramaSorts(sorts) }
    var activeSortId by remember(shortDramaSorts) {
        mutableStateOf(shortDramaSorts.firstOrNull()?.id)
    }

    AppTopBarScaffold(
        topBarStartInset = navStart,
        titleContent = {
            Text(
                text = stringResource(R.string.short_drama_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        actions = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) { topPad, _ ->
        Column(modifier = Modifier.fillMaxSize().padding(top = topPad)) {
            if (shortDramaSorts.isEmpty()) {
                LoadStateBox(
                    state = LoadState.Empty,
                    emptyText = stringResource(R.string.short_drama_empty),
                    errorText = "",
                    retryText = "",
                    emptyIconRes = R.drawable.ic_empty_record,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ShortDramaSortTabs(
                    sorts = shortDramaSorts,
                    selectedId = activeSortId,
                    onSelect = { id ->
                        activeSortId = id
                        vm.ensureLoaded(id)
                    },
                    navStart = navStart,
                )
                Crossfade(
                    targetState = activeSortId,
                    animationSpec = tween(220),
                    modifier = Modifier.fillMaxSize(),
                    label = "shortDramaTab",
                ) { sortId ->
                    val partition = partitions.firstOrNull { it.sort.id == sortId }
                    ProvideTmdbPosterLookup(vm) {
                        ShortDramaGrid(
                            vm = vm,
                            partition = partition,
                            navStart = navStart,
                            navBottom = navBottom,
                            onCardClick = { video -> handleCardClick(vm, video, context) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortDramaSortTabs(
    sorts: List<MovieSort.SortData>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    navStart: Dp,
) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp + navStart, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = sorts.size, key = { idx -> sorts[idx].id.orEmpty() }) { index ->
            val sort = sorts[index]
            val selected = sort.id == selectedId
            Text(
                text = sort.name ?: "",
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceBright
                    )
                    .clickable { onSelect(sort.id.orEmpty()) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ShortDramaGrid(
    vm: HomeViewModel,
    partition: HomeViewModel.Partition?,
    navStart: Dp,
    navBottom: Dp,
    onCardClick: (Movie.Video) -> Unit,
) {
    val videos = partition?.videos.orEmpty()
    val state = partition?.state

    when {
        state == HomeViewModel.PartitionState.Idle || state == HomeViewModel.PartitionState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ContainedLoadingIndicator(Modifier.size(64.dp))
        }

        state == HomeViewModel.PartitionState.Error -> LoadStateBox(
            state = LoadState.Error(),
            emptyText = "",
            errorText = stringResource(R.string.common_load_failed_network),
            retryText = stringResource(R.string.common_retry),
            onRetry = { partition?.let { vm.retryPartition(it) } },
            modifier = Modifier.fillMaxSize(),
        )

        state == HomeViewModel.PartitionState.Empty -> LoadStateBox(
            state = LoadState.Empty,
            emptyText = stringResource(R.string.common_empty_content),
            errorText = "",
            retryText = "",
            emptyIconRes = R.drawable.ic_empty_record,
            modifier = Modifier.fillMaxSize(),
        )

        else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val columns = WindowSize.gridColumns(
                availableWidthDp = (maxWidth - 32.dp - navStart).value.toInt(),
                minColumns = 3,
            )
            val gridState = remember(partition?.sort?.id) { LazyGridState() }
            val entranceMap = rememberGridEntranceMap(gridState)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp + navStart,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 88.dp + navBottom,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(
                    videos,
                    key = { index, video -> "${index}_${video.id}_${video.name}" },
                ) { index, video ->
                    val entered = entranceMap[index] ?: false
                    val staggerDelay = (index / columns) * 32
                    VodCard(
                        video = video,
                        onClick = { onCardClick(video) },
                        onLongClick = {},
                        modifier = Modifier.animateItemEntrance(
                            entered = entered,
                            delayMillis = staggerDelay,
                        ),
                        style = VodCardStyle.Stacked,
                        showCircularLoading = true,
                    )
                }
                item(key = "more_short_drama", span = { GridItemSpan(maxLineSpan) }) {
                    LaunchedEffect(videos.size) {
                        if (partition != null && partition.hasMore) vm.loadMorePartition(partition)
                    }
                    if (partition?.hasMore == true) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.common_loading_more),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleCardClick(vm: HomeViewModel, video: Movie.Video, context: android.content.Context) {
    context.dispatchVodCardClick(video, onAction = { vm.handleAction(it) })
}
