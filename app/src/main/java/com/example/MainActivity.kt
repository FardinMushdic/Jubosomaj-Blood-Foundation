package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.JbfCharcoal
import com.example.ui.theme.JbfRed
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppShell()
            }
        }
    }
}

@Composable
fun AppShell() {
    val viewModel: MainViewModel = viewModel()
    var currentTab by remember { mutableStateOf(0) } // 0: Home, 1: Search, 2: Requests, 3: Chat, 4: Admin, 5: Profile

    val alertNotification by viewModel.alertNotification.collectAsState()

    // Navigation Items
    val navItems = listOf(
        NavigationItem("হোম", Icons.Default.Home, "nav_home_tab"),
        NavigationItem("খুঁজুন", Icons.Default.Search, "nav_search_tab"),
        NavigationItem("আবেদন", Icons.Default.AddBox, "nav_requests_tab"),
        NavigationItem("চ্যাট হাব", Icons.Default.Forum, "nav_chat_tab"),
        NavigationItem("এডমিন", Icons.Default.AdminPanelSettings, "nav_admin_tab"),
        NavigationItem("আইডি কার্ড", Icons.Default.ContactPage, "nav_profile_tab")
    )

    Scaffold(
        bottomBar = {
            // Respect safe navigation bar padding of physical/virtual devices
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                containerColor = JbfCharcoal,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val checked = currentTab == index
                    NavigationBarItem(
                        selected = checked,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (checked) JbfRed else Color.LightGray
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 9.sp,
                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                                color = if (checked) Color.White else Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area based on Selected Tab
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { currentTab = 1 },
                    onNavigateToRequest = { currentTab = 2 },
                    onNavigateToRegister = { currentTab = 5 }, // profile form acts as registration
                    onNavigateToAi = { currentTab = 3 }, // chat hub hosts bot
                    onNavigateToChat = { currentTab = 3 },
                    onNavigateToKnowledge = { currentTab = 3 } // unified knowledge/chat triggers
                )
                1 -> DonorSearchScreen(viewModel = viewModel)
                2 -> RequestBloodScreen(viewModel = viewModel)
                3 -> ChatScreen(viewModel = viewModel)
                4 -> AdminDashboardScreen(viewModel = viewModel)
                5 -> ProfileScreen(viewModel = viewModel)
            }

            // Realtime Emergency Notification Broadcast Overlay Banner
            alertNotification?.let { alertText ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JbfRed)
                        .padding(12.dp)
                        .align(Alignment.TopCenter)
                        .testTag("emergency_alert_popup")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alertText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissAlert() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
