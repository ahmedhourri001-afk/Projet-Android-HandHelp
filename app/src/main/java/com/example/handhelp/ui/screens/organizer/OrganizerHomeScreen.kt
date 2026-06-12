package com.example.handhelp.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.data.model.Mission
import com.example.handhelp.data.model.MissionStatus
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val myMissions by missionViewModel.myMissions.collectAsState()
    var missionToDelete by remember { mutableStateOf<Mission?>(null) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { missionViewModel.loadOrganizerMissions(it) }
    }

    // Dialog suppression
    missionToDelete?.let { mission ->
        AlertDialog(
            onDismissRequest = { missionToDelete = null },
            title = { Text("Supprimer la mission") },
            text = { Text("Supprimer \"${mission.title}\" ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    missionViewModel.deleteMission(mission.id)
                    missionToDelete = null
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { missionToDelete = null }) { Text("Annuler") }
            }
        )
    }

    val active = myMissions.filter { it.status == MissionStatus.ACTIVE }
    val totalVolunteers = myMissions.sumOf { it.volunteersEnrolled }
    val fillRate = if (myMissions.isNotEmpty()) {
        val total = myMissions.sumOf { it.volunteersNeeded }
        if (total > 0) (myMissions.sumOf { it.volunteersEnrolled } * 100 / total) else 0
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Tableau de bord",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            currentUser?.displayName ?: "Organisateur",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) }) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(Icons.Filled.Notifications, null)
                        }
                    }
                    IconButton(onClick = { navController.navigate(NavRoutes.PROFILE) }) {
                        Icon(Icons.Filled.AccountCircle, null, tint = Primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(NavRoutes.ADD_MISSION) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nouvelle Mission") },
                containerColor = Primary
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true, onClick = {},
                    icon = { Icon(Icons.Filled.Home, null) },
                    label = { Text("Accueil") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) },
                    icon = { Icon(Icons.Filled.Notifications, null) },
                    label = { Text("Notifs") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.PROFILE) },
                    icon = { Icon(Icons.Filled.Person, null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Stats en temps réel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrgStatCard("${active.size}", "Actives", Modifier.weight(1f))
                    OrgStatCard("$totalVolunteers", "Bénévoles", Modifier.weight(1f))
                    OrgStatCard("$fillRate%", "Remplissage", Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mes missions (${myMissions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (myMissions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.AddCircleOutline, null,
                                Modifier.size(64.dp), tint = Color.LightGray
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Aucune mission créée\nAppuyez sur + pour commencer",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(myMissions, key = { it.id }) { mission ->
                    OrgMissionCard(
                        mission = mission,
                        onClick = { navController.navigate(NavRoutes.missionDetail(mission.id)) },
                        onDelete = { missionToDelete = mission },
                        onComplete = { missionViewModel.completeMission(mission.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun OrgMissionCard(
    mission: Mission,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mission.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "${mission.date} • ${mission.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Marquer terminée") },
                            leadingIcon = { Icon(Icons.Filled.CheckCircle, null, tint = Primary) },
                            onClick = { onComplete(); showMenu = false },
                            enabled = mission.status == MissionStatus.ACTIVE
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete, null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Barre progression
            LinearProgressIndicator(
                progress = {
                    if (mission.volunteersNeeded > 0)
                        mission.volunteersEnrolled.toFloat() / mission.volunteersNeeded
                    else 0f
                },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Primary,
                trackColor = Primary.copy(0.15f)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${mission.volunteersEnrolled}/${mission.volunteersNeeded} bénévoles",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                // Badge statut
                val (statusLabel, statusColor) = when (mission.status) {
                    MissionStatus.ACTIVE -> "Active" to Primary
                    MissionStatus.COMPLETED -> "Terminée" to Color(0xFF4CAF50)
                    MissionStatus.CANCELLED -> "Annulée" to Accent
                }
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun OrgStatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Primary.copy(0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Primary
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}