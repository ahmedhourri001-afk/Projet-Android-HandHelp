package com.example.handhelp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.handhelp.ui.theme.Accent
import com.example.handhelp.ui.theme.Primary

// ─────────────────────────────────────────
// Données FAQ
// ─────────────────────────────────────────
data class FaqItem(val question: String, val answer: String)

val faqItems = listOf(
    FaqItem(
        "Comment s'inscrire à une mission ?",
        "Rendez-vous sur l'accueil, trouvez une mission qui vous intéresse, puis cliquez sur « Je participe ». Votre inscription est immédiate et confirmée par une notification."
    ),
    FaqItem(
        "Comment créer une mission en tant qu'organisateur ?",
        "Depuis votre tableau de bord organisateur, appuyez sur le bouton « + Nouvelle Mission » en bas à droite. Remplissez le formulaire avec le titre, la description, le lieu, la date et le nombre de bénévoles souhaités."
    ),
    FaqItem(
        "Comment me désinscrire d'une mission ?",
        "Ouvrez le détail de la mission concernée et appuyez sur « Se désinscrire ». Une confirmation vous sera demandée avant de valider."
    ),
    FaqItem(
        "Pourquoi je ne reçois pas de notifications ?",
        "Vérifiez que les notifications sont activées pour HandHelp dans les paramètres de votre téléphone. Sur Android 13+, l'application demande explicitement la permission au premier lancement."
    ),
    FaqItem(
        "Comment modifier mon profil ?",
        "Allez dans Mon Profil → Modifier le profil. Vous pouvez y changer votre nom, votre numéro de téléphone et votre bio."
    ),
    FaqItem(
        "Comment changer mon mot de passe ?",
        "Allez dans Mon Profil → Sécurité → Changer le mot de passe. Vous aurez besoin de votre mot de passe actuel pour confirmer."
    ),
    FaqItem(
        "Puis-je utiliser l'application gratuitement ?",
        "Oui, HandHelp est entièrement gratuit pour les bénévoles comme pour les organisateurs."
    ),
    FaqItem(
        "Comment contacter un organisateur ?",
        "Pour l'instant, la communication se fait via les notifications de mission. Une messagerie directe sera disponible dans une prochaine mise à jour."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(navController: NavController) {
    val context = LocalContext.current
    val supportEmail = "support@handhelp.ma"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aide & Support") },
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
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ─────────────────────────────────────────
            // Banner bienvenue
            // ─────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(0.09f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SupportAgent, null, Modifier.size(40.dp), tint = Primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Nous sommes là pour vous aider",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Consultez la FAQ ou contactez-nous directement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─────────────────────────────────────────
            // SECTION FAQ
            // ─────────────────────────────────────────
            Text(
                "Questions fréquentes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            faqItems.forEachIndexed { index, item ->
                FaqCard(item = item)
                if (index < faqItems.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            // ─────────────────────────────────────────
            // SECTION Contact
            // ─────────────────────────────────────────
            Text(
                "Contacter le support",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Bouton envoyer email
            ContactCard(
                icon = Icons.Filled.Email,
                title = "Envoyer un email",
                subtitle = supportEmail,
                color = Primary,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$supportEmail")
                        putExtra(Intent.EXTRA_SUBJECT, "Support HandHelp")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Bonjour,\n\nJ'ai besoin d'aide concernant :\n\n[Décrivez votre problème ici]\n\nCordialement"
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, "Envoyer un email"))
                }
            )

            Spacer(Modifier.height(12.dp))

            // Délai de réponse
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Accent.copy(0.07f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, Modifier.size(20.dp), tint = Accent)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Délai de réponse habituel : 24 à 48 heures ouvrées",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ─────────────────────────────────────────
            // Version de l'application
            // ─────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.VolunteerActivism, null,
                    Modifier.size(32.dp), tint = Primary.copy(0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "HandHelp",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────
// Composant : carte FAQ avec expansion
// ─────────────────────────────────────────
@Composable
private fun FaqCard(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(if (expanded) 3.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) Primary.copy(0.05f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Question + chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.HelpOutline, null,
                        Modifier.size(18.dp).padding(top = 2.dp),
                        tint = Primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.question,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (expanded) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = Primary
                )
            }

            // Réponse (animée)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Primary.copy(0.15f))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.75f),
                        lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// Composant : carte de contact
// ─────────────────────────────────────────
@Composable
private fun ContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(28.dp), tint = color)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Color.LightGray)
        }
    }
}