package com.example.handhelp.ui.screens.volunteer

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.MissionCard
import com.example.handhelp.viewmodel.MissionUiState
import com.example.handhelp.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    missionViewModel: MissionViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val searchResults by missionViewModel.searchResults.collectAsState()
    val uiState by missionViewModel.uiState.collectAsState()

    // Recherche avec délai (debounce simple)
    LaunchedEffect(query) {
        if (query.length >= 2) {
            kotlinx.coroutines.delay(400)
            missionViewModel.search(query)
        } else if (query.isEmpty()) {
            missionViewModel.search("")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recherche") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Barre de recherche
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Titre, ville, catégorie...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            missionViewModel.search("")
                        }) {
                            Icon(Icons.Filled.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Résultats
            when {
                uiState is MissionUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                query.length < 2 -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Search, null,
                                Modifier.size(64.dp), tint = Color.LightGray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tapez au moins 2 caractères\npour rechercher",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                searchResults.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.SearchOff, null, Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(8.dp))
                            Text("Aucun résultat pour \"$query\"", color = Color.Gray)
                        }
                    }
                }
                else -> {
                    Text(
                        "${searchResults.size} résultat(s) pour \"$query\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                        color = Color.Gray
                    )
                    LazyColumn {
                        items(searchResults, key = { it.id }) { mission ->
                            MissionCard(
                                mission = mission,
                                onClick = { navController.navigate(NavRoutes.missionDetail(mission.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}