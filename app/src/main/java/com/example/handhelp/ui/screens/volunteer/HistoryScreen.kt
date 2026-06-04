package com.example.handhelp.ui.screens.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
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
            item {
                // Résumé global
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(0.08f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("12" to "Missions", "48h" to "Total", "2025" to "Année").forEach { (v, l) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(v, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
                                Text(l, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
            item {
                Text("Missions récentes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            }
            items(mockMissions) { mission ->
                ListItem(
                    headlineContent = { Text(mission.title, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("${mission.date} • ${mission.location}") },
                    leadingContent = {
                        Icon(Icons.Filled.CheckCircle, null, Modifier.size(32.dp), tint = Primary)
                    },
                    trailingContent = {
                        SuggestionChip(onClick = {}, label = { Text("Terminée", style = MaterialTheme.typography.labelSmall) })
                    }
                )
                Divider()
            }
        }
    }
}