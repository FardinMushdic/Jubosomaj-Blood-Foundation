package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.BloodRequest
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed
import com.example.viewmodel.MainViewModel

@Composable
fun RequestBloodScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val requests by viewModel.requests.collectAsState()
    val userRole by viewModel.currentUserType.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    // Form states
    var patientName by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("B+") }
    var hospitalName by remember { mutableStateOf("") }
    var addressDetails by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var reqDate by remember { mutableStateOf("") }
    var isEmergencyState by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    var formMessage by remember { mutableStateOf<String?>(null) }
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
                    text = "রক্তের পিটিশন ও রোগীর কেস",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(if (showForm) "আবেদন বন্ধ করুন" else "নতুন রক্তের আবেদন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                icon = { Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add Request", tint = Color.White) },
                onClick = { showForm = !showForm },
                containerColor = JbfRed,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("toggle_request_form_btn")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA))
                .padding(12.dp)
        ) {
            if (showForm) {
                // Request Blood Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "জরুরি রক্তের আবেদন ফরম (নিবন্ধন ছাড়াই)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JbfRed,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = patientName,
                                onValueChange = { patientName = it },
                                label = { Text("রোগীর নাম", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("req_patient_name"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                            )
                        }

                        // Blood Group Selection Spinner Alternative
                        item {
                            Text("প্রয়োজনীয় রক্তের গ্রুপ:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                bloodGroupsList.take(4).forEach { g ->
                                    val checked = bloodGroup == g
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (checked) JbfRed else JbfLightGray)
                                            .clickable { bloodGroup = g }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(g, color = if (checked) Color.White else JbfCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                bloodGroupsList.drop(4).forEach { g ->
                                    val checked = bloodGroup == g
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (checked) JbfRed else JbfLightGray)
                                            .clickable { bloodGroup = g }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(g, color = if (checked) Color.White else JbfCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = hospitalName,
                                onValueChange = { hospitalName = it },
                                label = { Text("হাসপাতালের নাম", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("req_hospital_name"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = addressDetails,
                                onValueChange = { addressDetails = it },
                                label = { Text("পূর্ণ ঠিকানা ও উপজেলা", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("req_address"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = emergencyPhone,
                                onValueChange = { emergencyPhone = it },
                                label = { Text("মোবাইল নম্বর", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("req_phone"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = reqDate,
                                onValueChange = { reqDate = it },
                                label = { Text("কোন তারিখে রক্ত প্রয়োজন? (উদা: ১০ জুন)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("req_date"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isEmergencyState,
                                    onCheckedChange = { isEmergencyState = it },
                                    colors = CheckboxDefaults.colors(checkedColor = JbfRed)
                                )
                                Text("এটি কি অত্যন্ত জরুরি অবস্থা?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("অতিরিক্ত তথ্য (উদা: ২ ব্যাগ প্লাজমা)", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .testTag("req_notes"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                            )
                        }

                        item {
                            formMessage?.let {
                                Text(it, color = JbfRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (patientName.isEmpty() || hospitalName.isEmpty() || addressDetails.isEmpty() || emergencyPhone.isEmpty() || reqDate.isEmpty()) {
                                        formMessage = "অন অনুগ্রহ করে সকল তথ্য পূরণ করুন।"
                                    } else {
                                        viewModel.submitBloodRequest(
                                            patientName, bloodGroup, hospitalName, addressDetails, emergencyPhone, reqDate, isEmergencyState, notes
                                        )
                                        // Reset
                                        patientName = ""
                                        hospitalName = ""
                                        addressDetails = ""
                                        emergencyPhone = ""
                                        reqDate = ""
                                        notes = ""
                                        formMessage = null
                                        showForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_request_btn")
                            ) {
                                Text("আবেদন পোস্ট করুন")
                            }
                        }
                    }
                }
            } else {
                // Active Requests Lists
                Text(
                    text = "সচল রক্তের গ্রুপ আবেদনসমূহ (${requests.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JbfCharcoal,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (requests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("আপাতত কোনো সক্রিয় রক্তের রিকোয়েস্ট নেই।", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(requests) { req ->
                            BloodRequestItemCard(
                                req = req,
                                roleType = userRole,
                                onStatusChange = { stat -> viewModel.updateRequestStatus(req, stat) }
                            ) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.phone}"))
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BloodRequestItemCard(
    req: BloodRequest,
    roleType: String,
    onStatusChange: (String) -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (req.isEmergency) Color(0xFFFFEBEE) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (req.isEmergency) {
                        Box(
                            modifier = Modifier
                                .background(JbfRed, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("জরুরি", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "রোগী: ${req.patientName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = JbfCharcoal
                    )
                }

                Text(
                    text = req.bloodGroup,
                    color = JbfRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "হাসপাতাল: ${req.hospitalName}", fontSize = 11.sp, color = Color.DarkGray)
            Text(text = "ঠিকানা: ${req.address}", fontSize = 11.sp, color = Color.DarkGray)
            Text(text = "প্রয়োজনের তারিখ: ${req.dateRequired}", fontSize = 11.sp, color = JbfCharcoal, fontWeight = FontWeight.Bold)

            if (req.additionalInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "মন্তব্য: ${req.additionalInfo}", fontSize = 10.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .background(
                            color = when (req.status) {
                                "Completed" -> Color(0xFFE8F5E9)
                                "Managed" -> Color(0xFFE3F2FD)
                                "Cancelled" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFFFF3E0)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = req.getBanglaStatus(),
                        fontSize = 10.sp,
                        color = when (req.status) {
                            "Completed" -> Color(0xFF2E7D32)
                            "Managed" -> Color(0xFF1565C0)
                            "Cancelled" -> Color(0xFFC62828)
                            else -> Color(0xFFE65100)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action Call
                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("কল দিন", fontSize = 11.sp)
                }
            }

            // If Admin or Committee member, let them update status
            if (roleType == "Super Admin" || roleType == "Admin" || roleType == "Moderator" || roleType == "Committee Member") {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))
                Text("অগ্রগতি পরিবর্তন করুন (কমিটি অনুমোদন):", fontSize = 10.sp, color = JbfCharcoal, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusButton("দাতাদাতা খোঁজা", "Searching Donor", req.status == "Searching Donor") { onStatusChange("Searching Donor") }
                    StatusButton("ম্যানেজড", "Managed", req.status == "Managed") { onStatusChange("Managed") }
                    StatusButton("সম্পন্ন", "Completed", req.status == "Completed") { onStatusChange("Completed") }
                    StatusButton("বাতিল", "Cancelled", req.status == "Cancelled") { onStatusChange("Cancelled") }
                }
            }
        }
    }
}

@Composable
fun StatusButton(label: String, target: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) JbfRed else JbfLightGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = if (isSelected) Color.White else JbfCharcoal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
