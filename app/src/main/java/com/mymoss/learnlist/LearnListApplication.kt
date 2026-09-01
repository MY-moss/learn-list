package com.mymoss.learnlist

import android.app.Application
import androidx.room.Room
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.local.LearnListDatabase

class LearnListApplication : Application() {
    val database: LearnListDatabase by lazy {
            Room.databaseBuilder(this, LearnListDatabase::class.java, "learn_list.db")
            .addMigrations(LearnListDatabase.MIGRATION_1_2)
            .addMigrations(LearnListDatabase.MIGRATION_2_3)
            .addMigrations(LearnListDatabase.MIGRATION_3_4)
            .addMigrations(LearnListDatabase.MIGRATION_4_5)
            .addMigrations(LearnListDatabase.MIGRATION_5_6)
            .addMigrations(LearnListDatabase.MIGRATION_6_7)
            .build()
    }

    val repository: LearnListRepository by lazy { LearnListRepository(database) }
}
