package com.example.handhelp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpTextField
import com.example.handhelp.ui.components.InitialsAvatar
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.ProfileUiState
import com.example.handhelp.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by profileViewModel.uiState.collectAsState()

    var displayName by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var phone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var bio by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
    var nameError by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = uiState is ProfileUiState.Loading

    // Gérer les résultats UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                authViewModel.refreshUserData()
                snackbarHostState.showSnackbar("Profil mis à jour ✅")
                profileViewModel.resetState()
            }
            is ProfileUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ProfileUiState.Error).message)
                profileViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil") },
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Avatar (initiale, lecture seule) ──
            InitialsAvatar(
                name = displayName.ifBlank { currentUser?.displayName ?: "?" },
                size = 96.dp
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Votre avatar utilise la première lettre\nde votre nom",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ── Formulaire ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Informations personnelles",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                HandHelpTextField(
                    value = displayName,
                    onValueChange = { displayName = it; nameError = "" },
                    label = "Nom complet",
                    leadingIcon = Icons.Filled.Person,
                    isError = nameError.isNotEmpty(),
                    errorMessage = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Email en lecture seule
                HandHelpTextField(
                    value = currentUser?.email ?: "",
                    onValueChange = {},
                    label = "Adresse email",
                    leadingIcon = Icons.Filled.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                HandHelpTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Téléphone",
                    leadingIcon = Icons.Filled.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 150) bio = it },
                    label = { Text("Bio") },
                    placeholder = { Text("Parlez un peu de vous...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 4,
                    supportingText = { Text("${bio.length}/150") }
                )

                // Rôle (lecture seule)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(0.06f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (currentUser?.role?.name == "ORGANIZER") Icons.Filled.Business
                            else Icons.Filled.VolunteerActivism,
                            null, tint = Primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Rôle", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                if (currentUser?.role?.name == "ORGANIZER") "Organisateur" else "Bénévole",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                HandHelpButton(
                    text = "Enregistrer les modifications",
                    onClick = {
                        if (displayName.isBlank()) {
                            nameError = "Le nom est requis"
                        } else {
                            currentUser?.uid?.let { uid ->
                                profileViewModel.updateProfile(uid, displayName, phone, bio)
                            }
                        }
                    },
                    isLoading = isLoading
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}