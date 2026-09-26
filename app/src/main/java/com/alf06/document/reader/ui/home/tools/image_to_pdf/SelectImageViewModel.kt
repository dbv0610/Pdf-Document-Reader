package com.alf06.document.reader.ui.home.tools.image_to_pdf

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.alf06.document.reader.model.GalleryImage
import com.alf06.document.reader.model.ImageFolder
import com.reader.pdfviewer.tools.PdfTools
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SelectImageViewModel(context: Context, private val pdfTools: PdfTools) : ViewModel() {
    private companion object {
        const val TAG = "SelectImageViewModel"
        const val MAX_IMAGE_SIDE = 2000
    }

    private val appContext = context.applicationContext

    private val allImages = MutableStateFlow<List<GalleryImage>>(emptyList())

    private val _currentFolderId = MutableStateFlow<String?>(null)

    private val _selected = MutableStateFlow<List<GalleryImage>>(emptyList())
    val selected: StateFlow<List<GalleryImage>> = _selected.asStateFlow()

    private val _convertState = MutableStateFlow<ConvertState>(ConvertState.Idle)
    val convertState: StateFlow<ConvertState> = _convertState.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    val folders: StateFlow<List<ImageFolder>> = allImages
        .map { images -> buildFolders(images) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val currentFolder: StateFlow<ImageFolder?> =
        combine(folders, _currentFolderId) { folders, id -> folders.firstOrNull { it.id == id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val images: StateFlow<List<GalleryImage>> =
        combine(allImages, _currentFolderId) { images, id ->
            if (id == null) images else images.filter { it.bucketId == id }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val images = withContext(Dispatchers.IO) { queryImages() }
            allImages.value = images

            val ids = images.mapTo(HashSet()) { it.id }
            _selected.update { selected -> selected.filter { it.id in ids } }
            if (images.none { it.bucketId == _currentFolderId.value }) _currentFolderId.value = null
            _isLoaded.value = true
        }
    }

    fun selectFolder(folder: ImageFolder) {
        _currentFolderId.value = folder.id
    }

    fun toggle(image: GalleryImage) {
        _selected.update { selected ->
            if (selected.any { it.id == image.id }) selected.filterNot { it.id == image.id }
            else selected + image
        }
    }

    fun remove(image: GalleryImage) {
        _selected.update { selected -> selected.filterNot { it.id == image.id } }
    }

    fun toggleSelectAll() {
        val visible = images.value
        if (visible.isEmpty()) return
        _selected.update { selected ->
            val selectedIds = selected.mapTo(HashSet()) { it.id }
            if (visible.all { it.id in selectedIds }) {
                val visibleIds = visible.mapTo(HashSet()) { it.id }
                selected.filterNot { it.id in visibleIds }
            } else {
                selected + visible.filterNot { it.id in selectedIds }
            }
        }
    }

    fun reorder(images: List<GalleryImage>) {
        _selected.value = images
    }

    fun convert(output: File) {
        val images = _selected.value
        if (images.isEmpty() || _convertState.value == ConvertState.Running) return
        _convertState.value = ConvertState.Running
        viewModelScope.launch {
            val result = runCatching { writePdf(images, output) }
            if (result.exceptionOrNull() is CancellationException) return@launch
            result.onFailure { Log.e(TAG, "Image to PDF failed", it) }
            if (result.isSuccess) {
                MediaScannerConnection.scanFile(appContext, arrayOf(output.path), arrayOf("application/pdf"), null)
            }
            _convertState.value =
                if (result.isSuccess) ConvertState.Done(output.path) else ConvertState.Failed
        }
    }

    fun consumeConvertResult() {
        _convertState.value = ConvertState.Idle
    }

    private suspend fun writePdf(images: List<GalleryImage>, output: File) =
        pdfTools.createFromImages(output) {
            images.forEach { image ->
                currentCoroutineContext().ensureActive()

                val target = Glide.with(appContext).asBitmap().load(image.uri)
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .submit(MAX_IMAGE_SIDE, MAX_IMAGE_SIDE)
                try {
                    addImage(target.get())
                } finally {
                    Glide.with(appContext).clear(target)
                }
            }
        }

    private fun buildFolders(images: List<GalleryImage>): List<ImageFolder> {
        val all = ImageFolder(id = null, name = "", cover = images.firstOrNull()?.uri, count = images.size)
        val buckets = images.groupBy { it.bucketId }.map { (id, list) ->
            ImageFolder(id = id, name = list.first().bucketName, cover = list.first().uri, count = list.size)
        }
        return listOf(all) + buckets.sortedBy { it.name.lowercase() }
    }

    private fun queryImages(): List<GalleryImage> {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        )
        val result = mutableListOf<GalleryImage>()
        runCatching {
            appContext.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Images.Media.SIZE} > 0",
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameCol =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    result += GalleryImage(
                        id = id,
                        uri = ContentUris.withAppendedId(collection, id),
                        bucketId = cursor.getString(bucketIdCol).orEmpty(),
                        bucketName = cursor.getString(bucketNameCol).orEmpty(),
                    )
                }
            }
        }
        return result
    }
}

sealed interface ConvertState {
    data object Idle : ConvertState
    data object Running : ConvertState
    data class Done(val path: String) : ConvertState
    data object Failed : ConvertState
}
