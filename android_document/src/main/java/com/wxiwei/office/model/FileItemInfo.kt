package com.wxiwei.office.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class FileItemInfo : Serializable {

    @SerializedName("name")
    var name = ""

    @SerializedName("length")
    var length: Long = 0

    @SerializedName("path")
    var path = ""

    @SerializedName("isFavourite")
    var isFavourite = false

    @SerializedName("gotoPage")
    var gotoPage = 0

    @SerializedName("createdTime")
    var createdTime: Long = 0

    @SerializedName("accessedTime")
    var accessedTime: Long = 0

    @SerializedName("itemType")
    var itemType = Constants.ITEM_TYPE_FILE

    fun updateFromCache(itemCache: FileItemInfo) {
        accessedTime = itemCache.accessedTime
        isFavourite = itemCache.isFavourite
        gotoPage = itemCache.gotoPage
    }

}
