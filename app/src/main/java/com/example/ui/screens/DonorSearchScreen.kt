package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donor
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed
import com.example.viewmodel.MainViewModel

@Composable
fun DonorSearchScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val searchResults by viewModel.searchResults.collectAsState()
    val role by viewModel.currentUserType.collectAsState()

    var selectedGroup by remember { mutableStateOf("A+") }
    var districtInput by remember { mutableStateOf("ঢাকা") }
    var upazilaInput by remember { mutableStateOf("মিরপুর") }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    // Run query on load
    LaunchedEffect(selectedGroup, districtInput, upazilaInput) {
        viewModel.searchDonors(selectedGroup, districtInput, upazilaInput)
    }

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
                    text = "রক্তদাতা অনুসন্ধান ও লাইভ কুয়েরি",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
                .padding(12.dp)
        ) {
            // Filter Area Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "সার্চ ফিল্টারসমূহ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JbfCharcoal
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Blood Groups selector Row
                    Text("রক্তের গ্রুপ নির্বাচন করুন:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        bloodGroups.chunked(4).forEach { chunk ->
                            Column(modifier = Modifier.weight(1f)) {
                                chunk.forEach { group ->
                                    val isSelected = selectedGroup == group
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) JbfRed else JbfLightGray)
                                            .clickable { selectedGroup = group }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = group,
                                            color = if (isSelected) Color.White else JbfCharcoal,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // District & Upazila input fields
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = districtInput,
                            onValueChange = { districtInput = it },
                            label = { Text("জেলা", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_district_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JbfRed,
                                focusedLabelColor = JbfRed
                            )
                        )
                        OutlinedTextField(
                            value = upazilaInput,
                            onValueChange = { upazilaInput = it },
                            label = { Text("উপজেলা", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_upazila_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JbfRed,
                                focusedLabelColor = JbfRed
                            )
                        )
                    }

                    // Admin Trigger Emergency alert
                    if (role == "Super Admin" || role == "Admin" || role == "Moderator") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.triggerEmergencyAlert(selectedGroup, districtInput, upazilaInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trigger_emergency_btn")
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = "Alert", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("জরুরি অ্যালার্ট সাইরেন পাঠান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nearest matches header
            Text(
                text = "অনুসন্ধানের ফলাফল (${searchResults.size} জন রক্তদাতা)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = JbfCharcoal,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = "Not Found", tint = Color.LightGray, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "কোনো রক্তদাতা মেলাতে পারা যায়নি।\nভিন্ন জেলা বা রক্তের গ্রুপ দিয়ে আবার চেষ্টা করুন।",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(searchResults) { donor ->
                        DonorSearchResultCard(donor = donor) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonorSearchResultCard(donor: Donor, onCall: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blood group circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(JbfRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = donor.bloodGroup,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = donor.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = JbfCharcoal
                    )
                    if (donor.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Donor",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = "${donor.district} | ${donor.upazila}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "সর্বশেষ রক্তদান: ${donor.lastDonationDate}",
                    fontSize = 11.sp,
                    color = JbfRed,
                    fontWeight = FontWeight.Medium
                )
                // Secure Medical Records Alert indicator
                if (donor.secureMedicalRecords.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = "Medical", tint = Color(0xFF1976D2), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ভেরিফাইড মেডিকেল রেকর্ড এনক্রিপ্টেড",
                            color = Color(0xFF1976D2),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Call Button
            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(JbfLightGray)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call Donor", tint = JbfRed)
            }
        }
    }
}
