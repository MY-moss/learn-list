package com.mymoss.learnlist

import android.app.Application
import androidx.room.Room
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.local.LearnListDatabase

class LearnListApplication : Application() {
    val database: LearnListDatabase by lazy {
        Room.databaseBuilder(this, LearnListDatabase::class.java, "learn_list.db")
            .addMigrations(LearnListDatabase.MIGRATION_1_2)
            .build()
    }

    val repository: LearnListRepository by lazy { LearnListRepository(database) }
}
