package com.alf06.document.reader.di

import androidx.room.Room
import com.alf06.document.reader.database.AppDatabase
import com.alf06.document.reader.utils.PreferenceHelper
import com.alf06.document.reader.utils.SharedPreference
import com.reader.pdfviewer.tools.PdfTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { SharedPreference(get()) }
    single { PreferenceHelper(get()) }
    single {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "documents.db").build()
    }
    single { get<AppDatabase>().documentDao() }
    single { get<AppDatabase>().favoriteDao() }
    single { PdfTools(androidContext()) }
    single(named("appScope")) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single<DocumentRepository> {
        DocumentRepositoryImpl(androidContext(), get(), get(), get(named("appScope")))
    }
}