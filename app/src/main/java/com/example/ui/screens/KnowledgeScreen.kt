package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfLightGray
import com.example.ui.theme.JbfRed

@Composable
fun KnowledgeScreen() {
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    val guides = listOf(
        Pair("১. রক্তদানের পূর্ব প্রস্তুতি:", "রক্তদানের আগের দিন পর্যাপ্ত ঘুম জরুরি। রক্তদানের অন্তত ৩ ঘণ্টা আগে ভারী পুষ্টিকর খাবার খেয়ে নেবেন এবং খালি পেটে রক্ত দেওয়া পরিহার করবেন। পর্যাপ্ত তরল খাবার ও পানি পান করবেন।"),
        Pair("২. ডাক্তারী চেকআপ:", "রক্ত দেওয়ার পূর্বে প্রশিক্ষক আপনার শরীরের ওজন, রক্তচাপ (Blood Pressure), তাপমাত্রা এবং রক্তের হিমোগ্লোবিন পরিমাপ করে আপনার উপযোগিতা নিশ্চিত করবেন।"),
        Pair("৩. রক্তদান প্রক্রিয়া:", "একটি ভেরিফাইড জীবানুমুক্ত ওয়ান-টাইম সুচ ব্যবহারের মাধ্যমে অতি যত্নসহকারে ৩৫০-৪৫০ মিলি রক্ত নেওয়া হয়। এতে মাত্র ৮ থেকে ১০ মিনিট সময় লাগে এবং বিন্দুমাত্র ব্যথা অনুভূত হয় না।"),
        Pair("৪. রক্তদান পরবর্তী বিশ্রাম:", "রক্ত দেওয়ার পর অন্তত ৫ থেকে ১০ মিনিট বেডে শুয়ে বিশ্রাম নিন। প্লাস্টার সংলগ্ন স্থানটি অন্তত ২ ঘণ্টা ভাঁজ করবেন না। আগামী কয়েক ঘণ্টা অতিরিক্ত ভারী কাজ বা সিঁড়ি বেয়ে ওঠা থেকে বিরত থাকুন।")
    )

    val faqs = listOf(
        Pair("রক্ত দিলে কি কোনো ক্ষতি বা দুর্বলতা হয়?", "না, একদমই নয়! একজন সুস্থ মানুষের শরীরে প্রায় ৫ থেকে ৬ লিটার রক্ত থাকে। এর মাত্র ৩৫০-৪৫০ মিলিগ্রাম রক্ত সংগ্রহ করা হয়। শরীর মাত্র কয়েক সপ্তাহের ভেতরই পুনরায় রক্তের সেলুলার ঘাটতি পূরণ করে নেয়।"),
        Pair("নিরাপদ রক্তদানের নুন্যতম শারীরিক যোগ্যতা কি?", "বয়স ১৮ থেকে ৬০ বছর হতে হবে। ওজন নূন্যতম ৪৫ কেজি (পুরুষ) অথবা ৪৭ কেজি (নারী)। রক্তচাপ এবং শরীরের তাপমাত্রা স্বাভাবিক ও জ্বরহীন হতে হবে। হিমোগ্লোবিন মাপা কমপক্ষে ১২.৫ গ্রাম হতে হবে।"),
        Pair("কোন ও রক্তের গ্রুপ সবচেয়ে বিরল বা দুষ্প্রাপ্য?", "সাধারণত Rh-Negative গ্রুপসমূহ যেমন: AB-Negative (AB-), A-Negative (A-), B-Negative (B-) এবং O-Negative (O-) অত্যন্ত বিরল। বাংলাদেশে মাত্র ০.৫% মানুষের রক্ত নেগেটিভ।"),
        Pair("কতদিন পর পর একজন রক্তদাতা রক্ত দিতে পারেন?", "নিয়ম অনুযায়ী প্রতি ১২০ দিন (৪ মাস) পর পর রক্ত দেওয়া যায়। নারীদের ক্ষেত্রে ঋতুচক্রের ও শারীরিক সক্ষমতার বিবেচনায় এ ব্যবধান কিছুটা বাড়ানোর পরামর্শ দেওয়া হয়।")
    )

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
                    text = "রক্তদান জ্ঞানকোষ ও স্বাস্থ্য নির্দেশিকা",
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
            // Header Image/Icon
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JbfRed)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Guide", modifier = Modifier.size(48.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("জেবিএফ স্বাস্থ্য এডুকেশন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("নিরাপদ রক্তদান গাইডবুক ও বৈজ্ঞানিক প্রশ্নোত্তর প্যানেল", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            // A. Blood donor guides
            item {
                Text("রক্তদানের ধাপে ধাপে নির্দেশিকা", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
            }

            items(guides) { (title, content) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JbfRed)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = content, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 16.sp)
                    }
                }
            }

            // B. FAQ area
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("সাধারণ জিজ্ঞাসিত প্রশ্নসমূহ (FAQ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal)
            }

            items(faqs.mapIndexed { idx, pair -> Triple(idx, pair.first, pair.second) }) { (index, question, answer) ->
                val isExpanded = expandedFaqIndex == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaqIndex = if (isExpanded) null else index },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = question, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JbfCharcoal, modifier = Modifier.weight(1f))
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand FAQ",
                                tint = JbfRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = answer, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
