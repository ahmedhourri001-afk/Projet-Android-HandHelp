package com.example.handhelp.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpTextField
import com.example.handhelp.ui.screens.volunteer.categories
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.MissionUiState
import com.example.handhelp.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMissionScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by missionViewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var volunteersNeeded by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Social") }
    var expanded by remember { mutableStateOf(false) }

    // Erreurs
    var titleError by remember { mutableStateOf("") }
    var descError by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }
    var volunteersError by remember { mutableStateOf("") }

    val isLoading = uiState is MissionUiState.Loading

    // Navigation après succès
    LaunchedEffect(uiState) {
        if (uiState is MissionUiState.Success) {
            missionViewModel.resetUiState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer une Mission") },
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Erreur globale
            if (uiState is MissionUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        (uiState as MissionUiState.Error).message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Text(
                "Informations générales",
                style = MaterialTheme.typography.titleMedium
            )

            HandHelpTextField(
                value = title,
                onValueChange = { title = it; titleError = "" },
                label = "Titre de la mission *",
                leadingIcon = Icons.Filled.Title,
                isError = titleError.isNotEmpty(),
                errorMessage = titleError,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descError = "" },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth().height(130.dp),
                maxLines = 5,
                isError = descError.isNotEmpty(),
                supportingText = if (descError.isNotEmpty()) {
                    { Text(descError, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            // Catégorie
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Catégorie") },
                    leadingIcon = { Icon(Icons.Filled.Category, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.drop(1).forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { selectedCategory = cat; expanded = false }
                        )
                    }
                }
            }

            HorizontalDivider()
            Text("Lieu & Date", style = MaterialTheme.typography.titleMedium)

            HandHelpTextField(
                value = location,
                onValueChange = { location = it; locationError = "" },
                label = "Ville / Lieu *",
                leadingIcon = Icons.Filled.LocationOn,
                isError = locationError.isNotEmpty(),
                errorMessage = locationError,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HandHelpTextField(
                    value = date,
                    onValueChange = { date = it; dateError = "" },
                    label = "Date *",
                    leadingIcon = Icons.Filled.CalendarToday,
                    isError = dateError.isNotEmpty(),
                    errorMessage = dateError,
                    modifier = Modifier.weight(1f)
                )
                HandHelpTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = "Heure",
                    leadingIcon = Icons.Filled.Schedule,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()
            Text("Bénévoles", style = MaterialTheme.typography.titleMedium)

            HandHelpTextField(
                value = volunteersNeeded,
                onValueChange = { volunteersNeeded = it; volunteersError = "" },
                label = "Nombre de bénévoles *",
                leadingIcon = Icons.Filled.People,
                isError = volunteersError.isNotEmpty(),
                errorMessage = volunteersError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            HandHelpButton(
                text = "Publier la mission",
                onClick = {
                    // Validation
                    var valid = true
                    if (title.isBlank()) { titleError = "Titre requis"; valid = false }
                    if (description.isBlank()) { descError = "Description requise"; valid = false }
                    if (location.isBlank()) { locationError = "Lieu requis"; valid = false }
                    if (date.isBlank()) { dateError = "Date requise"; valid = false }
                    val nbVolunteers = volunteersNeeded.toIntOrNull()
                    if (nbVolunteers == null || nbVolunteers <= 0) {
                        volunteersError = "Nombre valide requis"; valid = false
                    }
                    if (valid) {
                        missionViewModel.createMission(
                            title = title,
                            description = description,
                            category = selectedCategory,
                            location = location,
                            date = date,
                            time = time.ifBlank { "09:00" },
                            volunteersNeeded = nbVolunteers!!,
                            organizerId = currentUser?.uid ?: "",
                            organizerName = currentUser?.displayName ?: "Organisateur"
                        )
                    }
                },
                isLoading = isLoading
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}