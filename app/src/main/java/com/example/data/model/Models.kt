package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_requests")
data class BloodRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val bloodGroup: String,
    val hospitalName: String,
    val address: String, // District / Upazila / Details
    val phone: String,
    val dateRequired: String,
    val isEmergency: Boolean,
    val additionalInfo: String,
    val status: String // Pending, Searching Donor, Managed, Completed, Cancelled
) {
    fun getBanglaStatus(): String = when (status) {
        "Pending" -> "অপেক্ষমান"
        "Searching Donor" -> "দাতা খোঁজা হচ্ছে"
        "Managed" -> "রক্তদাতা নিশ্চিত"
        "Completed" -> "সফল রক্তদান"
        "Cancelled" -> "বাতিলকৃত"
        else -> status
    }
}

@Entity(tableName = "donors")
data class Donor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String = "",
    val bloodGroup: String,
    val birthDate: String,
    val gender: String,
    val district: String,
    val upazila: String,
    val phone: String,
    val lastDonationDate: String, // YYYY-MM-DD, or "Never"
    val isVerified: Boolean = false,
    val donationCount: Int = 0,
    val eligibilityStatus: String = "Eligible", // Eligible, Not Eligible
    val achievementBadges: String = "", // comma-separated titles
    val secureMedicalRecords: String = "" // encrypted or secure health notes
)

@Entity(tableName = "member_applications")
data class MemberApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String = "",
    val phone: String,
    val address: String,
    val institution: String,
    val profession: String,
    val skills: String,
    val reasonToJoin: String,
    val status: String = "Pending", // Pending, Approved, Rejected
    val applyDate: String,
    val designation: String = "স্বেচ্ছাসেবক"
)

@Entity(tableName = "donation_records")
data class DonationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val donorName: String,
    val bloodGroup: String,
    val patientName: String,
    val patientProblem: String,
    val donationNumber: Int,
    val hospitalName: String,
    val location: String,
    val date: String,
    val time: String,
    val photoUri: String = "",
    val uploadedBy: String,
    val uploadDate: String,
    val recordUid: String, // Auto-generated UUID style string
    val status: String = "Pending" // Pending, Verified
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val senderRole: String, // Super Admin, Admin, Moderator, Committee Member, Public User
    val messageText: String,
    val imageUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val channelType: String, // "SUPPORT" (Public to Committee) or "COMMITTEE" (Group Chat)
    val timestamp: Long
)

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val publishDate: String,
    val isFromWebsite: Boolean = false,
    val rawUrl: String = ""
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val location: String,
    val date: String,
    val time: String,
    val description: String,
    val imageUrl: String = "",
    val isFromWebsite: Boolean = false,
    val registrationCount: Int = 0,
    val participantsList: String = "" // Comma separated names
)
