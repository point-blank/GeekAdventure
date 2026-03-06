package pl.pointblank.geekadventure.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.pointblank.geekadventure.data.local.UserStats

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun UserStatsBar(stats: UserStats?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            icon = Icons.Default.Bolt,
            label = "Energia",
            value = "${stats?.actionPoints ?: 0}/10",
            color = Color(0xFFFFD600)
        )
        
        VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        
        StatItem(
            icon = Icons.Default.History,
            label = "Kryształy",
            value = "${stats?.chronocrystals ?: 0}",
            color = Color(0xFF00E5FF)
        )

        stats?.lastDiceResult?.let { result ->
            VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            StatItem(
                icon = Icons.Default.Casino,
                label = "Rzut",
                value = result.toString(),
                color = Color(0xFFE91E63)
            )
        }

        if (stats?.isPremiumUser == true) {
            VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            StatItem(
                icon = Icons.Default.Star,
                label = "Status",
                value = "PREMIUM",
                color = Color(0xFFFFD700)
            )
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
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
            Button(onClick = onRetry) {
                Text("Spróbuj ponownie")
            }
        }
    }
}
