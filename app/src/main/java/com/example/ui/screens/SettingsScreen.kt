package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotificationEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright

@Composable
fun SettingsScreen(
    user: UserEntity,
    isOnline: Boolean,
    notifications: List<NotificationEntity>,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleNetwork: () -> Unit,
    onClearNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Title block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "SETTINGS TERMINAL",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "ACCESS ACCESSIBILITY & PREFERENCES",
                    color = OrangeFlameBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Action Preference Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preference_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "USER INTERFACE CONFIG",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    // 1. Dark Mode switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness6,
                                contentDescription = "Dark Mode",
                                tint = OrangeFlameBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("Force Dark Appearance", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Immersive high contrast theme", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = user.darkModeEnabled,
                            onCheckedChange = onToggleDarkMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrangeFlame
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    Divider(color = Color(0xFF26262B))

                    // 2. Notification channel Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Push Notifications",
                                tint = OrangeFlameBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("In-App Push Alerts", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Receive real-time updates", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = user.notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrangeFlame
                            ),
                            modifier = Modifier.testTag("push_notifications_switch")
                        )
                    }

                    Divider(color = Color(0xFF26262B))

                    // 3. Online/Offline network simulations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = "Online",
                                tint = if (isOnline) Color(0xFF00E676) else Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("Connect to live Cloud", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(if (isOnline) "Connected sync active" else "Running in offline database cache mode", color = if (isOnline) Color.Gray else Color.Red, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = onToggleNetwork,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOnline) Color(0xFF1E1E24) else OrangeFlame
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("sim_network_btn"),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(
                                if (isOnline) "GO OFFLINE" else "GO ONLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Session Node & Secure credentials info row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SECURE NODE DETAILS",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Connected Node:", color = Color.Gray, fontSize = 12.sp)
                        Text(user.email, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Session persistence:", color = Color.Gray, fontSize = 12.sp)
                        Text("Database Secured (Room)", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("logout_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out")
                            Text("TERMINATE SESSION (LOGOUT)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Push Notifications / Updates Log Center (Satisfies "push notifications for real-time updates")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notification Center",
                        tint = OrangeFlameBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "NOTIFICATION CENTER HISTORY",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "RECYCLE LOGS",
                    color = OrangeFlame,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clickable { onClearNotifications() }
                        .testTag("clear_notifications_btn")
                )
            }
        }

        // Notifications List mapping
        items(notifications) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.body,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
