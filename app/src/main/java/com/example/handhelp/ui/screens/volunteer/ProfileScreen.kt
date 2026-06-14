package com.example.handhelp.ui.screens.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.data.model.UserRole
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.InitialsAvatar
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.ui.theme.PrimaryVariant
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val participatedMissions by missionViewModel.participatedMissions.collectAsState()
    val myMissions by missionViewModel.myMissions.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isOrganizer = currentUser?.role == UserRole.ORGANIZER

    // Charger les données selon le rôle
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            if (isOrganizer) missionViewModel.loadOrganizerMissions(uid)
            else missionViewModel.loadParticipatedMissions(uid)
        }
    }

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
            // ── Header profil avec avatar initiale ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryVariant)))
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar = initiale du nom, fond blanc semi-transparent
                    InitialsAvatar(
                        name = currentUser?.displayName ?: "?",
                        size = 90.dp,
                        textColor = Color.White,
                        modifier = Modifier.background(
                            Color.White.copy(alpha = 0.0f) // transparent, la couleur vient de InitialsAvatar
                        )
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        currentUser?.displayName?.ifBlank { "Utilisateur" } ?: "Utilisateur",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        currentUser?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.85f)
                    )

                    // Bio (si renseignée)
                    if (currentUser?.bio?.isNotBlank() == true) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            currentUser?.bio ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                if (isOrganizer) "Organisateur" else "Bénévole",
                                color = Primary
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White)
                    )
                }
            }

            // ── Stats dynamiques ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOrganizer) {
                    val totalVolunteers = myMissions.sumOf { it.volunteersEnrolled }
                    StatBox("${myMissions.size}", "Missions créées", Modifier.weight(1f))
                    StatBox("$totalVolunteers", "Bénévoles", Modifier.weight(1f))
                    StatBox("${myMissions.count { it.status.name == "COMPLETED" }}", "Terminées", Modifier.weight(1f))
                } else {
                    val completed = participatedMissions.count { it.status.name == "COMPLETED" }
                    StatBox("${participatedMissions.size}", "Missions", Modifier.weight(1f))
                    StatBox("$completed", "Terminées", Modifier.weight(1f))
                    StatBox(if (currentUser?.phone?.isNotBlank() == true) "✓" else "—", "Téléphone", Modifier.weight(1f))
                }
            }

            // ── Téléphone (si renseigné) ──
            if (currentUser?.phone?.isNotBlank() == true) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(currentUser?.phone ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Menu paramètres ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Filled.Edit,
                        title = "Modifier le profil",
                        onClick = { navController.navigate(NavRoutes.EDIT_PROFILE) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    ProfileMenuItem(
                        icon = Icons.Filled.Notifications,
                        title = "Notifications",
                        onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    ProfileMenuItem(
                        icon = Icons.Filled.Security,
                        title = "Sécurité",
                        onClick = { }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    ProfileMenuItem(
                        icon = Icons.Filled.Help,
                        title = "Aide & Support",
                        onClick = { }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Primary.copy(0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, null, tint = Primary) },
        trailingContent = { Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray) },
        modifier = Modifier.clickable { onClick() }
    )
}