package com.ikun.ikunbox.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun rememberGridEntranceMap(
    state: LazyGridState,
    key: Any? = Unit,
    leadingItemCount: Int = 0,
): MutableMap<Int, Boolean> {
    val entered = remember(key) { mutableStateMapOf<Int, Boolean>() }
    LaunchedEffect(state, key, leadingItemCount) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.map { it.index - leadingItemCount } }
            .collectLatest { visible ->
                visible.forEach { index ->
                    if (index >= 0 && !entered.containsKey(index)) entered[index] = true
                }
            }
    }
    return entered
}

@Composable
fun rememberListEntranceMap(
    state: LazyListState,
    key: Any? = Unit,
    leadingItemCount: Int = 0,
): MutableMap<Int, Boolean> {
    val entered = remember(key) { mutableStateMapOf<Int, Boolean>() }
    LaunchedEffect(state, key, leadingItemCount) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.map { it.index - leadingItemCount } }
            .collectLatest { visible ->
                visible.forEach { index ->
                    if (index >= 0 && !entered.containsKey(index)) entered[index] = true
                }
            }
    }
    return entered
}
