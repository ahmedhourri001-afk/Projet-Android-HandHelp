package com.example.handhelp.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val isLoading = authState is AuthState.Loading

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { authViewModel.handleGoogleSignInResult(it) }
            } catch (e: ApiException) { /* Gérer l'erreur */ }
        }
    }

    // Navigation selon l'état
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                val role = currentUser?.role
                val dest = if (role?.name == "ORGANIZER") NavRoutes.ORGANIZER_HOME else NavRoutes.VOLUNTEER_HOME
                navController.navigate(dest) { popUpTo(NavRoutes.LOGIN) { inclusive = true } }
            }
            is AuthState.NeedsRoleSelection -> {
                navController.navigate(NavRoutes.ROLE_SELECTION) { popUpTo(NavRoutes.LOGIN) { inclusive = true } }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // En-tête
        Icon(Icons.Filled.VolunteerActivism, null, Modifier.size(64.dp), tint = Primary)
        Spacer(Modifier.height(8.dp))
        Text("Connexion", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Text("Bon retour parmi nous !", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        // Erreur globale
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

        // Champs
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
        Spacer(Modifier.height(8.dp))

        // Mot de passe oublié
        TextButton(
            onClick = { navController.navigate(NavRoutes.FORGOT_PASSWORD) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Mot de passe oublié ?", color = Primary)
        }

        Spacer(Modifier.height(16.dp))

        // Bouton connexion
        HandHelpButton(
            text = "Se connecter",
            onClick = {
                var valid = true
                if (email.isBlank()) { emailError = "Email requis"; valid = false }
                if (password.isBlank()) { passwordError = "Mot de passe requis"; valid = false }
                if (valid) authViewModel.login(email, password)
            },
            isLoading = isLoading
        )

        Spacer(Modifier.height(20.dp))

        // Séparateur
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Divider(Modifier.weight(1f))
            Text("  ou  ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Divider(Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        // Google Sign-In
        OutlinedButton(
            onClick = {
                val client = authViewModel.getGoogleSignInClient(context as Activity)
                googleSignInLauncher.launch(client.signInIntent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.Search, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continuer avec Google")
        }

        Spacer(Modifier.height(24.dp))

        // Lien vers inscription
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pas encore de compte ?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = {
                authViewModel.resetState()
                navController.navigate(NavRoutes.REGISTER)
            }) {
                Text("S'inscrire", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}