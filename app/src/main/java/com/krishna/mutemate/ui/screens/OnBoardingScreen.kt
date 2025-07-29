package com.krishna.mutemate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun OnBoardingScreen(
    onGetStarted: () -> Unit ={}
) {
    val pages = listOf(
        OnBoardingPage(
            title = "Welcome to MuteMate",
            description = "Silence with a purpose. MuteMate is your personal sound assistant — quick, smart, and always in control.",
            icon = Icons.Default.Notifications,
            iconTint = MaterialTheme.colorScheme.primary
        ),
        OnBoardingPage(
            title = "Smart Mute Scheduling",
            description = "Mute your phone for a few minutes or hours — just the way you need it, whenever you need it.",
            icon = Icons.Default.VolumeOff,
            iconTint = Color(0xFF3186D1)
        ),
        OnBoardingPage(
            title = "Triple Volume Down = Instant Mute",
            description = "No app opening, no delay. Just press volume down 3 times — and you're instantly on silent.",
            icon = Icons.Default.Accessibility,
            iconTint = Color(0xFFFF9800)
        ),
        OnBoardingPage(
            title = "Custom Rules, Your Way",
            description = "Define mute rules that match your life — for meetings, focus time, or your daily routine.",
            icon = Icons.Default.Settings,
            iconTint = Color(0xFFE25441)
        ),
        OnBoardingPage(
            title = "Dynamic Theme Support",
            description = "Enjoy a theme that adapts to your device’s colors for a seamless, modern look.",
            icon = Icons.Default.Palette,
            iconTint = Color(0xFF9C27B0) // Purple for creativity and design
        ),
        OnBoardingPage(
            title = "Location-Based Muting (Coming Soon)",
            description = "Automatically mute your phone when you arrive at a saved place — like home, office, or college.",
            icon = Icons.Default.LocationOn,
            iconTint = Color(0xFF43A047)
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnBoardingPageView(page = pages[page])
        }
        Spacer(Modifier.height(16.dp))
        // Pager Indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pages.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(if (selected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Gray)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Get Started Button only on last page
        if (pagerState.currentPage == pages.lastIndex) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next", fontSize = 17.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

data class OnBoardingPage(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint: Color
)

@Composable
fun OnBoardingPageView(page: OnBoardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(
            imageVector = page.icon,
            contentDescription = page.title,
            modifier = Modifier.size(96.dp),
            tint = page.iconTint
        )
        Spacer(Modifier.height(36.dp))
        Text(page.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(
            page.description,
            fontSize = 17.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}
