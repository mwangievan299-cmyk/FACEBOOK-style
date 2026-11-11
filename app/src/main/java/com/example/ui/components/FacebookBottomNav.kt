package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.FacebookRed

enum class FacebookTab {
    FEED, WATCH, FRIENDS, NOTIFICATIONS, PROFILE
}

@Composable
fun FacebookBottomNav(
    selectedTab: FacebookTab,
    onTabSelected: (FacebookTab) -> Unit,
    userAvatarUrl: String,
    unreadNotificationsCount: Int = 2,
    pendingFriendRequestsCount: Int = 2,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 2.dp
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home Feed
                NavTabItem(
                    selected = selectedTab == FacebookTab.FEED,
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    label = "Home",
                    onClick = { onTabSelected(FacebookTab.FEED) },
                    testTag = "nav_tab_feed"
                )

                // Tab 2: Video / Watch / Reels
                NavTabItem(
                    selected = selectedTab == FacebookTab.WATCH,
                    selectedIcon = Icons.Filled.OndemandVideo,
                    unselectedIcon = Icons.Outlined.OndemandVideo,
                    label = "Watch",
                    badge = "NEW",
                    onClick = { onTabSelected(FacebookTab.WATCH) },
                    testTag = "nav_tab_watch"
                )

                // Tab 3: Friends
                NavTabItem(
                    selected = selectedTab == FacebookTab.FRIENDS,
                    selectedIcon = Icons.Filled.People,
                    unselectedIcon = Icons.Outlined.People,
                    label = "Friends",
                    badgeCount = pendingFriendRequestsCount,
                    onClick = { onTabSelected(FacebookTab.FRIENDS) },
                    testTag = "nav_tab_friends"
                )

                // Tab 4: Notifications
                NavTabItem(
                    selected = selectedTab == FacebookTab.NOTIFICATIONS,
                    selectedIcon = Icons.Filled.Notifications,
                    unselectedIcon = Icons.Outlined.Notifications,
                    label = "Notifications",
                    badgeCount = unreadNotificationsCount,
                    onClick = { onTabSelected(FacebookTab.NOTIFICATIONS) },
                    testTag = "nav_tab_notifications"
                )

                // Tab 5: Profile / Menu
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(FacebookTab.PROFILE) }
                        .testTag("nav_tab_profile"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val isSelected = selectedTab == FacebookTab.PROFILE
                        AsyncImage(
                            model = userAvatarUrl,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) FacebookBlue else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Menu",
                            fontSize = 11.sp,
                            color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavTabItem(
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    badgeCount: Int = 0,
    badge: String? = null
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = if (selected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint = if (selected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(16.dp)
                            .background(FacebookRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                } else if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .background(FacebookRed, CircleShape)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
