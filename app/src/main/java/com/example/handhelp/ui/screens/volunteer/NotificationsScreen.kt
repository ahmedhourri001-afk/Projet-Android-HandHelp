package com.example.handhelp.ui.screens.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary

data class NotificationItem(val title: String, val body: String, val time: String, val icon: ImageVector, val color: Color, val read: Boolean = false)

val mockNotifications = listOf(
    NotificationItem("Mission confirmée ✅", "Votre participation à 'Distribution alimentaire' a été confirmée.", "Il y a 2h", Icons.Filled.CheckCircle, Primary),
    NotificationItem("Nouvelle mission 🌱", "Une nouvelle mission de reboisement est disponible près de chez vous.", "Il y a 5h", Icons.Filled.Notifications, Accent),
    NotificationItem("Rappel 📅", "La mission 'Soutien scolaire' commence demain à 14h00.", "Hier", Icons.Filled.CalendarToday, Color(0xFF2196F3)),
    NotificationItem("Badge obtenu 🏆", "Félicitations ! Vous avez obtenu le badge 'Éco-citoyen'.", "Il y a 2 jours", Icons.Filled.Star, Color(0xFFFF9800), read = true),
    NotificationItem("Message de l'organisateur", "Merci pour votre participation à notre événement !", "Il y a 3 jours", Icons.Filled.Message, Color(0xFF9C27B0), read = true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            itemsIndexed(mockNotifications) { _, notif ->
                ListItem(
                    headlineContent = { Text(notif.title, fontWeight = if (!notif.read) FontWeight.Bold else FontWeight.Normal) },
                    supportingContent = {
                        Column {
                            Text(notif.body, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(notif.time, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(notif.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(notif.icon, null, Modifier.size(22.dp), tint = notif.color)
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (!notif.read) notif.color.copy(0.04f) else Color.Transparent
                    )
                )
                Divider()
            }
        }
    }
}