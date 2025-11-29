package com.example.modernnotepad.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import android.util.Log
import com.example.modernnotepad.dao.NoteDao
import com.example.modernnotepad.model.Note

@Database(
    entities = [Note::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteDatabase::class.java,
                        "note_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e("NoteDatabase", "Database creation failed: ${e.message}")
                    throw e
                }
            }
        }
    }
}