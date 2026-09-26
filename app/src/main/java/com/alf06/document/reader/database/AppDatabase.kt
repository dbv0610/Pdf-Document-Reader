package com.alf06.document.reader.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alf06.document.reader.model.Converters
import com.alf06.document.reader.model.FavoriteDocument
import com.alf06.document.reader.model.RecentDocument

@Database(
    entities = [RecentDocument::class, FavoriteDocument::class],
    version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): RecentDao
    abstract fun favoriteDao(): FavoriteDao
}