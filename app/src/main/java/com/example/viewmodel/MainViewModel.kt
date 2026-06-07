package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.network.GeminiService
import com.example.data.network.SyncResult
import com.example.data.network.WebsiteSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val bloodRequestDao = db.bloodRequestDao()
    private val donorDao = db.donorDao()
    private val memberApplicationDao = db.memberApplicationDao()
    private val donationRecordDao = db.donationRecordDao()
    private val chatMessageDao = db.chatMessageDao()
    private val noticeDao = db.noticeDao()
    private val eventDao = db.eventDao()

    // --- Core Database Flows ---
    val requests: StateFlow<List<BloodRequest>> = bloodRequestDao.getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donors: StateFlow<List<Donor>> = donorDao.getAllDonors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val applications: StateFlow<List<MemberApplication>> = memberApplicationDao.getAllApplications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records: StateFlow<List<DonationRecord>> = donationRecordDao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportMessages: StateFlow<List<ChatMessage>> = chatMessageDao.getMessagesByChannel("SUPPORT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val committeeMessages: StateFlow<List<ChatMessage>> = chatMessageDao.getMessagesByChannel("COMMITTEE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<Notice>> = noticeDao.getAllNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<Event>> = eventDao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Mock Authentication & Roles ---
    val userTypes = listOf("Super Admin", "Admin", "Moderator", "Committee Member", "Blood Donor", "Public User")
    val currentUserType = MutableStateFlow("Public User")
    val loggedInUser = MutableStateFlow("মেহরাব হাসান")

    // --- Search Query State ---
    val searchBloodGroup = MutableStateFlow("A+")
    val searchDistrict = MutableStateFlow("")
    val searchUpazila = MutableStateFlow("")
    val searchResults = MutableStateFlow<List<Donor>>(emptyList())

    // --- UI State Variables ---
    val isSyncing = MutableStateFlow(false)
    val syncErrorMessage = MutableStateFlow<String?>(null)
    val alertNotification = MutableStateFlow<String?>(null)

    // --- AI Assistant Prompt Flow ---
    val aiChatHistory = MutableStateFlow<List<AiMessage>>(listOf(
        AiMessage("সিস্টেম", "আসসালামু আলাইকুম! আমি 'যুবসমাজ ব্লাড ফাউন্ডেশন' এআই সহকারী। রক্তদান নির্দেশিকা, রক্তের গ্রুপ বা সংগঠন সম্পর্কে যেকোনো প্রশ্ন করতে পারেন।", System.currentTimeMillis())
    ))
    val aiLoading = MutableStateFlow(false)

    // --- Livestream State ---
    val liveState = MutableStateFlow(
        LiveStreamInfo(
            isActive = false,
            platform = "YouTube Channel",
            viewerCount = 0,
            title = ""
        )
    )

    init {
        // Run initial seed/website synchronization to ensure data exists
        syncWithWebsite()
        seedInitialSampleData()
    }

    // --- Website Synchronization Manager ---
    fun syncWithWebsite() {
        viewModelScope.launch {
            isSyncing.value = true
            syncErrorMessage.value = null
            when (val result = WebsiteSync.fetchWebsiteData()) {
                is SyncResult.Success -> {
                    noticeDao.clearWebsiteNotices()
                    eventDao.clearWebsiteEvents()
                    noticeDao.insertAllNotices(result.notices)
                    eventDao.insertAllEvents(result.events)
                }
                is SyncResult.Failure -> {
                    syncErrorMessage.value = result.error
                }
            }
            isSyncing.value = false
        }
    }

    // --- AI assistant connector (Gemini API) ---
    fun askAiAssistant(question: String) {
        if (question.trim().isEmpty()) return
        val timestamp = System.currentTimeMillis()
        val historyTemp = aiChatHistory.value.toMutableList()
        historyTemp.add(AiMessage("user", question, timestamp))
        aiChatHistory.value = historyTemp
        aiLoading.value = true

        viewModelScope.launch {
            try {
                val reply = GeminiService.getGeminiResponse(question)
                val updatedHistory = aiChatHistory.value.toMutableList()
                updatedHistory.add(AiMessage("ai", reply, System.currentTimeMillis()))
                aiChatHistory.value = updatedHistory
            } catch (e: Exception) {
                val updatedHistory = aiChatHistory.value.toMutableList()
                updatedHistory.add(AiMessage("ai", "দুঃখিত, এআই প্রসেস করার সময় কোনো ত্রুটি হয়েছে। টেকনিক্যাল ত্রুটি: ${e.localizedMessage}", System.currentTimeMillis()))
                aiChatHistory.value = updatedHistory
            } finally {
                aiLoading.value = false
            }
        }
    }

    fun clearAiChat() {
        aiChatHistory.value = listOf(
            AiMessage("সিস্টেম", "আসসালামু আলাইকুম! আমি 'যুবসমাজ ব্লাড ফাউন্ডেশন' এআই সহকারী। রক্তদান নির্দেশিকা, রক্তের গ্রুপ বা সংগঠন সম্পর্কে যেকোনো প্রশ্ন করতে পারেন।", System.currentTimeMillis())
        )
    }

    // --- Blood Requests Operation ---
    fun submitBloodRequest(
        patientName: String,
        bloodGroup: String,
        hospitalName: String,
        address: String,
        phone: String,
        requiredDate: String,
        isEmergency: Boolean,
        extraInfo: String
    ) {
        viewModelScope.launch {
            val req = BloodRequest(
                patientName = patientName,
                bloodGroup = bloodGroup,
                hospitalName = hospitalName,
                address = address,
                phone = phone,
                dateRequired = requiredDate,
                isEmergency = isEmergency,
                additionalInfo = extraInfo,
                status = "Pending"
            )
            bloodRequestDao.insertRequest(req)
            // Instant Push Alert simulated internally
            if (isEmergency) {
                alertNotification.value = "$bloodGroup রক্তদাতার জরুরি প্রয়োজন! স্থান: $hospitalName, $address। অতি দ্রুত যোগাযোগ করুন: $phone"
            } else {
                alertNotification.value = "নতুন রক্তের আবেদন পোস্ট করা হয়েছে! রোগীর নাম: $patientName, রক্তের গ্রুপ: $bloodGroup"
            }
        }
    }

    fun updateRequestStatus(request: BloodRequest, newStatus: String) {
        viewModelScope.launch {
            bloodRequestDao.updateRequest(request.copy(status = newStatus))
        }
    }

    fun deleteRequest(request: BloodRequest) {
        viewModelScope.launch {
            bloodRequestDao.deleteRequest(request)
        }
    }

    // --- Blood Donors Operation ---
    fun registerDonor(
        name: String,
        photoUri: String,
        bloodGroup: String,
        birthDate: String,
        gender: String,
        district: String,
        upazila: String,
        phone: String,
        lastDonationDate: String,
        secureMedicalRecords: String = "কোনো বিশেষ মেডিকেল হিস্ট্রি নেই।"
    ) {
        viewModelScope.launch {
            // Compute eligibility & sample badge
            val isEligible = isDonorEligible(lastDonationDate)
            val badge = if (isEligible) "ভেরিফাইড দাতা" else "অপেক্ষমান দাতা"
            val donor = Donor(
                name = name,
                photoUri = photoUri,
                bloodGroup = bloodGroup,
                birthDate = birthDate,
                gender = gender,
                district = district,
                upazila = upazila,
                phone = phone,
                lastDonationDate = lastDonationDate,
                isVerified = true,
                donationCount = if (lastDonationDate != "Never" && lastDonationDate.isNotEmpty()) 1 else 0,
                eligibilityStatus = if (isEligible) "Eligible" else "Not Eligible",
                achievementBadges = badge,
                secureMedicalRecords = secureMedicalRecords
            )
            donorDao.insertDonor(donor)
            alertNotification.value = "অভিনন্দন দাতা! যুবসমাজ ব্লাড ফাউন্ডেশনে আপনার রক্তদাতা নিবন্ধন সম্পন্ন হয়েছে।"
        }
    }

    fun deleteDonor(donor: Donor) {
        viewModelScope.launch {
            donorDao.deleteDonor(donor)
        }
    }

    private fun isDonorEligible(lastDateStr: String): Boolean {
        if (lastDateStr.isEmpty() || lastDateStr == "Never" || lastDateStr == "কখনো দেননি") return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val lastDate = sdf.parse(lastDateStr) ?: return true
            val diffMs = Date().time - lastDate.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays >= 120
        } catch (e: Exception) {
            true // fallback
        }
    }

    // --- Search System ---
    fun searchDonors(group: String, district: String, upazila: String) = viewModelScope.launch(Dispatchers.IO) {
        val distQuery = if (district.isEmpty()) "%" else "%$district%"
        val upzQuery = if (upazila.isEmpty()) "%" else "%$upazila%"
        val results = donorDao.searchDonors(group, distQuery, upzQuery)
        searchResults.value = results
    }

    // --- Committee Member Applications Operations ---
    fun submitMemberApplication(
        name: String,
        photoUri: String,
        phone: String,
        address: String,
        institution: String,
        profession: String,
        skills: String,
        reason: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD")).format(Date())
            val app = MemberApplication(
                name = name,
                photoUri = photoUri,
                phone = phone,
                address = address,
                institution = institution,
                profession = profession,
                skills = skills,
                reasonToJoin = reason,
                applyDate = dateStr,
                status = "Pending"
            )
            memberApplicationDao.insertApplication(app)
            alertNotification.value = "আপনার সদস্যপদের আবেদন জমা হয়েছে! এডমিন প্যানেলের অনুমোদনের পর তা সক্রিয় হবে।"
        }
    }

    fun updateApplicationStatus(application: MemberApplication, status: String, designation: String = "স্বেচ্ছাসেবক") {
        viewModelScope.launch {
            memberApplicationDao.updateApplication(application.copy(status = status, designation = designation))
            if (status == "Approved") {
                // If approved, verify and insert them into the Donors list automatically
                val donorDetails = Donor(
                    name = application.name,
                    photoUri = application.photoUri,
                    bloodGroup = "B+", // default
                    birthDate = "২০০০-০১-০১",
                    gender = "পুরুষ",
                    district = "ঢাকা",
                    upazila = "মিরপুর",
                    phone = application.phone,
                    lastDonationDate = "Never",
                    isVerified = true,
                    donationCount = 0,
                    achievementBadges = designation,
                    secureMedicalRecords = "সদস্য পদভুক্তি সম্পন্ন হয়েছে।"
                )
                donorDao.insertDonor(donorDetails)
            }
        }
    }

    // --- Blood Donation Records Management ---
    fun uploadDonationRecord(
        donorName: String,
        bloodGroup: String,
        patientName: String,
        patientProblem: String,
        donationNumber: Int,
        hospitalName: String,
        location: String,
        date: String,
        time: String,
        photoUri: String
    ) {
        viewModelScope.launch {
            val uploadDateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(Date())
            val recordUidStr = "JBF-REC-" + UUID.randomUUID().toString().take(8).uppercase()
            val record = DonationRecord(
                donorName = donorName,
                bloodGroup = bloodGroup,
                patientName = patientName,
                patientProblem = patientProblem,
                donationNumber = donationNumber,
                hospitalName = hospitalName,
                location = location,
                date = date,
                time = time,
                photoUri = photoUri,
                uploadedBy = loggedInUser.value,
                uploadDate = uploadDateStr,
                recordUid = recordUidStr,
                status = "Pending"
            )
            donationRecordDao.insertRecord(record)
            alertNotification.value = "সফল রক্তদানের প্রমাণ বা রেকর্ড আপলোড সম্পন্ন হয়েছে! ভেরিফিকেশনের জন্য পাঠানো হল।"
        }
    }

    fun updateDonationRecordStatus(record: DonationRecord, status: String) {
        viewModelScope.launch {
            donationRecordDao.updateRecord(record.copy(status = status))
            if (status == "Verified") {
                // Find matching donors and increment donation count
                val currentDonors = donors.value
                val matched = currentDonors.find { it.name.trim().lowercase() == record.donorName.trim().lowercase() }
                if (matched != null) {
                    val updatedCount = matched.donationCount + 1
                    val newBadges = generateAchievementBadges(updatedCount)
                    donorDao.insertDonor(matched.copy(
                        donationCount = updatedCount,
                        lastDonationDate = record.date,
                        eligibilityStatus = "Not Eligible", // needs to wait 120 days from record date
                        achievementBadges = newBadges
                    ))
                }
            }
        }
    }

    private fun generateAchievementBadges(count: Int): String {
        return when {
            count >= 10 -> "রক্তদাতা মহাপুরুষ, নিঃস্বার্থ প্রাণ, জীবন রক্ষাকারী"
            count >= 5 -> "রক্তদাতা বীর, জীবন রক্ষাকারী"
            count >= 3 -> "লাইফ সেভার"
            count >= 1 -> "ভেরিফাইড দাতা"
            else -> "রক্তদাতা"
        }
    }

    // --- Member Performance Matrix ---
    // Vol_Scores = (Blood Managed * 15) + (Upload Count * 10) + (Event registered * 5)
    fun getMemberPerformanceScore(name: String): Int {
        val managedCount = requests.value.count { it.status == "Completed" && (it.additionalInfo.contains(name) || name == "মেহরাব হাসান") }
        val uploadCount = records.value.count { it.uploadedBy == name && it.status == "Verified" }
        return (managedCount * 15) + (uploadCount * 10) + 15 // base points
    }

    // --- Broadcaster Live Alert and Alerts Management ---
    fun triggerEmergencyAlert(group: String, dist: String, upz: String) {
        viewModelScope.launch {
            alertNotification.value = "🚨 জরুরি এলার্ট সাইরেন: $group গ্রুপের রক্ত দিতে $dist জেলার $upz উপজেলায় দ্রুত পাশে দাঁড়ান! আমাদের স্বেচ্ছাসেবীরা আপনার ডাকের অপেক্ষায়।"
        }
    }

    fun dismissAlert() {
        alertNotification.value = null
    }

    // --- Live Streaming Logic ---
    fun toggleLiveStream(title: String, platform: String) {
        val cur = liveState.value
        if (cur.isActive) {
            liveState.value = LiveStreamInfo(isActive = false, platform = "YouTube Channel", viewerCount = 0, title = "")
        } else {
            val viewerCount = (25..120).random()
            liveState.value = LiveStreamInfo(
                isActive = true,
                platform = platform,
                viewerCount = viewerCount,
                title = title
            )
            alertNotification.value = "🔴 যুবসমাজ ব্লাড ফাউন্ডেশনের লাইভ সম্প্রচার শুরু হয়েছে! বিষয়: '$title' প্লাটফর্ম: $platform"
        }
    }

    // --- Chats Operations ---
    fun sendChatMessage(text: String, channel: String, attachedFile: String = "", fileName: String = "") {
        if (text.trim().isEmpty() && attachedFile.isEmpty()) return
        viewModelScope.launch {
            val msg = ChatMessage(
                senderName = loggedInUser.value,
                senderRole = currentUserType.value,
                messageText = text,
                imageUrl = if (attachedFile.endsWith(".png") || attachedFile.endsWith(".jpg") || attachedFile.endsWith(".jpeg")) attachedFile else "",
                fileUrl = if (attachedFile.isNotEmpty() && !attachedFile.endsWith(".png") && !attachedFile.endsWith(".jpg")) attachedFile else "",
                fileName = fileName,
                channelType = channel,
                timestamp = System.currentTimeMillis()
            )
            chatMessageDao.insertMessage(msg)

            // Simulator Support Bot replies:
            if (channel == "SUPPORT") {
                simulateSupportReply(text)
            }
        }
    }

    private fun simulateSupportReply(userMsg: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val replyText = when {
                userMsg.contains("রক্ত") && userMsg.contains("চাই") -> "অবশ্যই! আমাদের রক্তদানের জরুরি রিকোয়েস্ট তৈরি করতে হোমস্ক্রীন থেকে 'জরুরি রক্তের আবেদন' বাটনে প্রেস করুন। আমাদের এডমিন প্যানেল আপনার পোস্টটি সরাসরি দাতা বন্ধুদের কাছে গ্রুপ এলার্ট দেবে।"
                userMsg.contains("সদস্য") || userMsg.contains("কমিটি") -> "আসসালামু আলাইকুম। আমাদের কমিটিতে যোগ দিতে প্রোফাইল ট্যাব থেকে 'মেম্বারশিপ ফর্ম' সাবমিট করুন। প্রতি মাসে এডমিন মিটিং-এ আবেদনসমূহ বিবেচনা করে যুক্ত করা হয়।"
                userMsg.contains("ঠিকানা") || userMsg.contains("অফিস ->") -> "আমাদের প্রধান অফিস গুগল সাইটস্ যোগাযোগ লিঙ্ক 'sites.google.com/view/teamofjbf' থেকে দেখতে পারবেন। এটি সার্বক্ষণিক অনলাইনে মনিটর করা হচ্ছে।"
                else -> "জাজাকাল্লাহ খাইরান। আপনার জিজ্ঞাসাটি যুবসমাজ ব্লাড ফাউন্ডেশন মডারেটর টিমের প্যানেলে পৌঁছেছে। অতি দ্রুত আমাদের একজন প্রতিনিধি ফেসবুক/মোবাইলে যোগাযোগ করবেন।"
            }
            chatMessageDao.insertMessage(
                ChatMessage(
                    senderName = "জেবিএফ হেল্প ডেস্ক",
                    senderRole = "মডারেটর",
                    messageText = replyText,
                    channelType = "SUPPORT",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Seeding of Initial Sample Data if app is empty ---
    private fun seedInitialSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Seed sample requests if empty
            val requestCount = db.bloodRequestDao().getAllRequests().first().size
            if (requestCount == 0) {
                db.bloodRequestDao().insertRequest(
                    BloodRequest(
                        patientName = "তাহমিনা আক্তার",
                        bloodGroup = "O+",
                        hospitalName = "বঙ্গবন্ধু শেখ মুজিব মেডিকেল বিশ্ববিদ্যালয়",
                        address = "শাহবাগ, ঢাকা",
                        phone = "০১৭১২৩৪৫৬৭৮",
                        dateRequired = "১০ জুন, ২০২৬",
                        isEmergency = true,
                        additionalInfo = "সিজারিয়ান অপারেশনের জন্য জরুরি ২ ব্যাগ ও-পজিটিভ রক্ত প্রয়োজন। সিঙ্ক ডোনাটদের অনুরোধ রইল।",
                        status = "Searching Donor"
                    )
                )
                db.bloodRequestDao().insertRequest(
                    BloodRequest(
                        patientName = "জাহিদুল ইসলাম",
                        bloodGroup = "A-",
                        hospitalName = "ঢাকা মেডিকেল কলেজ হাসপাতাল",
                        address = "বখশিবাজার, ঢাকা",
                        phone = "০১৯৮৭৬৫৪৩২১",
                        dateRequired = "১৫ জুন, ২০২৬",
                        isEmergency = false,
                        additionalInfo = "থ্যালাসেমিয়া রোগীর জন্য রেগুলার ব্যাকআপ ব্লাড ব্যাগ প্রয়োজন। রক্তের গ্রুপ এ-নেগেটিভ।",
                        status = "Pending"
                    )
                )
                db.bloodRequestDao().insertRequest(
                    BloodRequest(
                        patientName = "আরিফুল হক",
                        bloodGroup = "B+",
                        hospitalName = "চট্টগ্রাম মেডিকেল কলেজ হাসপাতাল",
                        address = "চকবাজার, চট্টগ্রাম",
                        phone = "০১৮২৩৪৫৬৭৮৯",
                        dateRequired = "০২ জুন, ২০২৬",
                        isEmergency = true,
                        additionalInfo = "হার্ট সার্জারির জন্য ১ ব্যাগ বি-পজিটিভ রক্ত ব্যবস্থাপনাকারী স্বেচ্ছাসেবককে অনুরোধ করা হয়েছে।",
                        status = "Completed"
                    )
                )
            }

            // Seed sample Donors
            val donorCount = db.donorDao().getAllDonors().first().size
            if (donorCount == 0) {
                db.donorDao().insertDonor(
                    Donor(
                        name = "তানভীর আহমেদ",
                        photoUri = "",
                        bloodGroup = "A+",
                        birthDate = "১৯৯৮-১০-১২",
                        gender = "পুরুষ",
                        district = "ঢাকা",
                        upazila = "মিরপুর",
                        phone = "০১৫৫৫১১২২৩৩",
                        lastDonationDate = "২০২৬-০১-১৫",
                        isVerified = true,
                        donationCount = 6,
                        achievementBadges = "রক্তদাতা বীর, জেবিএফ এলিট",
                        secureMedicalRecords = "শারীরিক সুস্থতা সন্তোষজনক। কোনো এলার্জি নেই।"
                    )
                )
                db.donorDao().insertDonor(
                    Donor(
                        name = "সাবরিনা চৌধুরী",
                        photoUri = "",
                        bloodGroup = "O-",
                        birthDate = "১৯৯৯-০৪-০৪",
                        gender = "নারী",
                        district = "ঢাকা",
                        upazila = "ধানমন্ডি",
                        phone = "০১৬১১২২৩৩৪৪",
                        lastDonationDate = "২০২৫-১২-২০",
                        isVerified = true,
                        donationCount = 4,
                        achievementBadges = "লাইফ সেভার, ইউনিভার্সাল ডোনাার",
                        secureMedicalRecords = "হিমোগ্লোবিন ১১.৭%। সুস্থ।"
                    )
                )
                db.donorDao().insertDonor(
                    Donor(
                        name = "মেহরাব হাসান",
                        photoUri = "",
                        bloodGroup = "B+",
                        birthDate = "১৯৯৫-০৮-২৫",
                        gender = "পুরুষ",
                        district = "ঢাকা",
                        upazila = "মোহাম্মদপুর",
                        phone = "০১৭৬০০০১১২২",
                        lastDonationDate = "২০২৬-০৫-০১",
                        isVerified = true,
                        donationCount = 8,
                        achievementBadges = "সেরা স্বেচ্ছাসেবক, রক্তদাতা বীর",
                        secureMedicalRecords = "রক্তচাপ স্বাভাবিক। কোলেস্টেরল লেভেল একদম পারফেক্ট।"
                    )
                )
            }

            // Seed initial applications
            val appCount = db.memberApplicationDao().getAllApplications().first().size
            if (appCount == 0) {
                db.memberApplicationDao().insertApplication(
                    MemberApplication(
                        name = "রাসেল মাহমুদ",
                        phone = "০১৮১১২২৩৩৪৪",
                        address = "কুমিল্লা",
                        institution = "কুমিল্লা বিশ্ববিদ্যালয়",
                        profession = "ছাত্র",
                        skills = "ডিজিটাল পোস্টার ডিজাইন ও ফেসবুক গ্রুপ মডারেট করা",
                        reasonToJoin = "মানুষের বিপদে রক্তের জন্য হাহাকার দেখে যুবসমাজ ব্লাড ফাউন্ডেশনের মানবিক কাজ অত্যন্ত ভালো লেগেছে। তাই নিজেকে সম্পৃক্ত করতে চাই।",
                        applyDate = "০৬ জুন, ২০২৬",
                        status = "Pending"
                    )
                )
            }

            // Seed initial donation record
            val recCount = db.donationRecordDao().getAllRecords().first().size
            if (recCount == 0) {
                db.donationRecordDao().insertRecord(
                    DonationRecord(
                        donorName = "মেহরাব হাসান",
                        bloodGroup = "B+",
                        patientName = "করিম মিয়া",
                        patientProblem = "দুর্ঘটনাজনিত রক্তক্ষরণ",
                        donationNumber = 8,
                        hospitalName = "কুর্মিটোলা জেনারেল হাসপাতাল",
                        location = "খিলক্ষেত, ঢাকা",
                        date = "২০২৬-০৫-০১",
                        time = "বিকাল ০৬:৩০ মিনিট",
                        uploadedBy = "মেহরাব হাসান",
                        uploadDate = "০১-০৫-২০২৬",
                        recordUid = "JBF-REC-MOCK888",
                        status = "Verified"
                    )
                )
            }
        }
    }
}

// --- Data Structures ---
data class AiMessage(
    val sender: String, // system, user, ai
    val text: String,
    val time: Long
)

data class LiveStreamInfo(
    val isActive: Boolean,
    val platform: String,
    val viewerCount: Int,
    val title: String
)
