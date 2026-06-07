package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed
import com.example.viewmodel.AiMessage
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    var selectedChannelTab by remember { mutableStateOf(0) } // 0: AI Assistant, 1: Public Support, 2: Committee Forum
    val channels = listOf("এআই সহকারী", "পাবলিক সাপোর্ট", "অভ্যন্তরীণ কমিটি")

    val role by viewModel.currentUserType.collectAsState()

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
                    text = "জেবিএফ লাইভ চ্যাট হাব",
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
        ) {
            // Channel selection row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JbfLightGray)
                    .padding(2.dp)
            ) {
                channels.forEachIndexed { idx, label ->
                    val checked = selectedChannelTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (checked) JbfRed else Color.Transparent)
                            .clickable { selectedChannelTab = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (checked) Color.White else JbfCharcoal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Render selected channel view
            when (selectedChannelTab) {
                0 -> AiChatTab(viewModel)
                1 -> LiveSupportChatTab(viewModel)
                2 -> CommitteeGroupChatTab(viewModel, role)
            }
        }
    }
}

@Composable
fun AiChatTab(viewModel: MainViewModel) {
    val history by viewModel.aiChatHistory.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom of chat
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Warning if API Key is placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Security Note", tint = Color(0xFFF57F17), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "জেবিএফ এআই সহকারী সার্ভিসের সিকিউরড মোড সক্রিয় আছে। এটি সাধারণ স্বাস্থ্য জিজ্ঞাসায় সহায়তা দেয়।",
                    fontSize = 9.sp,
                    color = Color(0xFF5D4037)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(history) { msg ->
                val isUser = msg.sender == "user"
                val isSystem = msg.sender == "সিস্টেম"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = when {
                        isSystem -> Arrangement.Center
                        isUser -> Arrangement.End
                        else -> Arrangement.Start
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (!isUser) 0.dp else 12.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                )
                            )
                            .background(
                                when {
                                    isSystem -> Color(0xFFEEEEEE)
                                    isUser -> JbfRed
                                    else -> JbfCharcoal
                                }
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isUser) "আপনি" else if (isSystem) "সিস্টেম নোটিস" else "জেবিএফ এআই সহকারী",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser || !isSystem) Color.White else JbfCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                fontSize = 12.sp,
                                color = if (isUser || !isSystem) Color.White else JbfCharcoal
                            )
                        }
                    }
                }
            }

            if (loading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = JbfCharcoal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("উত্তর তৈরি হচ্ছে...", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Input send box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("রক্তের কোনো গ্রুপ সবচেয়ে বিরল? জিজ্ঞাসা করুন...", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        viewModel.askAiAssistant(inputQuery)
                        inputQuery = ""
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JbfRed)
                        .testTag("ai_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(75.dp))
    }
}

@Composable
fun LiveSupportChatTab(viewModel: MainViewModel) {
    val messages by viewModel.supportMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Text(
                text = "এটি একটি ওপেন সাপোর্ট লাইন। যেকোনো জিজ্ঞাসা করলে আমাদের স্বেচ্ছাসেবকরা সরাসরি প্যানেল থেকে রিপ্লে দিবেন।",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(10.dp)
            )
        }

        // Chat lists
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderName == "মেহরাব হাসান" || msg.senderName == "কাজী ফয়সাল" || msg.senderName == "মেহবুবা রহমান"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMe) JbfRed else Color.White)
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.senderName + " (${msg.senderRole})",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White else JbfCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.messageText,
                                fontSize = 12.sp,
                                color = if (isMe) Color.White else JbfCharcoal
                            )
                        }
                    }
                }
            }
        }

        // Input Send row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("সাপোর্টে মেসেজ লিখুন...", fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("support_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
            )
            IconButton(
                onClick = {
                    viewModel.sendChatMessage(inputQuery, "SUPPORT")
                    inputQuery = ""
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JbfCharcoal)
                    .testTag("support_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(75.dp))
    }
}

@Composable
fun CommitteeGroupChatTab(viewModel: MainViewModel, role: String) {
    val messages by viewModel.committeeMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // check if member
    val isMember = role == "Super Admin" || role == "Admin" || role == "Moderator" || role == "Committee Member"

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (!isMember) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = JbfRed, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "অভ্যন্তরীণ কমিটি চ্যাটরুম লকড!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JbfCharcoal,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "এই অংশটি শুধুমাত্র আমাদের সংগঠনের সক্রিয় কমিটি সদস্যগণ ব্যবহার করতে পারবেন অত্যন্ত সুরক্ষিত উপায়ে।",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = JbfCharcoal)
            ) {
                Text(
                    text = "সদস্য গ্রুপ চ্যাটরুম - ফাইল ও ছবি শেয়ারিং এনক্রিপ্টেড",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // message board
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderName == viewModel.loggedInUser.value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isMe) JbfRed else Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = msg.senderName + " (${msg.senderRole})",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White else JbfRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.messageText,
                                    fontSize = 12.sp,
                                    color = if (isMe) Color.White else JbfCharcoal
                                )

                                // Handle mock attachment
                                if (msg.fileName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isMe) Color.White.copy(alpha = 0.15f) else JbfLightGray)
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (msg.fileName.endsWith(".pdf")) Icons.Default.InsertDriveFile else Icons.Default.Image,
                                            contentDescription = "Attachment",
                                            tint = if (isMe) Color.White else JbfRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = msg.fileName,
                                            fontSize = 9.sp,
                                            color = if (isMe) Color.White else JbfCharcoal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Send Row with simulated attachment options (Image / File / Clip)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mock Attachment Action
                IconButton(
                    onClick = {
                        viewModel.sendChatMessage("সম্মেলন ড্রাফট রিপোর্ট ফাইল সংযুক্তি", "COMMITTEE", "camp_report_jbf.pdf", "camp_report_jbf.pdf")
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Add File", tint = JbfCharcoal)
                }

                IconButton(
                    onClick = {
                        viewModel.sendChatMessage("ক্যাম্পিং প্রোগ্রামের ফটো শেয়ার", "COMMITTEE", "donation_camp_photo.jpg", "donation_camp_photo.jpg")
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Add Photo", tint = JbfCharcoal)
                }

                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("গ্রুপে মেসেজ লিখুন...", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("committee_chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JbfRed)
                )

                IconButton(
                    onClick = {
                        viewModel.sendChatMessage(inputQuery, "COMMITTEE")
                        inputQuery = ""
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JbfRed)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(75.dp))
        }
    }
}
