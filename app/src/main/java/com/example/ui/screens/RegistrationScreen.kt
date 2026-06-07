package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun RegistrationScreen(viewModel: MainViewModel) {
    var activeTab by remember { mutableStateOf(0) } // 0: Donor Registration, 1: Member Application

    // Tab labels
    val tabs = listOf("রক্তদাতা নিবন্ধন", "কমিটি মেম্বার আবেদন")

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
                    text = "নিবন্ধন ফোরাম ও সংযুক্তি",
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
            // Tab Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JbfLightGray)
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) JbfRed else Color.Transparent)
                            .clickable { activeTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else JbfCharcoal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeTab == 0) {
                DonorRegistrationForm(viewModel)
            } else {
                CommitteeMemberForm(viewModel)
            }
        }
    }
}

@Composable
fun DonorRegistrationForm(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("B+") }
    var birthDate by remember { mutableStateOf("২০০৪-০১-০১") }
    var gender by remember { mutableStateOf("পুরুষ") }
    var district by remember { mutableStateOf("ঢাকা") }
    var upazila by remember { mutableStateOf("মিরপুর") }
    var phone by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("Never") }
    var medicalNote by remember { mutableStateOf("") }

    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 85.dp)
        ) {
            item {
                Text(
                    text = "মানবসেবায় রক্তদাতা হিসেবে নিবন্ধন করুন",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JbfRed
                )
                Text(
                    text = "আপনার সঠিক তথ্য প্রদান করুন, তথ্য ভেরিফাই হওয়ার পর তা সার্চে ভিজিবল হবে।",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("রক্তদাতার নাম", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_donor_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                Text("রক্তের গ্রুপ:", fontSize = 11.sp, color = JbfCharcoal, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bloodGroups.take(4).forEach { g ->
                        val checked = bloodGroup == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (checked) JbfRed else JbfLightGray)
                                .clickable { bloodGroup = g }
                                .padding(vertical = 8.dp),
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
                    bloodGroups.drop(4).forEach { g ->
                        val checked = bloodGroup == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (checked) JbfRed else JbfLightGray)
                                .clickable { bloodGroup = g }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(g, color = if (checked) Color.White else JbfCharcoal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("জন্মতারিখ (উদা: ২০০৪-০১-০১)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_donor_birth"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                Text("লিঙ্গ:", fontSize = 11.sp, color = JbfCharcoal, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("পুরুষ", "নারী", "অন্যান্য").forEach { g ->
                        val checked = gender == g
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { gender = g }
                        ) {
                            RadioButton(
                                selected = checked,
                                onClick = { gender = g },
                                colors = RadioButtonDefaults.colors(selectedColor = JbfRed)
                            )
                            Text(g, fontSize = 11.sp, fontWeight = weightFor(checked))
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("জেলা", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reg_donor_district"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                    )
                    OutlinedTextField(
                        value = upazila,
                        onValueChange = { upazila = it },
                        label = { Text("উপজেলা", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reg_donor_upazila"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("যোগাযোগের মোবাইল নম্বর", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_donor_phone"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = lastDonationDate,
                    onValueChange = { lastDonationDate = it },
                    label = { Text("সর্বশেষ রক্তদানের তারিখ? (YYYY-MM-DD, না দিলে Never)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_donor_last_date"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = medicalNote,
                    onValueChange = { medicalNote = it },
                    label = { Text("মেডিকেল রেকর্ড বা শারিরীক অবস্থা (উদা: ডায়াবেটিস নেই)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                successMsg?.let {
                    Text(it, color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                errorMsg?.let {
                    Text(it, color = JbfRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (name.isEmpty() || phone.isEmpty()) {
                            errorMsg = "রক্তদাতার নাম ও মোবাইল নম্বর প্রদান করা আবশ্যক।"
                        } else {
                            viewModel.registerDonor(
                                name, "", bloodGroup, birthDate, gender, district, upazila, phone, lastDonationDate, medicalNote
                            )
                            successMsg = "অভিনন্দন! আপনার রক্তদাতা ডাটাবেজ ভুক্তি সফল হয়েছে।"
                            errorMsg = null
                            // clear
                            name = ""
                            phone = ""
                            medicalNote = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_donor_reg_btn")
                ) {
                    Text("নিবন্ধন সম্পন্ন করুন")
                }
            }
        }
    }
}

@Composable
fun CommitteeMemberForm(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("ঢাকা") }
    var school by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var skillText by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }

    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 85.dp)
        ) {
            item {
                Text(
                    text = "কমিটি মেম্বার ও ভলান্টিয়ার আবেদন ফর্ম",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JbfCharcoal
                )
                Text(
                    text = "সংগঠনে সক্রিয় সদস্য হতে প্রয়োজনীয় তথ্যসহ ফরমটি নির্ভুলভাবে পূরণ করুন।",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আবেদনকারীর পূর্ণ নাম", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_member_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_member_phone"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("বাসস্থান / জেলা ঠিকানা", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_member_address"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("শিক্ষা প্রতিষ্ঠান", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_member_school"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text("পেশা", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_member_job"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = skillText,
                    onValueChange = { skillText = it },
                    label = { Text("দক্ষতা (উদা: পোস্টার ডিজাইন, ফটোশপ, ক্যাম্প লিডিং)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_member_skills"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                OutlinedTextField(
                    value = coverLetter,
                    onValueChange = { coverLetter = it },
                    label = { Text("কেন আমাদের সংগঠনের সদস্য হতে চান?", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("reg_member_reason"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )
            }

            item {
                successMsg?.let {
                    Text(it, color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                errorMsg?.let {
                    Text(it, color = JbfRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (name.isEmpty() || phone.isEmpty() || coverLetter.isEmpty()) {
                            errorMsg = "নাম, মোবাইল ও সদস্য হতে চাওয়ার আবশ্যিক কারণ উল্লেখ করুন।"
                        } else {
                            viewModel.submitMemberApplication(
                                name, "", phone, address, school, job, skillText, coverLetter
                            )
                            successMsg = "সদস্যপদের আবেদন সফলভাবে জমা হয়েছে। ধন্যবাদ।"
                            errorMsg = null
                            // clear form
                            name = ""
                            phone = ""
                            coverLetter = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_member_apply_btn")
                ) {
                    Text("আবেদন জমা দিন")
                }
            }
        }
    }
}

@Composable
fun weightFor(isSelected: Boolean): FontWeight {
    return if (isSelected) FontWeight.Bold else FontWeight.Normal
}
