package com.example.handhelp.ui.screens.auth

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpTextField
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthState
import com.example.handhelp.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmError by remember { mutableStateOf("") }

    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.NeedsRoleSelection) {
            navController.navigate(NavRoutes.ROLE_SELECTION) {
                popUpTo(NavRoutes.REGISTER) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Filled.PersonAdd, null, Modifier.size(60.dp), tint = Primary)
        Spacer(Modifier.height(8.dp))
        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Text("Rejoignez HandHelp dès maintenant", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(28.dp))

        if (authState is AuthState.Error) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    (authState as AuthState.Error).message,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        HandHelpTextField(
            value = displayName,
            onValueChange = { displayName = it; nameError = "" },
            label = "Nom complet",
            leadingIcon = Icons.Filled.Person,
            isError = nameError.isNotEmpty(),
            errorMessage = nameError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        HandHelpTextField(
            value = email,
            onValueChange = { email = it; emailError = "" },
            label = "Adresse email",
            leadingIcon = Icons.Filled.Email,
            isError = emailError.isNotEmpty(),
            errorMessage = emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        HandHelpTextField(
            value = password,
            onValueChange = { password = it; passwordError = "" },
            label = "Mot de passe",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            isError = passwordError.isNotEmpty(),
            errorMessage = passwordError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        HandHelpTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = "" },
            label = "Confirmer le mot de passe",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            isError = confirmError.isNotEmpty(),
            errorMessage = confirmError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        HandHelpButton(
            text = "Créer mon compte",
            onClick = {
                var valid = true
                if (displayName.isBlank()) { nameError = "Nom requis"; valid = false }
                if (email.isBlank()) { emailError = "Email requis"; valid = false }
                if (password.length < 6) { passwordError = "Minimum 6 caractères"; valid = false }
                if (password != confirmPassword) { confirmError = "Les mots de passe ne correspondent pas"; valid = false }
                if (valid) authViewModel.register(email, password, displayName)
            },
            isLoading = isLoading
        )

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Déjà un compte ?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = {
                authViewModel.resetState()
                navController.navigate(NavRoutes.LOGIN)
            }) {
                Text("Se connecter", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}