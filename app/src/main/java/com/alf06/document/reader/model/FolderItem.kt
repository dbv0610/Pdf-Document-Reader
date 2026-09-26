package com.alf06.document.reader.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FolderItem(
    var folderName: String = "All photos",
    var previewPath: String = "",
    var listData: MutableList<RecentDocument> = mutableListOf<RecentDocument>(),
    var itemCount: Int = 0
) : Parcelable
