package com.example.masterand.di

import androidx.room.Room
import com.example.masterand.database.PlayerDatabase
import com.example.masterand.dao.PlayerDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Provide AppDatabase instance
    single {
        Room.databaseBuilder(
            androidContext(),
            PlayerDatabase::class.java,
            "app_database"
        ).build()
    }

    // Provide PlayerDao instance
    single {
        get<PlayerDatabase>().playerDao()
    }
}