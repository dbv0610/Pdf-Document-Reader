package com.alf06.document.reader.ui.home.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.utils.AppUtils
import com.wxiwei.office.editor.DocumentCreator
import com.wxiwei.office.editor.EditResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CreateDocumentViewModel(application: Application) : AndroidViewModel(application) {
    sealed interface State {
        data object Idle : State
        data object Creating : State
        data class Created(val document: RecentDocument) : State
        data class Failed(val format: DocumentCreator.Format, val name: String, val exists: Boolean) : State
    }

    private val mutableState = MutableStateFlow<State>(State.Idle)
    val state = mutableState.asStateFlow()

    fun create(format: DocumentCreator.Format, name: String) {
        if (mutableState.value != State.Idle) return
        mutableState.value = State.Creating
        viewModelScope.launch {
            val context = getApplication<Application>()
            mutableState.value = try {
                withContext(Dispatchers.IO) {
                    val filename = if (name.endsWith(".${format.extension}", true)) name else "$name.${format.extension}"
                    val file = File(AppUtils.ensureDocumentDirectory(), filename)
                    when (val result = DocumentCreator.create(context, format, file)) {
                        is EditResult.Ok -> State.Created(AppUtils.registerCreatedDocument(context, file))
                        is EditResult.Error -> {
                            Log.e("CreateDocument", result.message, result.cause)
                            State.Failed(format, name, file.exists())
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CreateDocument", "Creation failed", e)
                State.Failed(format, name, false)
            }
        }
    }

    fun consumeResult() {
        if (mutableState.value != State.Creating) mutableState.value = State.Idle
    }
}
