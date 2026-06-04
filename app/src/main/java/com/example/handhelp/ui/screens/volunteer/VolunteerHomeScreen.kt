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
import androidx.navigation.NavController
import com.example.handhelp.data.model.Mission
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.MissionCard
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel

// Données mockées
val mockMissions = listOf(
    Mission("1", "Distribution alimentaire", "Aide aux familles dans le besoin dans le quartier de Hay Hassani.", "Social", "Casablanca", "15 Jan 2025", "09:00", 10, 7, "", "ONG Espoir"),
    Mission("2", "Plantation d'arbres", "Reboisement de la forêt de Maâmora avec l'association Verte Vie.", "Environnement", "Rabat", "20 Jan 2025", "08:00", 20, 12, "", "Verte Vie"),
    Mission("3", "Soutien scolaire", "Aide aux devoirs pour enfants défavorisés à Fès.", "Éducation", "Fès", "18 Jan 2025", "14:00", 5, 3, "", "Éclaire"),
    Mission("4", "Visite aux personnes âgées", "Accompagnement et divertissement en maison de retraite.", "Santé", "Marrakech", "22 Jan 2025", "10:00", 8, 8, "", "Solidarité Senior"),
)

val categories = listOf("Tous", "Social", "Environnement", "Éducation", "Santé", "Sport")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerHomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var selectedCategory by remember { mutableStateOf("Tous") }

    val filteredMissions = if (selectedCategory == "Tous") mockMissions
    else mockMissions.filter { it.category == selectedCategory }

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
                // ✅ Utilisation directe de NavigationBarItem de Material3
                // sans fonction privée qui crée le conflit de noms
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Filled.Home, "Accueil") },
                    label = { Text("Accueil") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.SEARCH) },
                    icon = { Icon(Icons.Filled.Search, "Recherche") },
                    label = { Text("Recherche") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.HISTORY) },
                    icon = { Icon(Icons.Filled.History, "Historique") },
                    label = { Text("Historique") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.PROFILE) },
                    icon = { Icon(Icons.Filled.Person, "Profil") },
                    label = { Text("Profil") }
                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("12", "Missions\nréalisées", Icons.Filled.CheckCircle, Modifier.weight(1f))
                    StatCard("48h", "Heures\nbénévoles", Icons.Filled.Timer, Modifier.weight(1f))
                    StatCard("5★", "Score\ncommunauté", Icons.Filled.Star, Modifier.weight(1f))
                }
            }
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
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            item {
                Text(
                    "Missions disponibles",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )
            }
            items(filteredMissions) { mission ->
                MissionCard(
                    mission = mission,
                    onClick = { navController.navigate(NavRoutes.missionDetail(mission.id)) }
                )
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