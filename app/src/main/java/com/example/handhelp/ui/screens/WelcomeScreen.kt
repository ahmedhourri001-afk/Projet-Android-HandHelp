package com.example.handhelp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handhelp.navigation.NavRoutes
import com.example.handhelp.ui.components.HandHelpButton
import com.example.handhelp.ui.components.HandHelpOutlinedButton
import com.example.handhelp.ui.theme.Primary
import com.example.handhelp.ui.theme.PrimaryVariant

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header avec dégradé
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .background(
                    Brush.verticalGradient(colors = listOf(Primary, PrimaryVariant))
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.VolunteerActivism,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = Color.White
                )
                Spacer(Modifier.height(16.dp))
                Text("HandHelp", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Bénévolat & Solidarité", fontSize = 16.sp, color = Color.White.copy(0.85f))
            }
        }

        // Section boutons
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Rejoignez notre communauté\nde bénévoles",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Trouvez des missions, participez à des événements et faites la différence.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
            Spacer(Modifier.height(32.dp))
            HandHelpButton(text = "Commencer", onClick = { navController.navigate(NavRoutes.REGISTER) })
            Spacer(Modifier.height(12.dp))
            HandHelpOutlinedButton(text = "J'ai déjà un compte", onClick = { navController.navigate(NavRoutes.LOGIN) })
        }
    }
}