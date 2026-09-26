package com.example.videoplayer.ui.gallery

import com.example.videoplayer.data.GalleryMedia
import com.example.videoplayer.data.SortOrder
import com.example.videoplayer.util.formatDateHeader

sealed class GalleryGridRow {
    abstract val stableKey: String

    data class Header(val title: String) : GalleryGridRow() {
        override val stableKey: String = "header-$title"
    }

    data class Cell(val media: GalleryMedia) : GalleryGridRow() {
        override val stableKey: String = "cell-${media.id}"
    }
}

fun buildGalleryGridRows(items: List<GalleryMedia>, sortOrder: SortOrder): List<GalleryGridRow> {
    if (items.isEmpty()) return emptyList()

    val useDateSections = sortOrder == SortOrder.DATE_NEWEST || sortOrder == SortOrder.DATE_OLDEST
    if (!useDateSections) {
        return items.map { GalleryGridRow.Cell(it) }
    }

    val grouped = items.groupBy { formatDateHeader(it.dateAddedSec) }
    val sectionOrder = if (sortOrder == SortOrder.DATE_NEWEST) {
        grouped.toList().sortedByDescending { (_, group) -> group.maxOf { it.dateAddedSec } }
    } else {
        grouped.toList().sortedBy { (_, group) -> group.minOf { it.dateAddedSec } }
    }

    return buildList {
        sectionOrder.forEach { (header, groupItems) ->
            add(GalleryGridRow.Header(header))
            groupItems.forEach { add(GalleryGridRow.Cell(it)) }
        }
    }
}
