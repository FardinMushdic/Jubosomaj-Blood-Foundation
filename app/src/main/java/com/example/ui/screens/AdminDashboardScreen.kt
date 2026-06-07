package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationRecord
import com.example.data.model.MemberApplication
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed
import com.example.viewmodel.MainViewModel

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val role by viewModel.currentUserType.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val records by viewModel.records.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) } // 0: Analytics & Alerts, 1: Member Approvals, 2: Donation Audit, 3: Certificate Generator

    val adminTabs = listOf("ড্যাশবোর্ড", "সদস্য অনুমোদন", "রক্তদান রেকর্ড", "সার্টিফিকেট")

    // Check authorization/RBAC
    val isAuthorized = role == "Super Admin" || role == "Admin" || role == "Moderator"

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
                    text = "জেবিএফ অনলাইন অ্যাডমিন কন্ট্রোল ডেস্ক",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        if (!isAuthorized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFFAFAFA))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = JbfRed, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "অননুমোদিত প্রবেশাধিকার!",
                        color = JbfCharcoal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "দুঃখিত, এই এডমিন প্যানেলটি শুধুমাত্র সুপার এডমিন, এডমিন এবং মডারেটরদের ব্যবহারের জন্য সংরক্ষিত। অনুগ্রহ করে উপরের প্রোফাইল রোল সুইচার থেকে রোল পরিবর্তন করুন।",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFFAFAFA))
            ) {
                // Tab Header for Admin Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JbfCharcoal.copy(alpha = 0.05f))
                        .padding(2.dp)
                ) {
                    adminTabs.forEachIndexed { idx, label ->
                        val selected = activeAdminTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) JbfCharcoal else Color.Transparent)
                                .clickable { activeAdminTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else JbfCharcoal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Render respective tab layouts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    when (activeAdminTab) {
                        0 -> AdminMainStatsTab(viewModel)
                        1 -> AdminMemberApprovalsTab(viewModel, applications)
                        2 -> AdminDonationAuditTab(viewModel, records)
                        3 -> AdminCertificateTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMainStatsTab(viewModel: MainViewModel) {
    // Analytics boxes
    val requests by viewModel.requests.collectAsState()
    val donors by viewModel.donors.collectAsState()

    val totalReq = requests.size
    val managedReq = requests.count { it.status == "Completed" || it.status == "Managed" }
    val pendingReq = requests.count { it.status == "Pending" || it.status == "Searching Donor" }
    val totalDonors = donors.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 85.dp)
    ) {
        item {
            Text(
                text = "সংগঠনের কাজের বাস্তব তথ্যচিত্র",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = JbfCharcoal,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Stats Matrix Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminStatCard(title = "মোট ডোনার ডাটাবেজ", value = "$totalDonors জন", tint = JbfRed, modifier = Modifier.weight(1f))
                AdminStatCard(title = "মোট রক্তের আবেদন", value = "$totalReq টি", tint = JbfCharcoal, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminStatCard(title = "ম্যানেজড বা সম্পন্ন", value = "$managedReq টি", tint = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                AdminStatCard(title = "অনুরোধ পেন্ডিং", value = "$pendingReq টি", tint = Color(0xFFE65100), modifier = Modifier.weight(1f))
            }
        }

        // Custom canvas graphics for chart represent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "গত ৪ মাসের ব্লাড ম্যানেজমেন্ট ট্রেন্ড (গ্রাফ)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JbfCharcoal
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw blood trend line graph via Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Grid lines
                        val verticalDivider = height / 4
                        for (i in 0..4) {
                            val y = i * verticalDivider
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Coordinates for 4 points representing Feb, Mar, Apr, May
                        val points = listOf(
                            Offset(0.05f * width, 0.7f * height),
                            Offset(0.35f * width, 0.55f * height),
                            Offset(0.65f * width, 0.3f * height),
                            Offset(0.95f * width, 0.15f * height)
                        )

                        // Draw lines between coordinates
                        for (idx in 0 until points.size - 1) {
                            drawLine(
                                color = JbfRed,
                                start = points[idx],
                                end = points[idx + 1],
                                strokeWidth = 4f
                            )
                        }

                        // Draw joints as circles
                        points.forEach { point ->
                            drawCircle(
                                color = JbfCharcoal,
                                radius = 8f,
                                center = point
                            )
                        }
                    }

                    // Months Label x-axis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ফেব্রুয়ারি (৪৫ ব্যাগ)", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("মার্চ (৬৮ ব্যাগ)", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("এপ্রিল (৯৫ ব্যাগ)", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("মে (১৩০ ব্যাগ)", fontSize = 9.sp, color = JbfRed, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Global Alert Trigger Simulator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "জরুরি সাড়াদান এলার্ট হর্ন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = JbfRed
                    )
                    Text(
                        text = "বিশেষ রক্তের গ্রুপের জন্য সমগ্র ডাটাবেজে তাৎক্ষণিক মোবাইল পুশ বা অ্যালার্ট পাঠাতে এটি ব্যবহার করুন।",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.triggerEmergencyAlert("AB-", "ঢাকা", "রমনা") },
                        colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = "Alert", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("তাৎক্ষনিক AB-নেগেটিভ এলার্ট ট্রিগার করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = tint)
        }
    }
}

@Composable
fun AdminMemberApprovalsTab(viewModel: MainViewModel, applications: List<MemberApplication>) {
    val pendingApp = applications.filter { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 85.dp)
    ) {
        item {
            Text(
                text = "নতুন কমিটির সদস্য হওয়ার আবেদনসমূহ (${pendingApp.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = JbfCharcoal
            )
        }

        if (pendingApp.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো নতুন পেন্ডিং আবেদন পাওয়া যায়নি।", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            items(pendingApp) { app ->
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
                            Text(text = app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                            Text(text = "তারিখ: ${app.applyDate}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "মোবাইল: ${app.phone}", fontSize = 11.sp, color = JbfCharcoal)
                        Text(text = "শিক্ষা প্রতিষ্ঠান: ${app.institution}", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "পেশা: ${app.profession} | দক্ষতা: ${app.skills}", fontSize = 11.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "কেন যোগ দিতে ইচ্ছুক: ${app.reasonToJoin}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateApplicationStatus(app, "Rejected") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("বাতিল করুন", color = JbfCharcoal, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.updateApplicationStatus(app, "Approved", "স্বেচ্ছাসেবক মেম্বার") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Verify", modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("অনুমোদন দিন", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDonationAuditTab(viewModel: MainViewModel, records: List<DonationRecord>) {
    val pendingRecords = records.filter { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 85.dp)
    ) {
        item {
            Text(
                text = "ভলান্টিয়ার আপলোডকৃত রক্তদানের অডিট (${pendingRecords.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = JbfCharcoal
            )
        }

        if (pendingRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো পেন্ডিং রক্তদান সফলতার অডিট বাকি নেই।", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            items(pendingRecords) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "দাতা: ${rec.donorName} (${rec.bloodGroup})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                                Text(text = "রোগী: ${rec.patientName}", fontSize = 11.sp, color = Color.DarkGray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(JbfRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "কেস আইডি: ${rec.recordUid}", fontSize = 9.sp, color = JbfRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "হাসপাতাল: ${rec.hospitalName}", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "তারিখ ও সময়: ${rec.date} (${rec.time})", fontSize = 11.sp, color = Color.DarkGray)
                        Text(text = "আপলোডকারী: ${rec.uploadedBy} [তারিখ: ${rec.uploadDate}]", fontSize = 10.sp, color = JbfRed)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateDonationRecordStatus(rec, "Cancelled") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("ডিলেট বা অমিল", fontSize = 11.sp, color = JbfCharcoal)
                            }
                            Button(
                                onClick = { viewModel.updateDonationRecordStatus(rec, "Verified") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "Approve", modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("যাচাই বা ভেরিফাই করুন", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCertificateTab(viewModel: MainViewModel) {
    var receiverName by remember { mutableStateOf("রাসেল মাহমুদ") }
    var selectedCertificateType by remember { mutableStateOf("Participation") } // Participation, Volunteer, Appreciation
    var dateString by remember { mutableStateOf("০৭ জুন, ২০২৬") }

    var generated by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 85.dp)
    ) {
        item {
            Text(
                text = "অফিসিয়াল সার্টিফিকেট জেনারেটর (পিডিএফ/ইমেজ)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = JbfCharcoal
            )
            Text(
                text = "ভলান্টিয়ার বা আমাদের ক্যাম্পেইনে সাহায্যকারীদের জন্য ডিজিটাল শংসাপত্র প্রস্তুত করুন।",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        item {
            OutlinedTextField(
                value = receiverName,
                onValueChange = {
                    receiverName = it
                    generated = false
                },
                label = { Text("গ্রহীতার নাম", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cert_receiver_name"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
            )
        }

        item {
            Text("শংসাপত্রের ধরণ:", fontSize = 11.sp, color = JbfCharcoal, fontWeight = FontWeight.Bold)
            val types = listOf(
                Pair("অংশগ্রহণকারী শংসাপত্র", "Participation"),
                Pair("স্বেচ্ছাসেবী স্বীকৃতি", "Volunteer"),
                Pair("বিশেষ ধন্যবাদ শংসাপত্র", "Appreciation")
            )
            types.forEach { t ->
                val active = selectedCertificateType == t.second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (active) JbfRed.copy(alpha = 0.1f) else Color.White)
                        .border(1.dp, if (active) JbfRed else Color.LightGray, RoundedCornerShape(6.dp))
                        .clickable {
                            selectedCertificateType = t.second
                            generated = false
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = active,
                        onClick = {
                            selectedCertificateType = t.second
                            generated = false
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = JbfRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(t.first, fontSize = 12.sp, fontWeight = weightFor(active), color = JbfCharcoal)
                }
            }
        }

        item {
            OutlinedTextField(
                value = dateString,
                onValueChange = {
                    dateString = it
                    generated = false
                },
                label = { Text("ইস্যু তারিখ", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cert_issue_date"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
            )
        }

        item {
            Button(
                onClick = { generated = true },
                colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_cert_btn")
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium")
                Spacer(modifier = Modifier.width(6.dp))
                Text("ডিজিটাল প্রশংসাপত্র তৈরি করুন")
            }
        }

        // Preview Generated Certificate
        if (generated) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("certificate_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF7)), // cream colored diploma theme
                    border = BorderStroke(3.dp, JbfRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "যুবসমাজ ব্লাড ফাউন্ডেশন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = JbfRed,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "সেবাই আমাদের আদর্শ ও প্রতিজ্ঞা",
                            fontSize = 11.sp,
                            color = JbfCharcoal,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = JbfRed.copy(alpha = 0.3f), modifier = Modifier.width(150.dp))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = when (selectedCertificateType) {
                                "Participation" -> "অংশগ্রহণ প্রশংসাপত্র"
                                "Volunteer" -> "স্বেচ্ছাসেবী স্বীকৃতি শংসাপত্র"
                                else -> "বিশেষ অনারারি প্রশংসা শংসাপত্র"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JbfCharcoal,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("এই শংসাপত্রটি অত্যন্ত আনন্দের সাথে প্রদান করা হচ্ছে—", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = receiverName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = JbfRed,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedCertificateType) {
                                "Participation" -> "যুবসমাজ ব্লাড ফাউন্ডেশন কর্তৃক আয়োজিত গ্রীষ্মকালীন স্বেচ্ছায় রক্তদান ক্যাম্পেইন-২০২৬ এ সাফল্যের সাথে নিষ্কামভাবে অংশ নেওয়ার স্বীকৃতিস্বরূপ এই ক্রেডেন্সিয়াল প্রদান করা হল।"
                                "Volunteer" -> "সংগঠনের প্রতিটি রক্ত ম্যানেজমেন্ট, ডনার তলব এবং অসহায় রোগীর পাশে থেকে মাঠপর্যায়ে রক্ত সংগ্রহ বুথে নিরলস ও অতন্দ্র প্রহরী সদৃশ দায়িত্ব পালনের মহান স্বীকৃতিস্বরূপ।"
                                else -> "রক্তদাতা অনুসন্ধানে অসামান্য অবদান রেখে এবং সংগঠনের অভ্যন্তরীণ কার্যক্রমে সর্বোচ্চ তৎপরতা প্রদার কেরে অসাধারণ ত্যাগের নজির রাখায় আমরা ধন্য ও গর্বিত।"
                            },
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মেহরাব হাসান", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                                Text("সভাপতি ও পরিচালক", fontSize = 9.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(dateString, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                                Text("ইস্যু তারিখ", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Download trigger
                        Button(
                            onClick = {
                                viewModel.dismissAlert() // simple trigger to check button click
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JbfCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ছবি ও পিডিএফ শংসাপত্র ডাউনলোড করুন")
                        }
                    }
                }
            }
        }
    }
}
