package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedBorder

@Composable
fun TopHeaderBar(
    userSession: UserSession,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onRoleChanged: (UserRole) -> Unit,
    showShieldLogo: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon (Hamburger or Shield)
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .testTag("header_menu_button")
                        .size(36.dp)
                ) {
                    if (showShieldLogo) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Thara Shield",
                            tint = TharaRed,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = TharaRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Center Title: THARA SERVICES
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "THARA SERVICES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = TharaRed
                    )
                }

                // Right Notification Bell Icon
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .testTag("header_notification_button")
                        .size(36.dp)
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = TharaRed,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = TharaRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Bottom subtle divider line tint
            HorizontalDivider(
                thickness = 1.dp,
                color = TharaRedBorder
            )
        }
    }
}
