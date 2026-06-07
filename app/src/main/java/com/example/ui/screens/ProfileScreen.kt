package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed
import com.example.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val role by viewModel.currentUserType.collectAsState()

    val volunteerScore = viewModel.getMemberPerformanceScore(loggedInUser)

    var showUploadRecordForm by remember { mutableStateOf(false) }

    // Upload records states
    var donorName by remember { mutableStateOf("") }
    var recordBloodGroup by remember { mutableStateOf("B+") }
    var patientName by remember { mutableStateOf("") }
    var patientProblem by remember { mutableStateOf("") }
    var donationNumber by remember { mutableStateOf("1") }
    var hospitalName by remember { mutableStateOf("") }
    var locationDetails by remember { mutableStateOf("") }
    var donationDate by remember { mutableStateOf("২০২৬-০৬-০৭") }
    var donationTime by remember { mutableStateOf("বিকাল ০৫:০০ মিনিট") }

    var uploadSuccess by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val bloodGroupsList = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JbfCharcoal)
                    .statusBarsPadding()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ডিজিটাল আইডি ও পারসোনাল কন্ট্রোল",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 85.dp)
        ) {
            // Role switcher to simulate RBAC testing in dev
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("rbac_card_selector"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, JbfRed.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = "RBAC switcher", tint = JbfRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ডেভলপার রুল সুইচার (রোল-বেসড টেস্ট)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                        }
                        Text(
                            text = "ভিন্ন রোল সিলেক্ট করে অ্যাপের ফিচার ও এডমিন প্রিভিলেজ মক্ করুন:",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid Selection
                        viewModel.userTypes.chunked(3).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                chunk.forEach { type ->
                                    val checked = type == role
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (checked) JbfRed else JbfLightGray)
                                            .clickable {
                                                viewModel.currentUserType.value = type
                                                if (type == "Super Admin" || type == "Admin") {
                                                    viewModel.loggedInUser.value = "মেহরাব হাসান"
                                                } else if (type == "Committee Member") {
                                                    viewModel.loggedInUser.value = "কাজী ফয়সাল"
                                                } else {
                                                    viewModel.loggedInUser.value = "মেহবুবা রহমান"
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(type, color = if (checked) Color.White else JbfCharcoal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. Digital Membership Badge ID Card (Material Style)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("digital_id_card"),
                    colors = CardDefaults.cardColors(containerColor = JbfCharcoal)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card Header Logo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(JbfRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "যুবসমাজ ব্লাড ফাউন্ডেশন",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "অফিসিয়াল আইডি",
                                color = JbfRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar & Details block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar drawing
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, JbfRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile Pic",
                                    tint = JbfCharcoal,
                                    modifier = Modifier.size(45.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Details
                            Column {
                                Text(
                                    text = loggedInUser,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (role == "Super Admin" || role == "Admin") "প্রতিষ্ঠাতা সভাপতি ও ডিরেক্টর" else "স্বেচ্ছাসেবী কন্ট্রিবিউটর",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "সদস্য আইডি: JBF-2026-6284",
                                    color = JbfRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "যোগদান: ০১ জানুয়ারি, ২০২২",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ID Scan QR Code via Canvas drawings representing a scanning area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("রক্ত গ্রুপ: B+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("স্বেচ্ছাসেবী স্কোর: $volunteerScore", color = Color.LightGray, fontSize = 11.sp)
                                Text("ভেরিফাইড ব্যাজ: এলিট", color = JbfRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Dynamic QR code box
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(52.dp)) {
                                    val cellSize = size.width / 4
                                    // Simulated QR code block grids
                                    drawRect(Color.Black, Offset(0f, 0f), Size(cellSize, cellSize))
                                    drawRect(Color.Black, Offset(0f, cellSize * 3), Size(cellSize, cellSize))
                                    drawRect(Color.Black, Offset(cellSize * 3, 0f), Size(cellSize, cellSize))
                                    drawRect(Color.Black, Offset(cellSize, cellSize), Size(cellSize, cellSize))
                                    drawRect(Color.Black, Offset(cellSize * 2, cellSize * 2), Size(cellSize, cellSize))
                                    drawRect(Color.Black, Offset(cellSize * 3, cellSize * 3), Size(cellSize, cellSize))
                                }
                            }
                        }
                    }
                }
            }

            // 2. Activity / Badge Counter Metrics Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreBox(title = "রক্তদান সংখ্যা", value = "৮ বার", modifier = Modifier.weight(1f))
                    ScoreBox(title = "রক্ত ম্যানেজড", value = "৪১ বার", modifier = Modifier.weight(1f))
                    ScoreBox(title = "স্বেচ্ছাসেবী পয়েন্ট", value = "$volunteerScore", modifier = Modifier.weight(1f))
                }
            }

            // 3. Document Donation Record (ফর ভলান্টিয়ারস্)
            item {
                val isCommitteeMember = role == "Super Admin" || role == "Admin" || role == "Moderator" || role == "Committee Member"
                if (isCommitteeMember) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("রক্তদানের প্রমাণপত্র আপলোড করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                                    Text("রক্তদাতাকে উৎসাহিত করতে এবং স্কোর বৃদ্ধি করতে ব্যবহার করুন।", fontSize = 10.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(JbfRed.copy(alpha = 0.1f))
                                        .clickable { showUploadRecordForm = !showUploadRecordForm },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (showUploadRecordForm) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = JbfRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Form items
                            if (showUploadRecordForm) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = donorName,
                                        onValueChange = { donorName = it },
                                        label = { Text("রক্তদাতার নাম (উদা: তানভীর আহমেদ)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_donor_name"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    Text("রক্তের গ্রুপ:", fontSize = 11.sp, color = JbfCharcoal, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        bloodGroupsList.take(4).forEach { g ->
                                            val active = recordBloodGroup == g
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (active) JbfRed else JbfLightGray)
                                                    .clickable { recordBloodGroup = g }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(g, color = if (active) Color.White else JbfCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        bloodGroupsList.drop(4).forEach { g ->
                                            val active = recordBloodGroup == g
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (active) JbfRed else JbfLightGray)
                                                    .clickable { recordBloodGroup = g }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(g, color = if (active) Color.White else JbfCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = patientName,
                                        onValueChange = { patientName = it },
                                        label = { Text("গ্রহীতা রোগীর নাম", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_patient_name"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    OutlinedTextField(
                                        value = patientProblem,
                                        onValueChange = { patientProblem = it },
                                        label = { Text("রোগীর সমস্যা (উদা: থ্যালাসেমিয়া/অপারেশন)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_patient_prob"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    OutlinedTextField(
                                        value = donationNumber,
                                        onValueChange = { donationNumber = it },
                                        label = { Text("রক্তদানের সংখ্যা (ডোনারের ২য় বা ৩য় দান)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_donation_no"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    OutlinedTextField(
                                        value = hospitalName,
                                        onValueChange = { hospitalName = it },
                                        label = { Text("হাসপাতাল বা মেডিকেল বুথ", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_hospital"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    OutlinedTextField(
                                        value = locationDetails,
                                        onValueChange = { locationDetails = it },
                                        label = { Text("স্থান (জেলা/উপজেলা)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_location"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    OutlinedTextField(
                                        value = donationDate,
                                        onValueChange = { donationDate = it },
                                        label = { Text("রক্তদানের তারিখ (YYYY-MM-DD)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_date"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    OutlinedTextField(
                                        value = donationTime,
                                        onValueChange = { donationTime = it },
                                        label = { Text("রক্তদানের সময়", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_time"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                                    )

                                    uploadSuccess?.let {
                                        Text(it, color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    uploadError?.let {
                                        Text(it, color = JbfRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (donorName.isEmpty() || patientName.isEmpty() || locationDetails.isEmpty()) {
                                                uploadError = "দয়া করে রক্তদাতা ও রোগীর নাম উল্লেখ করুন।"
                                            } else {
                                                val countVal = donationNumber.toIntOrNull() ?: 1
                                                viewModel.uploadDonationRecord(
                                                    donorName, recordBloodGroup, patientName, patientProblem, countVal, hospitalName, locationDetails, donationDate, donationTime, ""
                                                )
                                                uploadSuccess = "রক্তদান বিবরণী সফলভাবে আপলোড হয়েছে এবং অডিট লাইনে প্রেরণ করা হল!"
                                                uploadError = null
                                                // reset
                                                donorName = ""
                                                patientName = ""
                                                patientProblem = ""
                                                hospitalName = ""
                                                locationDetails = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                                        modifier = Modifier.fillMaxWidth().testTag("submit_donation_record_btn")
                                    ) {
                                        Text("বিবরণী সাবমিট করুন")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Public user message guide
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("কমিটি কাস্টম অপশন লকড", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "রক্তদানের সফল রেকর্ড আপলোড করা, মেম্বারদের আবেদন অনুমোদন দেওয়া ইত্যাদি এক্সক্লুসিভ ফিচার শুধুমাত্র যুবসমাজ ব্লাড ফাউন্ডেশনের ইমিডিয়েট কমিটি মেম্বারদের ব্যবহারের জন্য সীমাবদ্ধ।",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = JbfRed, textAlign = TextAlign.Center)
        }
    }
}
