package com.example.handhelp.ui.screens.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.ui.theme.PrimaryVariant
import com.example.handhelp.viewmodel.AuthViewModel
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion") },
            text = { Text("Voulez-vous vraiment vous déconnecter ?") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.WELCOME) { popUpTo(0) { inclusive = true } }
                }) { Text("Oui", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.Logout, "Déconnexion", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header profil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryVariant)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White.copy(0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, Modifier.size(54.dp), tint = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(currentUser?.displayName ?: "Utilisateur", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(currentUser?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.85f))
                    Spacer(Modifier.height(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(if (currentUser?.role?.name == "ORGANIZER") "Organisateur" else "Bénévole", color = Primary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White)
                    )
                }
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("12" to "Missions", "48h" to "Heures", "8" to "Badges").forEach { (v, l) ->
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(v, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Primary)
                            Text(l, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // Paramètres
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
                Column {
                    listOf(
                        Triple(Icons.Filled.Edit, "Modifier le profil", {}),
                        Triple(Icons.Filled.Notifications, "Notifications", {}),
                        Triple(Icons.Filled.Security, "Sécurité", {}),
                        Triple(Icons.Filled.Help, "Aide & Support", {})
                    ).forEachIndexed { i, (icon, title, action) ->
                        ListItem(
                            headlineContent = { Text(title) },
                            leadingContent = { Icon(icon, null, tint = Primary) },
                            trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
                            modifier = Modifier.clickableWithRipple { action() }
                        )
                        if (i < 3) Divider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// Extension helper
fun Modifier.clickableWithRipple(onClick: () -> Unit) = this.then(
    Modifier
)