package com.example.handhelp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.data.model.Notification
import com.example.handhelp.data.model.NotificationType
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Charger les notifications au démarrage
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { notificationViewModel.loadNotifications(it) }
    }

    // Dialogue suppression totale
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tout supprimer") },
            text = { Text("Supprimer toutes vos notifications ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    currentUser?.uid?.let { notificationViewModel.deleteAll(it) }
                    showDeleteAllDialog = false
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("Annuler") }
            }
        )
    }

    val unread = notifications.filter { !it.isRead }
    val read = notifications.filter { it.isRead }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications")
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = Primary) {
                                Text("$unreadCount", color = Color.White)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    // Tout marquer comme lu
                    if (unreadCount > 0) {
                        IconButton(onClick = {
                            currentUser?.uid?.let {
                                notificationViewModel.markAllAsRead(it)
                            }
                        }) {
                            Icon(Icons.Filled.DoneAll, "Tout lire", tint = Primary)
                        }
                    }
                    // Tout supprimer
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                "Tout supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            // État vide
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.NotificationsNone, null,
                        Modifier.size(80.dp), tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Aucune notification",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Vous serez notifié des mises à jour\nde vos missions ici.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Section non-lues
                if (unread.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Nouvelles (${unread.size})",
                            color = Primary
                        )
                    }
                    items(unread, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onRead = {
                                notificationViewModel.markAsRead(notification.id)
                            },
                            onDelete = {
                                notificationViewModel.deleteNotification(notification.id)
                            },
                            onMissionClick = {
                                if (notification.missionId.isNotBlank()) {
                                    notificationViewModel.markAsRead(notification.id)
                                    navController.navigate(
                                        NavRoutes.missionDetail(notification.missionId)
                                    )
                                }
                            }
                        )
                    }
                }

                // Section lues
                if (read.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Déjà lues (${read.size})",
                            color = Color.Gray
                        )
                    }
                    items(read, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onRead = {},
                            onDelete = {
                                notificationViewModel.deleteNotification(notification.id)
                            },
                            onMissionClick = {
                                if (notification.missionId.isNotBlank()) {
                                    navController.navigate(
                                        NavRoutes.missionDetail(notification.missionId)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// Composant : en-tête de section
// ─────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

// ─────────────────────────────────────────
// Composant : une notification avec swipe
// ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationItem(
    notification: Notification,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    onMissionClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Fond rouge swipe-to-delete
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Supprimer",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        NotificationCard(
            notification = notification,
            onClick = {
                if (!notification.isRead) onRead()
                if (notification.missionId.isNotBlank()) onMissionClick()
            }
        )
    }
}

// ─────────────────────────────────────────
// Composant : carte de notification
// ─────────────────────────────────────────
@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    val (icon, iconColor) = notificationIcon(notification.type)
    val timeText = remember(notification.createdAt) {
        formatRelativeTime(notification.createdAt)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (!notification.isRead) 3.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead)
                iconColor.copy(alpha = 0.06f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icône
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(24.dp), tint = iconColor)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (!notification.isRead) FontWeight.Bold
                            else FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    // Point non-lu
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule, null,
                        Modifier.size(12.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    // Lien vers mission si applicable
                    if (notification.missionId.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Voir la mission →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// Helper : icône selon le type
// ─────────────────────────────────────────
private fun notificationIcon(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.MISSION_JOINED ->
            Pair(Icons.Filled.PersonAdd, Color(0xFF2E7D32))
        NotificationType.MISSION_FULL ->
            Pair(Icons.Filled.CheckCircle, Color(0xFF4CAF50))
        NotificationType.MISSION_REMINDER ->
            Pair(Icons.Filled.CalendarToday, Color(0xFF2196F3))
        NotificationType.MISSION_COMPLETED ->
            Pair(Icons.Filled.EmojiEvents, Color(0xFFFF9800))
        NotificationType.NEW_MISSION ->
            Pair(Icons.Filled.Explore, Color(0xFF9C27B0))
        NotificationType.GENERAL ->
            Pair(Icons.Filled.Notifications, Color(0xFF607D8B))
    }
}

// ─────────────────────────────────────────
// Helper : formater le temps relatif
// ─────────────────────────────────────────
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "À l'instant"
        diff < 3_600_000 -> "Il y a ${diff / 60_000} min"
        diff < 86_400_000 -> "Il y a ${diff / 3_600_000}h"
        diff < 172_800_000 -> "Hier"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.FRENCH)
            sdf.format(Date(timestamp))
        }
    }
}