package pl.pointblank.geekadventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle

@Composable
fun LustrousScenarioCard(scenario: Scenario, onClick: () -> Unit) {
    when (scenario.visualStyle) {
        ScenarioStyle.CYBERPUNK -> CyberpunkCard(scenario, onClick)
        ScenarioStyle.FANTASY -> FantasyCard(scenario, onClick)
        ScenarioStyle.HORROR -> HorrorCard(scenario, onClick)
        ScenarioStyle.SUPERHERO -> ComicCard(scenario, onClick)
        else -> DefaultLustrousCard(scenario, onClick)
    }
}

@Composable
fun CyberpunkCard(scenario: Scenario, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    val cyberpunkShape = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width - 40f, 0f)
        lineTo(size.width, 40f)
        lineTo(size.width, size.height)
        lineTo(40f, size.height)
        lineTo(0f, size.height - 40f)
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(12.dp * glowIntensity, cyberpunkShape, ambientColor = scenario.themeColor, spotColor = scenario.themeColor)
            .clip(cyberpunkShape)
            .background(Color(0xFF0D0D0D))
            .border(2.dp, Brush.linearGradient(listOf(scenario.themeColor, scenario.secondaryColor)), cyberpunkShape)
            .clickable(onClick = onClick)
            .drawBehind {
                // Dodajemy "siatkę" technologiczna w tle
                for (i in 0..size.width.toInt() step 40) {
                    drawLine(Color.White.copy(alpha = 0.05f), Offset(i.f, 0f), Offset(i.f, size.height))
                }
            }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = scenario.title.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = scenario.themeColor,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FantasyCard(scenario: Scenario, onClick: () -> Unit) {
    val parchmentColor = Color(0xFFF5E6D3)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(4.dp, shape = MaterialTheme.shapes.medium)
            .background(parchmentColor)
            .border(4.dp, Brush.sweepGradient(listOf(scenario.themeColor, scenario.secondaryColor, scenario.themeColor)), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .drawBehind {
                // Efekt "starości" pergaminu
                drawRect(Color.Black.copy(alpha = 0.05f))
            }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4B2C20), // Dark brown
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D4037)
            )
        }
    }
}

@Composable
fun HorrorCard(scenario: Scenario, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color.Black)
            .border(1.dp, scenario.themeColor.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .drawBehind {
                // Krwawe/atramentowe rozbryzgi w rogach
                drawCircle(scenario.themeColor.copy(alpha = 0.2f), radius = 100f, center = Offset(size.width, 0f))
                drawCircle(scenario.themeColor.copy(alpha = 0.1f), radius = 150f, center = Offset(0f, size.height))
            }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.headlineSmall,
                color = scenario.themeColor,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ComicCard(scenario: Scenario, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .drawBehind {
                // Hard comic shadow
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(16f, 16f),
                    size = size
                )
            }
            .background(scenario.themeColor)
            .border(4.dp, Color.Black)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Surface(color = Color.Yellow, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = scenario.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DefaultLustrousCard(scenario: Scenario, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = scenario.themeColor.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, scenario.themeColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.headlineSmall,
                color = scenario.themeColor,
                fontWeight = FontWeight.Bold
            )
            Text(text = scenario.description)
        }
    }
}

val Int.f get() = this.toFloat()
