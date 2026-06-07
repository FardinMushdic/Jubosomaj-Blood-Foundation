package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodRequestDao {
    @Query("SELECT * FROM blood_requests ORDER BY id DESC")
    fun getAllRequests(): Flow<List<BloodRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BloodRequest): Long

    @Update
    suspend fun updateRequest(request: BloodRequest)

    @Delete
    suspend fun deleteRequest(request: BloodRequest)
}

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors ORDER BY id DESC")
    fun getAllDonors(): Flow<List<Donor>>

    @Query("SELECT * FROM donors WHERE bloodGroup = :group AND district LIKE :district AND upazila LIKE :upazila ORDER BY id DESC")
    fun searchDonors(group: String, district: String, upazila: String): List<Donor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: Donor)

    @Update
    suspend fun updateDonor(donor: Donor)

    @Delete
    suspend fun deleteDonor(donor: Donor)
}

@Dao
interface MemberApplicationDao {
    @Query("SELECT * FROM member_applications ORDER BY id DESC")
    fun getAllApplications(): Flow<List<MemberApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: MemberApplication): Long

    @Update
    suspend fun updateApplication(application: MemberApplication)

    @Delete
    suspend fun deleteApplication(application: MemberApplication)
}

@Dao
interface DonationRecordDao {
    @Query("SELECT * FROM donation_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<DonationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DonationRecord)

    @Update
    suspend fun updateRecord(record: DonationRecord)

    @Delete
    suspend fun deleteRecord(record: DonationRecord)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE channelType = :channel ORDER BY timestamp ASC")
    fun getMessagesByChannel(channel: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE channelType = :channel")
    suspend fun clearChannel(channel: String)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY id DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotices(notices: List<Notice>)

    @Query("DELETE FROM notices WHERE isFromWebsite = 1")
    suspend fun clearWebsiteNotices()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<Event>)

    @Query("DELETE FROM events WHERE isFromWebsite = 1")
    suspend fun clearWebsiteEvents()

    @Update
    suspend fun updateEvent(event: Event)
}
