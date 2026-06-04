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
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.MissionCard
import com.example.handhelp.ui.screens.volunteer.mockMissions
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerHomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tableau de bord", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Organisateur", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Accueil") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) }, icon = { Icon(Icons.Filled.Notifications, null) }, label = { Text("Notifs") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(NavRoutes.PROFILE) }, icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Profil") })
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                // Stats organisateur
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrgStatCard("4", "Missions actives", Modifier.weight(1f))
                    OrgStatCard("31", "Bénévoles inscrits", Modifier.weight(1f))
                    OrgStatCard("96%", "Taux remplissage", Modifier.weight(1f))
                }
            }
            item {
                Text("Mes missions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            }
            items(mockMissions) { mission ->
                MissionCard(mission = mission, onClick = { navController.navigate(NavRoutes.missionDetail(mission.id)) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun OrgStatCard(value: String, label: String, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Primary.copy(0.08f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}