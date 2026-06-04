package com.example.handhelp.ui.screens.auth

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpTextField
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthState
import com.example.handhelp.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    val isLoading = authState is AuthState.Loading
    val isSuccess = authState is AuthState.PasswordResetSent

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
        }
        Spacer(Modifier.height(24.dp))
        Icon(Icons.Filled.LockReset, null, Modifier.size(72.dp), tint = Primary)
        Spacer(Modifier.height(16.dp))
        Text("Mot de passe oublié", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        Text(
            "Entrez votre adresse email et nous vous enverrons un lien de réinitialisation.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(Modifier.height(32.dp))

        if (isSuccess) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Email envoyé ! Vérifiez votre boîte de réception.", color = Primary)
                }
            }
            Spacer(Modifier.height(24.dp))
            HandHelpButton("Retour à la connexion", onClick = {
                authViewModel.resetState()
                navController.navigate(NavRoutes.LOGIN) { popUpTo(NavRoutes.FORGOT_PASSWORD) { inclusive = true } }
            })
        } else {
            if (authState is AuthState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((authState as AuthState.Error).message, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(Modifier.height(12.dp))
            }
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
            Spacer(Modifier.height(24.dp))
            HandHelpButton(
                text = "Envoyer le lien",
                onClick = {
                    if (email.isBlank()) { emailError = "Email requis" }
                    else authViewModel.sendPasswordReset(email)
                },
                isLoading = isLoading
            )
        }
    }
}