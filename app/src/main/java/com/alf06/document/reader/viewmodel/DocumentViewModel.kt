package com.alf06.document.reader.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alf06.document.reader.database.FavoriteDao
import com.alf06.document.reader.di.DocumentRepository
import com.alf06.document.reader.di.DocumentRepositoryImpl
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.FavoriteDocument
import com.alf06.document.reader.model.FavoriteUi
import com.alf06.document.reader.model.FileTypeFilter
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.model.RecentUi
import com.alf06.document.reader.model.SortByData
import com.alf06.document.reader.model.SortDateType
import com.alf06.document.reader.model.SortOrder
import com.alf06.document.reader.model.SortSizeType
import com.alf06.document.reader.model.StorageTab
import com.alf06.document.reader.utils.formatDateByMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DocumentViewModel(
    private val repo: DocumentRepository,
    private val favoriteDao: FavoriteDao
) : ViewModel() {

    val isScanning: StateFlow<Boolean> = repo.documents
        .map { it is DataResponse.DataLoading || it is DataResponse.DataIdle }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val permissionDenied: StateFlow<Boolean> = repo.documents
        .map { it is DataResponse.DataError && it.message == DocumentRepositoryImpl.PERMISSION_DENIED }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val allDocuments: Flow<List<RecentDocument>> =
        repo.documents.mapNotNull { if (it is DataResponse.DataSuccess) it.data else null }

    private val favoriteIds: Flow<Set<Long>> = favoriteDao.favoriteIds().map { it.toHashSet() }

    val sortByData: StateFlow<SortByData>
        field = MutableStateFlow(SortByData.None)

    val sortOrder: StateFlow<SortOrder>
        field = MutableStateFlow(SortOrder.Decrease)

    val sortByDateFlow = MutableStateFlow(SortDateType.NoSelect)
    val sortBySizeFlow = MutableStateFlow(SortSizeType.NoSelect)
    val filterTypesFlow = MutableStateFlow(
        listOf(
            DocumentType.Doc,
            DocumentType.Ppt,
            DocumentType.Pdf,
            DocumentType.Excel,
            DocumentType.Txt,
            DocumentType.Image
        )
    )

    private val _searchKey = MutableStateFlow("")
    val searchKey: StateFlow<String> get() = _searchKey
    private val _searchType = MutableStateFlow<DocumentType?>(null)

    val fileTypeFilter: StateFlow<FileTypeFilter>
        field = MutableStateFlow<FileTypeFilter>(FileTypeFilter.All)

    val storageTab: StateFlow<StorageTab>
        field = MutableStateFlow(StorageTab.Recent)

    val recentDocument: StateFlow<List<RecentUi>> =
        combine(repo.recents, favoriteIds) { recents, ids ->
            recents.map { RecentUi(it, it.mediaId in ids) }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteDocument: StateFlow<List<FavoriteUi>> = favoriteDao.getAll()
        .map { documents -> documents.map { FavoriteUi(it, true) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val storageDocuments: StateFlow<List<RecentUi>> =
        combine(storageTab, recentDocument, favoriteDocument) { tab, recents, favorites ->
            when (tab) {
                StorageTab.Recent -> recents
                StorageTab.Favorites -> favorites.map { RecentUi(it.document.toRecent(), isFavorite = true) }
            }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val listRecentSearch: StateFlow<List<RecentUi>> = combine(
        recentDocument,
        _searchKey,
        sortByDateFlow,
        sortBySizeFlow,
        filterTypesFlow
    ) { docs, key, byDate, bySize, types ->
        docs.applyQuery(types, key, byDate, bySize) { it.document }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val listFavoriteSearch: StateFlow<List<FavoriteUi>> = combine(
        favoriteDocument,
        _searchKey,
        sortByDateFlow,
        sortBySizeFlow,
        filterTypesFlow
    ) { docs, key, byDate, bySize, types ->
        docs.applyQuery(types, key, byDate, bySize) { it.document.toRecent() }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val listDocumentSearch: StateFlow<List<RecentUi>> = combine(
        allDocuments,
        favoriteIds,
        _searchKey,
        _searchType,
        combine(sortByData, sortOrder, ::Pair)
    ) { docs, favIds, rawKey, type, (sortType, order) ->
        val key = rawKey.trim().lowercase()
        val filtered = docs
            .asSequence()
            .filter { type == null || it.type == type }
            .filter { key.isEmpty() || it.matches(key) }
            .map { RecentUi(it, it.mediaId in favIds) }
            .toList()

        val sorted = when (sortType) {
            SortByData.None -> return@combine filtered
            SortByData.SortByName -> filtered.sortedBy { File(it.document.path).name.lowercase() }
            SortByData.SortBySize -> filtered.sortedBy { it.document.size }
            SortByData.SortByDate -> filtered.sortedBy { it.document.lastModified }
        }
        if (order == SortOrder.Decrease) sorted.reversed() else sorted
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadDocuments(context: Context) = repo.refresh()

    fun refresh() = repo.refresh()

    fun setStorageTab(tab: StorageTab) {
        storageTab.value = tab
    }

    fun setSortByData(data: SortByData) {
        sortByData.value = data
    }

    fun setSort(data: SortByData, order: SortOrder) {
        sortByData.value = data
        sortOrder.value = order
    }

    fun setFileTypeFilter(filter: FileTypeFilter) {
        fileTypeFilter.value = filter
        _searchType.value = filter.documentType
    }

    fun setSearchCriteria(key: String, type: DocumentType?) {
        _searchKey.value = key
        _searchType.value = type
    }

    fun filterSortFavorite(
        byDate: SortDateType,
        bySize: SortSizeType,
        types: List<DocumentType>
    ) {
        sortByDateFlow.value = byDate
        sortBySizeFlow.value = bySize
        filterTypesFlow.value = types
    }

    fun addToRecent(doc: RecentDocument) {
        viewModelScope.launch { repo.addRecent(doc) }
    }

    fun addToRecent(doc: FavoriteDocument) {
        viewModelScope.launch { favoriteDao.insert(doc) }
    }

    fun toggleFavorite(doc: FavoriteDocument) {
        viewModelScope.launch {
            val exists = favoriteDao.isExistInFavorite(doc.mediaId.toInt()) > 0
            favoriteDao.toggleFavorite(doc, !exists)
        }
    }

    suspend fun isFavorite(doc: RecentDocument): Boolean =
        favoriteDao.isExistInFavorite(doc.mediaId.toInt()) > 0

    fun toggleFavoriteRecent(doc: RecentDocument) {
        viewModelScope.launch {
            val exists = favoriteDao.isExistInFavorite(doc.mediaId.toInt()) > 0
            repo.setFavorite(doc, !exists)
        }
    }

    fun removeFavorite(model: FavoriteDocument) {
        viewModelScope.launch { favoriteDao.deleteById(model.mediaId) }
    }

    fun removeRecent(model: RecentDocument) {
        viewModelScope.launch { repo.removeRecent(model) }
    }

    fun deleteFile(model: RecentDocument, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.delete(model).isSuccess) }
    }

    fun deleteFile(model: FavoriteDocument, onResult: (Boolean) -> Unit = {}) {
        deleteFile(model.toRecent(), onResult)
    }

    fun deleteFiles(models: List<RecentDocument>, onResult: (failedCount: Int) -> Unit = {}) {
        viewModelScope.launch {
            val failed = models.count { repo.delete(it).isFailure }
            onResult(failed)
        }
    }

    fun renameFile(
        model: RecentDocument,
        newBaseName: String,
        onResult: (RecentDocument?) -> Unit = {}
    ): Boolean {
        val name = newBaseName.trim()
        val oldFile = File(model.path)
        val parent = oldFile.parentFile
        if (name.isEmpty() || name.contains('/') || parent == null || !oldFile.exists()) return false

        val ext = oldFile.extension
        val fullName = if (ext.isEmpty() || name.endsWith(".$ext", ignoreCase = true)) name else "$name.$ext"
        if (File(parent, fullName).exists()) return false

        viewModelScope.launch { onResult(repo.rename(model, name).getOrNull()) }
        return true
    }

    fun renameFile(
        model: FavoriteDocument,
        newBaseName: String,
        onResult: (RecentDocument?) -> Unit = {}
    ): Boolean = renameFile(model.toRecent(), newBaseName, onResult)


    private fun FavoriteDocument.toRecent() = RecentDocument(
        mediaId = mediaId,
        path = path,
        lastModified = lastModified,
        size = size,
        type = type
    )

    private fun RecentDocument.matches(key: String): Boolean =
        File(path).name.lowercase().contains(key) ||
                formatDateByMillis(lastModified).lowercase().contains(key)

    private inline fun <T> List<T>.applyQuery(
        types: List<DocumentType>,
        rawKey: String,
        byDate: SortDateType,
        bySize: SortSizeType,
        crossinline doc: (T) -> RecentDocument
    ): List<T> {
        if (types.isEmpty()) return emptyList()
        val key = rawKey.trim().lowercase()
        val filtered = filter { item ->
            val d = doc(item)
            d.type in types && (key.isEmpty() || d.matches(key))
        }
        return when (byDate) {
            SortDateType.NewToOld -> filtered.sortedByDescending { doc(it).lastModified }
            SortDateType.OldToNew -> filtered.sortedBy { doc(it).lastModified }
            SortDateType.NoSelect -> when (bySize) {
                SortSizeType.BigToSmall -> filtered.sortedByDescending { doc(it).size }
                SortSizeType.SmallToBig -> filtered.sortedBy { doc(it).size }
                SortSizeType.NoSelect -> filtered
            }
        }
    }
}
