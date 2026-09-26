package com.alf06.document.reader.model

import android.net.Uri

data class GalleryImage(
    val id: Long,
    val uri: Uri,
    val bucketId: String,
    val bucketName: String,
)

/** [id] is null for the virtual "All Images" folder. */
data class ImageFolder(
    val id: String?,
    val name: String,
    val cover: Uri?,
    val count: Int,
)
