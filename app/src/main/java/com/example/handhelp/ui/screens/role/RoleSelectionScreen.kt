package com.example.handhelp.ui.screens.role

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.data.model.UserRole
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthState
import com.example.handhelp.viewmodel.AuthViewModel

@Composable
fun RoleSelectionScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val dest = if (authViewModel.currentUser.value?.role == UserRole.ORGANIZER)
                NavRoutes.ORGANIZER_HOME else NavRoutes.VOLUNTEER_HOME
            navController.navigate(dest) { popUpTo(NavRoutes.ROLE_SELECTION) { inclusive = true } }
        }
    }

    val uid = (authState as? AuthState.NeedsRoleSelection)?.uid ?: ""
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.VolunteerActivism, null, Modifier.size(72.dp), tint = Primary)
        Spacer(Modifier.height(16.dp))
        Text("Quel est votre rôle ?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Text("Choisissez votre profil pour personnaliser votre expérience", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(Modifier.height(40.dp))

        RoleCard(
            title = "Bénévole",
            description = "Participez à des missions, aidez votre communauté et faites la différence.",
            icon = Icons.Filled.Favorite,
            color = Primary,
            selected = selectedRole == UserRole.VOLUNTEER,
            onClick = { selectedRole = UserRole.VOLUNTEER }
        )
        Spacer(Modifier.height(16.dp))
        RoleCard(
            title = "Organisateur",
            description = "Créez des missions, gérez des bénévoles et organisez des événements.",
            icon = Icons.Filled.Business,
            color = Accent,
            selected = selectedRole == UserRole.ORGANIZER,
            onClick = { selectedRole = UserRole.ORGANIZER }
        )
        Spacer(Modifier.height(40.dp))

        HandHelpButton(
            text = "Continuer",
            onClick = {
                selectedRole?.let { role ->
                    authViewModel.selectRole(
                        uid = uid.ifBlank { firebaseUser?.uid ?: "" },
                        role = role,
                        displayName = firebaseUser?.displayName ?: "",
                        email = firebaseUser?.email ?: ""
                    )
                }
            },
            enabled = selectedRole != null,
            isLoading = isLoading
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = if (selected) BorderStroke(2.dp, color) else BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(48.dp), tint = color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}