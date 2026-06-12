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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.MissionUiState
import com.example.handhelp.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailScreen(
    navController: NavController,
    missionId: String,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    val mission by missionViewModel.selectedMission.collectAsState()
    val uiState by missionViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val isEnrolled = remember(mission, currentUser) {
        currentUser?.uid?.let { uid -> mission?.participants?.contains(uid) } ?: false
    }
    val isFull = remember(mission) {
        (mission?.volunteersEnrolled ?: 0) >= (mission?.volunteersNeeded ?: 1)
    }

    var showLeaveDialog by remember { mutableStateOf(false) }

    // Charger la mission au démarrage
    LaunchedEffect(missionId) {
        missionViewModel.loadMissionById(missionId)
    }

    // Dialogue confirmation désinscription
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Se désinscrire") },
            text = { Text("Voulez-vous vraiment vous désinscrire de cette mission ?") },
            confirmButton = {
                TextButton(onClick = {
                    currentUser?.uid?.let { uid ->
                        missionViewModel.leaveMission(missionId, uid)
                    }
                    showLeaveDialog = false
                }) { Text("Oui", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Snackbar erreur/succès
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is MissionUiState.Error) {
            snackbarHostState.showSnackbar((uiState as MissionUiState.Error).message)
            missionViewModel.resetUiState()
        }
        if (uiState is MissionUiState.Success) {
            missionViewModel.resetUiState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        when {
            uiState is MissionUiState.Loading && mission == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            mission == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Mission introuvable", color = Color.Gray)
                }
            }
            else -> {
                val m = mission!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.VolunteerActivism, null, Modifier.size(80.dp), tint = Primary)
                            Spacer(Modifier.height(8.dp))
                            SuggestionChip(onClick = {}, label = { Text(m.category) })
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        // Titre + organisateur
                        Text(
                            m.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Par ${m.organizerName}", color = Color.Gray)
                        Spacer(Modifier.height(20.dp))

                        // Infos
                        DetailInfoRow(Icons.Filled.CalendarToday, "${m.date} à ${m.time}")
                        Spacer(Modifier.height(10.dp))
                        DetailInfoRow(Icons.Filled.LocationOn, m.location)
                        Spacer(Modifier.height(10.dp))
                        DetailInfoRow(
                            Icons.Filled.Group,
                            "${m.volunteersEnrolled}/${m.volunteersNeeded} bénévoles inscrits"
                        )

                        Spacer(Modifier.height(20.dp))

                        // Barre de progression
                        Text(
                            "Places disponibles",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (m.volunteersNeeded > 0)
                                    m.volunteersEnrolled.toFloat() / m.volunteersNeeded
                                else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isFull) Accent else Primary,
                            trackColor = Primary.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isFull) "Mission complète !"
                            else "${m.volunteersNeeded - m.volunteersEnrolled} place(s) restante(s)",
                            color = if (isFull) Accent else Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))

                        // Description
                        Text(
                            "Description",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(m.description, style = MaterialTheme.typography.bodyMedium)

                        // Tags
                        if (m.tags.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Tags",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                m.tags.forEach { tag ->
                                    AssistChip(onClick = {}, label = { Text(tag) })
                                }
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        // Bouton inscription / désinscription
                        if (isEnrolled) {
                            OutlinedButton(
                                onClick = { showLeaveDialog = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.PersonRemove, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Se désinscrire")
                            }
                        } else {
                            HandHelpButton(
                                text = if (isFull) "Mission complète" else "Je participe !",
                                onClick = {
                                    currentUser?.uid?.let { uid ->
                                        missionViewModel.joinMission(missionId, uid)
                                    }
                                },
                                enabled = !isFull,
                                isLoading = uiState is MissionUiState.Loading,
                                containerColor = if (isFull) Color.Gray else Primary
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Primary.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = Primary)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}