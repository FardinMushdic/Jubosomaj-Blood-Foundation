package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodRequest
import com.example.data.model.Event
import com.example.data.model.Notice
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToRequest: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToKnowledge: () -> Unit
) {
    val context = LocalContext.current
    val requests by viewModel.requests.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val events by viewModel.events.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncErrorMessage.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Banners state
    val bannerTexts = listOf(
        Pair("রক্ত দিন, জীবন বাঁচান", "আপনার এক ব্যাগ রক্ত বাঁচাতে পারে একটি মুমূর্ষু মানুষের প্রাণ। আজই রক্তদানে এগিয়ে আসুন।"),
        Pair("যুবসমাজ ব্লাড ফাউন্ডেশন", "বাংলাদেশের একটি নির্ভরযোগ্য অলাভজনক রক্তদাতা নেটওয়ার্ক সিবিএম।"),
        Pair("অফিসিয়াল গুগল ওয়েবসাইট", "আমাদের দলগত অফিশিয়াল ওয়েবসাইট পরিদর্শন করে বিস্তারিত জানুন ও আপনার মতামত পেশ করুন।")
    )
    val pagerState = rememberPagerState(pageCount = { bannerTexts.size })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Google Sites Web Sync Header Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = JbfCharcoal)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অফিসিয়াল ওয়েবসাইট সার্ভিস",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "sites.google.com/view/teamofjbf",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        // Website browser button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/teamofjbf"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("website_button"),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Website", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ওয়েবসাইট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sync button
                        Box {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(
                                    onClick = { viewModel.syncWithWebsite() },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            syncError?.let {
                Text(
                    text = "সিঙ্ক ত্রুটি: $it (অফলাইন মোড অ্যাক্টিভ)",
                    color = JbfRed,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Banner Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) { page ->
                    val banner = bannerTexts[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(JbfRed, JbfRed.copy(alpha = 0.8f), JbfCharcoal)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Text(
                                text = banner.first,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = banner.second,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Pager Indicator
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(bannerTexts.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) JbfRed else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 12.dp else 6.dp)
                                .height(6.dp)
                        )
                    }
                }
            }
        }

        // 3. Quick Action Menu
        item {
            Text(
                text = "দ্রুত সেবা মেনু",
                color = JbfCharcoal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickMenuOption(
                    title = "রক্ত খুঁজুন",
                    icon = Icons.Default.Search,
                    color = JbfRed,
                    onClick = onNavigateToSearch,
                    tag = "quick_search_btn"
                )
                QuickMenuOption(
                    title = "রক্তের আবেদন",
                    icon = Icons.Default.AddBox,
                    color = JbfCharcoal,
                    onClick = onNavigateToRequest,
                    tag = "quick_request_btn"
                )
                QuickMenuOption(
                    title = "দাতা হোন",
                    icon = Icons.Default.CardGiftcard,
                    color = JbfRed,
                    onClick = onNavigateToRegister,
                    tag = "quick_register_btn"
                )
                QuickMenuOption(
                    title = "গাইডবুক",
                    icon = Icons.Default.MenuBook,
                    color = JbfCharcoal,
                    onClick = onNavigateToKnowledge,
                    tag = "quick_guide_btn"
                )
            }
        }

        // 4. Live Broadcast Module
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (liveState.isActive) Color(0xFFFFEBEE) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (liveState.isActive) JbfRed else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (liveState.isActive) "লাইভ সম্প্রচার চলছে!" else "লাইভ সম্প্রচার (বন্ধ আছে)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (liveState.isActive) JbfRed else JbfCharcoal
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (liveState.isActive) {
                        Text(
                            text = liveState.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = JbfCharcoal
                        )
                        Text(
                            text = "প্লাটফর্ম: ${liveState.platform} | দর্শক: ${liveState.viewerCount} জন",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val url = if (liveState.platform.contains("Facebook")) {
                                    "https://www.facebook.com"
                                } else {
                                    "https://www.youtube.com"
                                }
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("লাইভে যুক্ত হোন")
                        }
                    } else {
                        Text(
                            text = "যুবসমাজ ব্লাড ফাউন্ডেশনের ফেসবুক লাইভ বা ইউটিউব ক্যাম্পেইন শুরু হলে এখানে সরাসরি দেখার লিংক ও নোটিফিকেশন দেয়া হবে।",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // 5. Emergency Blood Requests (জরুরি রক্তের আবেদন)
        item {
            val emergencyRequests = requests.filter { it.isEmergency && it.status != "Completed" && it.status != "Cancelled" }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "জরুরি রক্তের আবেদন (${emergencyRequests.size})",
                    color = JbfRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সব দেখুন",
                    color = JbfRed,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigateToRequest() }
                )
            }

            if (emergencyRequests.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো জরুরি রক্তদানের আবেদন নেই।",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(emergencyRequests) { req ->
                        EmergencyRequestCard(req = req) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.phone}"))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }

        // 6. Recent Notices (সাম্প্রতিক নোটিশ)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক নোটিশ",
                    color = JbfCharcoal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (notices.isEmpty()) {
                Text(
                    text = "কোনো সচল নোটিশ পাওয়া যায়নি।",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    notices.take(3).forEach { notice ->
                        NoticeCard(notice = notice)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 7. Upcoming Events (আসন্ন ইভেন্ট)
        item {
            Text(
                text = "আসন্ন ইভেন্ট",
                color = JbfCharcoal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            if (events.isEmpty()) {
                Text(
                    text = "আপাতত কোনো আসন্ন ইভেন্ট সূচি নেই।",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    events.take(2).forEach { event ->
                        EventCard(event = event)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // 8. Organization Statistics (সংগঠনের পরিসংখ্যান)
        item {
            Text(
                text = "আমাদের কার্যক্রমের সংক্ষেপিত পরিসংখ্যান",
                color = JbfCharcoal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 10.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(containerColor = JbfCharcoal)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = "মোট রক্তদাতা", value = "১৫৪০+")
                    StatBox(label = "রক্তদান সম্পন্ন", value = "৩৮৯০+")
                    StatBox(label = "সক্রিয় ভলান্টিয়ার", value = "১৮০+")
                    StatBox(label = "জেবিএফ সাড়াদান", value = "১০০%")
                }
            }
        }

        // 9. AI Assistant Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onNavigateToAi() },
                colors = CardDefaults.cardColors(containerColor = JbfLightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "AI Assistant",
                        tint = JbfRed,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আমাদের বুদ্ধিমান AI সহকারী!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = JbfCharcoal
                        )
                        Text(
                            text = "রক্তদান নির্দেশিকা ও গ্রুপ সম্পর্কিত যেকোনো প্রশ্ন করতে ক্লিক করুন।",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForwardIos,
                        contentDescription = "Forward",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 10. Donation Successful Story
        item {
            Text(
                text = "সফল রক্তদানের গল্প",
                color = JbfCharcoal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "“রাতের ৩ টায় মুমূর্ষু প্রসূতি মায়ের জন্য ও-নেগেটিভ রক্তের গ্রুপ খোঁজার পর জেবিএফ গ্রুপে অ্যালার্ট পাওয়ার ১৫ মিনিটের মাথায় দাতা তানভীর ভাই হাসপাতালে উপস্থিত হয়েছিলেন এবং প্রসব সফল হয়েছিল। জেবিএফ এর এই ঋণ আমাদের পরিবার কখনো শোধ করতে পারবে না।”",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "— জহিরুল ইসলাম (তাহমিনা আক্তারের স্বামী)",
                        fontSize = 11.sp,
                        color = JbfRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickMenuOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = JbfCharcoal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmergencyRequestCard(req: BloodRequest, onCall: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(JbfRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("জরুরি", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = req.bloodGroup,
                    color = JbfRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("রোগী: ${req.patientName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
            Text("স্থান: ${req.hospitalName}", fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("প্রয়োজন: ${req.dateRequired}", fontSize = 11.sp, color = JbfCharcoal, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = JbfRed),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("কল দিন", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun NoticeCard(notice: Notice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notice.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = JbfCharcoal,
                    modifier = Modifier.weight(1f)
                )
                if (notice.isFromWebsite) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("ওয়েবসাইট", fontSize = 8.sp, color = Color.DarkGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notice.body, fontSize = 11.sp, color = Color.Gray, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "প্রকাশের তারিখ: ${notice.publishDate}", fontSize = 9.sp, color = JbfRed, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = event.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = JbfRed, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.location, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Schedule", tint = Color.Gray, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "তারিখ: ${event.date} | সময়: ${event.time}", fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = event.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "নিবন্ধিত: ${event.registrationCount} জন", fontSize = 10.sp, color = JbfRed, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(JbfRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("অনলাইন রেজিস্ট্রেশন", fontSize = 9.sp, color = JbfRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = Color.LightGray)
    }
}
