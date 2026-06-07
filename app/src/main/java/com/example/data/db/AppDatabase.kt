package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        BloodRequest::class,
        Donor::class,
        MemberApplication::class,
        DonationRecord::class,
        ChatMessage::class,
        Notice::class,
        Event::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bloodRequestDao(): BloodRequestDao
    abstract fun donorDao(): DonorDao
    abstract fun memberApplicationDao(): MemberApplicationDao
    abstract fun donationRecordDao(): DonationRecordDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun noticeDao(): NoticeDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jbf_blood_foundation_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
