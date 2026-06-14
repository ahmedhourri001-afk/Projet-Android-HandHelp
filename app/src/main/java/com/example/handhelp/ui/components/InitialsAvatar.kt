package com.example.handhelp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// Palette de couleurs pour les avatars (style Material)
private val avatarColors = listOf(
    Color(0xFF2E7D32), // Vert
    Color(0xFF1565C0), // Bleu
    Color(0xFFD84315), // Orange foncé
    Color(0xFF6A1B9A), // Violet
    Color(0xFFAD1457), // Rose
    Color(0xFF00838F), // Cyan
    Color(0xFFEF6C00), // Orange
    Color(0xFF4527A0), // Indigo
    Color(0xFF558B2F), // Vert olive
    Color(0xFFC62828), // Rouge
)

/**
 * Avatar circulaire affichant l'initiale du nom.
 * La couleur de fond est déterminée par le nom (toujours identique pour un même utilisateur).
 *
 * @param name Le nom complet de l'utilisateur (ex: "Soufiane Mouaddine")
 * @param size Taille du cercle (par défaut 56.dp)
 * @param fontSize Taille du texte (calculée automatiquement si non précisée)
 */
@Composable
fun InitialsAvatar(
    name: String,
    size: Dp = 56.dp,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val initial = name.trim()
        .firstOrNull { it.isLetter() }
        ?.uppercaseChar()
        ?.toString() ?: "?"

    val colorIndex = abs(name.trim().lowercase().hashCode()) % avatarColors.size
    val bgColor = if (name.isBlank()) Color.Gray else avatarColors[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}