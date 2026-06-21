package com.example.handhelp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpTextField
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.viewmodel.AuthViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── États changer mot de passe ──
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPwError by remember { mutableStateOf("") }
    var newPwError by remember { mutableStateOf("") }
    var confirmPwError by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }

    // ── États désactiver compte ──
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var deactivatePassword by remember { mutableStateOf("") }
    var deactivateError by remember { mutableStateOf("") }
    var isDeactivating by remember { mutableStateOf(false) }

    // ── Dialogue désactivation ──
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeactivateDialog = false
                deactivatePassword = ""
                deactivateError = ""
            },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Désactiver le compte", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Cette action est irréversible. Votre compte et toutes vos données seront supprimés définitivement.",
                        color = Color.Gray
                    )
                    HandHelpTextField(
                        value = deactivatePassword,
                        onValueChange = { deactivatePassword = it; deactivateError = "" },
                        label = "Confirmez votre mot de passe",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        isError = deactivateError.isNotEmpty(),
                        errorMessage = deactivateError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deactivatePassword.isBlank()) {
                            deactivateError = "Mot de passe requis"
                            return@TextButton
                        }
                        scope.launch {
                            isDeactivating = true
                            try {
                                val user = FirebaseAuth.getInstance().currentUser
                                    ?: throw Exception("Utilisateur non connecté")
                                val email = user.email
                                    ?: throw Exception("Email introuvable")

                                // Ré-authentification obligatoire avant suppression
                                val credential = EmailAuthProvider.getCredential(
                                    email, deactivatePassword
                                )
                                user.reauthenticate(credential).await()
                                user.delete().await()

                                // Déconnexion et retour à l'accueil
                                authViewModel.logout()
                                navController.navigate(NavRoutes.WELCOME) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                val msg = when {
                                    e.message?.contains("password") == true ||
                                            e.message?.contains("credential") == true ->
                                        "Mot de passe incorrect"
                                    else -> e.localizedMessage ?: "Erreur de suppression"
                                }
                                deactivateError = msg
                            } finally {
                                isDeactivating = false
                            }
                        }
                    },
                    enabled = !isDeactivating
                ) {
                    if (isDeactivating) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Supprimer définitivement", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    deactivatePassword = ""
                    deactivateError = ""
                }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Sécurité") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ─────────────────────────────────────────
            // SECTION 1 — Compte connecté
            // ─────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(0.07f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AccountCircle, null, tint = Primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Compte connecté", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            currentUser?.email ?: "—",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // ─────────────────────────────────────────
            // SECTION 2 — Changer le mot de passe
            // ─────────────────────────────────────────
            Text(
                "Changer le mot de passe",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            HandHelpTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; currentPwError = "" },
                label = "Mot de passe actuel",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                isError = currentPwError.isNotEmpty(),
                errorMessage = currentPwError,
                modifier = Modifier.fillMaxWidth()
            )

            HandHelpTextField(
                value = newPassword,
                onValueChange = { newPassword = it; newPwError = "" },
                label = "Nouveau mot de passe",
                leadingIcon = Icons.Filled.LockOpen,
                isPassword = true,
                isError = newPwError.isNotEmpty(),
                errorMessage = newPwError,
                modifier = Modifier.fillMaxWidth()
            )

            HandHelpTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPwError = "" },
                label = "Confirmer le nouveau mot de passe",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                isError = confirmPwError.isNotEmpty(),
                errorMessage = confirmPwError,
                modifier = Modifier.fillMaxWidth()
            )

            // Indicateur de force du mot de passe
            if (newPassword.isNotEmpty()) {
                PasswordStrengthIndicator(password = newPassword)
            }

            HandHelpButton(
                text = "Mettre à jour le mot de passe",
                isLoading = isChangingPassword,
                onClick = {
                    // Validation
                    var valid = true
                    if (currentPassword.isBlank()) {
                        currentPwError = "Requis"; valid = false
                    }
                    if (newPassword.length < 6) {
                        newPwError = "Minimum 6 caractères"; valid = false
                    }
                    if (newPassword != confirmPassword) {
                        confirmPwError = "Les mots de passe ne correspondent pas"; valid = false
                    }
                    if (newPassword == currentPassword) {
                        newPwError = "Le nouveau mot de passe doit être différent"; valid = false
                    }
                    if (!valid) return@HandHelpButton

                    scope.launch {
                        isChangingPassword = true
                        try {
                            val user = FirebaseAuth.getInstance().currentUser
                                ?: throw Exception("Utilisateur non connecté")
                            val email = user.email
                                ?: throw Exception("Email introuvable")

                            // Ré-authentification avant changement
                            val credential = EmailAuthProvider.getCredential(email, currentPassword)
                            user.reauthenticate(credential).await()
                            user.updatePassword(newPassword).await()

                            // Reset les champs
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            snackbarHostState.showSnackbar("Mot de passe mis à jour ✅")
                        } catch (e: Exception) {
                            val msg = when {
                                e.message?.contains("password") == true ||
                                        e.message?.contains("credential") == true ->
                                    "Mot de passe actuel incorrect"
                                else -> e.localizedMessage ?: "Erreur"
                            }
                            currentPwError = msg
                        } finally {
                            isChangingPassword = false
                        }
                    }
                }
            )

            // ─────────────────────────────────────────
            // SECTION 3 — Zone danger
            // ─────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                "Zone de danger",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Désactiver le compte",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Supprime définitivement votre compte et toutes vos données.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { showDeactivateDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.DeleteForever, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Supprimer")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────
// Composant : indicateur force mot de passe
// ─────────────────────────────────────────
@Composable
private fun PasswordStrengthIndicator(password: String) {
    val strength = when {
        password.length < 6 -> 0
        password.length < 8 -> 1
        password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() } -> 3
        password.length >= 8 -> 2
        else -> 1
    }

    val (label, color) = when (strength) {
        0 -> "Trop court" to Color.Red
        1 -> "Faible" to Color(0xFFFF9800)
        2 -> "Moyen" to Color(0xFFFFC107)
        3 -> "Fort" to Color(0xFF4CAF50)
        else -> "Faible" to Color.Red
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { if (strength > i) 1f else 0f },
                        modifier = Modifier.fillMaxSize(),
                        color = color,
                        trackColor = Color.LightGray.copy(0.4f)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Force : $label",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}