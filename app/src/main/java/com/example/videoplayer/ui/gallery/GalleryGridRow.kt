package com.example.videoplayer.ui.gallery

import com.example.videoplayer.data.GalleryMedia
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

fun buildGalleryGridRows(items: List<GalleryMedia>): List<GalleryGridRow> {
    if (items.isEmpty()) return emptyList()
    val grouped = items.groupBy { formatDateHeader(it.dateAddedSec) }
        .toList()
        .sortedByDescending { (_, group) -> group.maxOf { it.dateAddedSec } }
    return buildList {
        grouped.forEach { (header, groupItems) ->
            add(GalleryGridRow.Header(header))
            groupItems.forEach { add(GalleryGridRow.Cell(it)) }
        }
    }
}
