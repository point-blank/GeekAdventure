package pl.pointblank.geekadventure.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.pointblank.geekadventure.data.local.UserStats
import pl.pointblank.geekadventure.ui.theme.OrbitronFont // Upewnij się, że masz ten import!

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun UserStatsBar(stats: UserStats?) {
    // Definiujemy kształt naszego HUD-a
    val hudShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(8.dp, hudShape, ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF00E5FF))
            .clip(hudShape)
            // Szklane, ciemne tło (Glassmorphism)
            .background(Color(0xFF0A0A0A).copy(alpha = 0.85f))
            // Subtelna, cyber-ramka
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color(0xFFFF00FF).copy(alpha = 0.3f))
                ),
                shape = hudShape
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                icon = Icons.Default.Bolt,
                label = "Energia",
                value = "${stats?.actionPoints ?: 0}/10",
                color = Color(0xFFFFD600),
                isPulsing = true // Włączamy pulsowanie dla Energii!
            )

            // Stylizowane separatory
            HUDDivider()

            StatItem(
                icon = Icons.Default.History,
                label = "Kryształy",
                value = "${stats?.chronocrystals ?: 0}",
                color = Color(0xFF00E5FF)
            )

            stats?.lastDiceResult?.let { result ->
                HUDDivider()
                StatItem(
                    icon = Icons.Default.Casino,
                    label = "Rzut",
                    value = result.toString(),
                    color = Color(0xFFE91E63)
                )
            }

            if (stats?.isPremiumUser == true) {
                HUDDivider()
                StatItem(
                    icon = Icons.Default.Star,
                    label = "Status",
                    value = "PRO",
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String, color: Color, isPulsing: Boolean = false) {
    // Logika animacji "bicia serca"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = if (isPulsing) 8f else 0f // Lekki glow podczas pulsu
                }
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                fontFamily = OrbitronFont, // Używamy Twojego gamingowego fontu!
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun HUDDivider() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.2f), Color.Transparent)
                )
            )
    )
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Spróbuj ponownie")
            }
        }
    }
}