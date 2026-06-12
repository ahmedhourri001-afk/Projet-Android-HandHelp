package com.example.handhelp.ui.screens.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.MissionCard
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.MissionUiState
import com.example.handhelp.viewmodel.MissionViewModel

val categories = listOf("Tous", "Social", "Environnement", "Éducation", "Santé", "Sport")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val missions by missionViewModel.missions.collectAsState()
    val selectedCategory by missionViewModel.selectedCategory.collectAsState()
    val uiState by missionViewModel.uiState.collectAsState()
    val participatedMissions by missionViewModel.participatedMissions.collectAsState()

    // Charger les missions au lancement
    LaunchedEffect(Unit) {
        missionViewModel.loadActiveMissions()
        currentUser?.uid?.let { missionViewModel.loadParticipatedMissions(it) }
    }

    // Filtrer par catégorie
    val filteredMissions = if (selectedCategory == "Tous") missions
    else missions.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Bonjour, ${currentUser?.displayName?.split(" ")?.first() ?: "Bénévole"} 👋",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Trouvez votre prochaine mission",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) }) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Filled.Notifications, null)
                        }
                    }
                    IconButton(onClick = { navController.navigate(NavRoutes.PROFILE) }) {
                        Icon(Icons.Filled.AccountCircle, null, tint = Primary)
                    }
                }
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
                    onClick = { navController.navigate(NavRoutes.SEARCH) },
                    icon = { Icon(Icons.Filled.Search, null) },
                    label = { Text("Recherche") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.HISTORY) },
                    icon = { Icon(Icons.Filled.History, null) },
                    label = { Text("Historique") }
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

            // Stats bénévole
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = "${participatedMissions.size}",
                        label = "Missions\nréalisées",
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = "${missions.size}",
                        label = "Disponibles",
                        icon = Icons.Filled.Explore,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = categories.size.toString(),
                        label = "Catégories",
                        icon = Icons.Filled.Category,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filtres catégories
            item {
                Text(
                    "Catégories",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { missionViewModel.selectCategory(cat) },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Titre liste + compteur
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Missions disponibles",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "${filteredMissions.size} mission(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // État chargement
            when (uiState) {
                is MissionUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }
                is MissionUiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                (uiState as MissionUiState.Error).message,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                else -> {
                    if (filteredMissions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.SearchOff, null,
                                        Modifier.size(64.dp), tint = Color.LightGray
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Aucune mission disponible\ndans cette catégorie",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredMissions) { mission ->
                            MissionCard(
                                mission = mission,
                                onClick = {
                                    navController.navigate(NavRoutes.missionDetail(mission.id))
                                }
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
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Primary.copy(0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = Primary)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Primary
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}