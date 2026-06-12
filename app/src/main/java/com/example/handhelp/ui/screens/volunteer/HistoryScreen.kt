package com.example.handhelp.ui.screens.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.handhelp.data.model.MissionStatus
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.MissionViewModel
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val participatedMissions by missionViewModel.participatedMissions.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { missionViewModel.loadParticipatedMissions(it) }
    }

    val completed = participatedMissions.filter { it.status == MissionStatus.COMPLETED }
    val active = participatedMissions.filter { it.status == MissionStatus.ACTIVE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Historique") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Résumé
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(0.08f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HistoryStat("${participatedMissions.size}", "Total")
                        HistoryStat("${active.size}", "En cours")
                        HistoryStat("${completed.size}", "Terminées")
                    }
                }
            }

            // Missions en cours
            if (active.isNotEmpty()) {
                item {
                    Text(
                        "Missions en cours (${active.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 8.dp)
                    )
                }
                items(active, key = { it.id }) { mission ->
                    ListItem(
                        headlineContent = {
                            Text(mission.title, fontWeight = FontWeight.SemiBold)
                        },
                        supportingContent = {
                            Text("${mission.date} • ${mission.location}")
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.PlayArrow, null,
                                Modifier.size(32.dp),
                                tint = Primary
                            )
                        },
                        trailingContent = {
                            Icon(
                                Icons.Filled.ChevronRight, null,
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate(NavRoutes.missionDetail(mission.id))
                        }
                    )
                    HorizontalDivider()
                }
            }

            // Missions terminées
            if (completed.isNotEmpty()) {
                item {
                    Text(
                        "Missions terminées (${completed.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, top = 16.dp)
                    )
                }
                items(completed, key = { it.id }) { mission ->
                    ListItem(
                        headlineContent = {
                            Text(
                                mission.title,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        },
                        supportingContent = {
                            Text("${mission.date} • ${mission.location}")
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.CheckCircle, null,
                                Modifier.size(32.dp),
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        trailingContent = {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        "Terminée",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }

            // État vide
            if (participatedMissions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.History, null,
                                Modifier.size(64.dp), tint = Color.LightGray
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Vous n'avez participé\nà aucune mission pour l'instant",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HistoryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Primary
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}