package com.tapman104.mpvplayer.feature.playlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ── Data types ────────────────────────────────────────────────────────────────

data class PlaylistItem(
    val uri: String,
    val title: String
)

enum class RepeatMode { None, One, All }

// ── PlaylistManager ───────────────────────────────────────────────────────────

/**
 * Pure Kotlin playlist with shuffle and repeat support.  No Android dependencies.
 *
 * Shuffle is implemented by keeping a parallel [shuffledOrder] index list that is
 * rebuilt whenever the item list changes or shuffle is toggled.  [currentIndex]
 * always refers to a position in [shuffledOrder] when shuffled, or directly in
 * [items] when not shuffled.
 */
class PlaylistManager {

    private val _items = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val items: StateFlow<List<PlaylistItem>> = _items.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.None)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    /** Parallel order list used when shuffle is active. */
    private var shuffledOrder: MutableList<Int> = mutableListOf()

    // ── Mutation ──────────────────────────────────────────────────────────────

    fun add(item: PlaylistItem) {
        val newList = _items.value + item
        val newShuffledOrder = buildShuffledOrder(newList.size, shuffledOrder)
        shuffledOrder = newShuffledOrder.toMutableList()
        _items.value = newList
        // If this is the first item, point at it.
        if (_currentIndex.value == -1) _currentIndex.value = 0
    }

    fun remove(index: Int) {
        val list = _items.value.toMutableList()
        if (index < 0 || index >= list.size) return
        list.removeAt(index)
        _items.value = list

        // Rebuild shuffled order for the new size.
        shuffledOrder = buildShuffledOrder(list.size, shuffledOrder.filter { it != index }
            .map { if (it > index) it - 1 else it }).toMutableList()

        // Adjust current index.
        val cur = _currentIndex.value
        _currentIndex.value = when {
            list.isEmpty()  -> -1
            index < cur     -> cur - 1
            index == cur    -> if (cur >= list.size) list.size - 1 else cur
            else            -> cur
        }
    }

    fun clear() {
        _items.value = emptyList()
        _currentIndex.value = -1
        shuffledOrder = mutableListOf()
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    fun next(): PlaylistItem? {
        val list = _items.value
        if (list.isEmpty()) return null

        if (_repeatMode.value == RepeatMode.One) {
            return currentItem()
        }

        val nextIdx = _currentIndex.value + 1
        return when {
            nextIdx < list.size -> {
                _currentIndex.value = nextIdx
                itemAtLogicalIndex(nextIdx)
            }
            _repeatMode.value == RepeatMode.All -> {
                _currentIndex.value = 0
                itemAtLogicalIndex(0)
            }
            else -> null
        }
    }

    fun previous(): PlaylistItem? {
        val list = _items.value
        if (list.isEmpty()) return null

        val prevIdx = (_currentIndex.value - 1).coerceAtLeast(0)
        _currentIndex.value = prevIdx
        return itemAtLogicalIndex(prevIdx)
    }

    fun jumpTo(index: Int): PlaylistItem? {
        val list = _items.value
        if (index < 0 || index >= list.size) return null
        _currentIndex.value = index
        return itemAtLogicalIndex(index)
    }

    // ── Shuffle ───────────────────────────────────────────────────────────────

    fun toggleShuffle() {
        val shuffled = !_isShuffled.value
        _isShuffled.value = shuffled
        if (shuffled) {
            shuffledOrder = buildShuffledOrder(_items.value.size, emptyList()).toMutableList()
            // Keep the current item first in the new shuffled order.
            val cur = _currentIndex.value
            if (cur >= 0 && shuffledOrder.isNotEmpty()) {
                val pos = shuffledOrder.indexOf(cur)
                if (pos > 0) {
                    shuffledOrder.removeAt(pos)
                    shuffledOrder.add(0, cur)
                }
            }
            _currentIndex.value = 0
        }
        // When turning shuffle off just leave currentIndex as-is (pointing to logical position).
    }

    // ── Repeat ────────────────────────────────────────────────────────────────

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.None -> RepeatMode.All
            RepeatMode.All  -> RepeatMode.One
            RepeatMode.One  -> RepeatMode.None
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun currentItem(): PlaylistItem? {
        val idx = _currentIndex.value
        if (idx < 0) return null
        return itemAtLogicalIndex(idx)
    }

    private fun itemAtLogicalIndex(logicalIndex: Int): PlaylistItem? {
        val list = _items.value
        if (list.isEmpty()) return null
        val realIndex = if (_isShuffled.value && shuffledOrder.size == list.size) {
            shuffledOrder.getOrNull(logicalIndex) ?: logicalIndex
        } else {
            logicalIndex
        }
        return list.getOrNull(realIndex)
    }

    /** Builds a shuffled permutation of [0, size), reusing existing indices where possible. */
    private fun buildShuffledOrder(size: Int, existing: List<Int>): List<Int> {
        if (size == 0) return emptyList()
        val all = (0 until size).toMutableList()
        // Remove already-placed indices.
        val remaining = all.filter { it !in existing }.toMutableList()
        remaining.shuffle()
        return existing.filter { it < size } + remaining
    }
}
