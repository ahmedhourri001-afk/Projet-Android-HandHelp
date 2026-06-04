package com.example.handhelp.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpTextField
import com.example.handhelp.ui.screens.volunteer.categories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMissionScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var volunteersNeeded by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Social") }
    var expanded by remember { mutableStateOf(false) }

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
            Text("Informations de la mission", style = MaterialTheme.typography.titleMedium)

            HandHelpTextField(value = title, onValueChange = { title = it }, label = "Titre de la mission", leadingIcon = Icons.Filled.Title, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            HandHelpTextField(value = location, onValueChange = { location = it }, label = "Lieu", leadingIcon = Icons.Filled.LocationOn, modifier = Modifier.fillMaxWidth())
            HandHelpTextField(value = date, onValueChange = { date = it }, label = "Date (ex: 15 Jan 2025)", leadingIcon = Icons.Filled.CalendarToday, modifier = Modifier.fillMaxWidth())
            HandHelpTextField(value = volunteersNeeded, onValueChange = { volunteersNeeded = it }, label = "Nombre de bénévoles", leadingIcon = Icons.Filled.People, modifier = Modifier.fillMaxWidth())

            // Catégorie
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Catégorie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.drop(1).forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HandHelpButton(
                text = "Publier la mission",
                onClick = { navController.popBackStack() } // Logique Firestore à ajouter plus tard
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}