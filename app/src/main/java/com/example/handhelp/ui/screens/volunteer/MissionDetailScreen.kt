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
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailScreen(navController: NavController, missionId: String) {
    val mission = mockMissions.find { it.id == missionId } ?: mockMissions.first()
    var isEnrolled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail de la mission") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Header coloré
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.VolunteerActivism, null, Modifier.size(80.dp), tint = Primary)
                    Text(mission.category, style = MaterialTheme.typography.labelLarge, color = Primary)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(mission.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text("Par ${mission.organizerName}", color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                // Infos clés
                InfoRow(Icons.Filled.CalendarToday, "${mission.date} à ${mission.time}")
                Spacer(Modifier.height(8.dp))
                InfoRow(Icons.Filled.LocationOn, mission.location)
                Spacer(Modifier.height(8.dp))
                InfoRow(Icons.Filled.Group, "${mission.volunteersEnrolled}/${mission.volunteersNeeded} bénévoles inscrits")
                Spacer(Modifier.height(20.dp))

                // Barre progression
                Text("Places disponibles", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { mission.volunteersEnrolled.toFloat() / mission.volunteersNeeded },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Primary,
                    trackColor = Primary.copy(alpha = 0.2f)
                )
                Text(
                    "${mission.volunteersNeeded - mission.volunteersEnrolled} places restantes",
                    color = Accent,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(20.dp))

                Divider()
                Spacer(Modifier.height(16.dp))

                Text("Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                Text(mission.description + "\n\nCette mission est une opportunité unique de contribuer positivement à la société. Vous travaillerez en équipe avec d'autres bénévoles engagés dans un cadre bienveillant et organisé.", style = MaterialTheme.typography.bodyMedium, lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified)
                Spacer(Modifier.height(24.dp))

                HandHelpButton(
                    text = if (isEnrolled) "✓ Inscription confirmée" else "Je participe",
                    onClick = { isEnrolled = !isEnrolled },
                    containerColor = if (isEnrolled) Color(0xFF4CAF50) else Primary
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = Primary)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}