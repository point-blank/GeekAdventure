package pl.pointblank.geekadventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.ui.theme.ThemeEngine

@Composable
fun LustrousScenarioCard(scenario: Scenario, onClick: () -> Unit) {
    // Pobieramy font z ThemeEngine
    val theme = ThemeEngine.getTheme(scenario.visualStyle, scenario.themeColor, scenario.secondaryColor)
    val font = theme.fontFamily

    when (scenario.visualStyle) {
        ScenarioStyle.CYBERPUNK -> CyberpunkCard(scenario, font, onClick)
        ScenarioStyle.FANTASY -> FantasyCard(scenario, font, onClick)
        ScenarioStyle.HORROR -> HorrorCard(scenario, font, onClick)
        ScenarioStyle.SUPERHERO -> ComicCard(scenario, font, onClick)
        ScenarioStyle.PIRATES -> PiratesCard(scenario, font, onClick)
        ScenarioStyle.WESTERN -> WesternCard(scenario, font, onClick)
        else -> DefaultLustrousCard(scenario, font, onClick)
    }
}

@Composable
fun CyberpunkCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse), label = "glow"
    )

    val cyberpunkShape = GenericShape { size, _ ->
        moveTo(0f, 0f); lineTo(size.width - 40f, 0f); lineTo(size.width, 40f)
        lineTo(size.width, size.height); lineTo(40f, size.height); lineTo(0f, size.height - 40f); close()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(
                elevation = 12.dp * glowIntensity, 
                shape = cyberpunkShape, 
                ambientColor = scenario.themeColor, 
                spotColor = scenario.themeColor
            )
            .clip(cyberpunkShape)
            .background(Color(0xFF0D0D0D))
            .border(2.dp, Brush.linearGradient(listOf(scenario.themeColor, scenario.secondaryColor)), cyberpunkShape)
            .clickable(onClick = onClick)
            .drawBehind {
                for (i in 0..size.width.toInt() step 40) {
                    drawLine(Color.White.copy(alpha = 0.05f), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height))
                }
            }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = scenario.title.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = scenario.themeColor,
                fontFamily = font, // DODANO
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = font // DODANO
            )
        }
    }
}

@Composable
fun FantasyCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth().height(140.dp)
            .shadow(4.dp, MaterialTheme.shapes.medium)
            .background(Color(0xFFF5E6D3))
            .border(4.dp, Brush.sweepGradient(listOf(scenario.themeColor, scenario.secondaryColor, scenario.themeColor)), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4B2C20),
                fontFamily = font, // DODANO
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D4037),
                fontFamily = font // DODANO
            )
        }
    }
}

@Composable
fun PiratesCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .background(Color(0xFF004D40))
            .border(3.dp, Color(0xFFC2B280))
            .clickable(onClick = onClick).padding(20.dp)
    ) {
        Column {
            Text(text = scenario.title, color = Color(0xFFC2B280), fontFamily = font, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, color = Color.White.copy(alpha = 0.9f), fontFamily = font, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HorrorCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .background(Color.Black).border(1.dp, scenario.themeColor.copy(alpha = 0.5f))
            .clickable(onClick = onClick).padding(20.dp)
    ) {
        Column {
            Text(text = scenario.title, color = scenario.themeColor, fontFamily = font, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, color = Color.Gray, fontFamily = font, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ComicCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .drawBehind { drawRect(Color.Black, Offset(12f, 12f), size) }
            .background(scenario.themeColor).border(3.dp, Color.Black)
            .clickable(onClick = onClick).padding(16.dp)
    ) {
        Column {
            Surface(color = Color.Yellow, border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)) {
                Text(text = scenario.title.uppercase(), color = Color.Black, fontFamily = font, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
            }
            Text(text = scenario.description, color = Color.Black, fontFamily = font, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun WesternCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .background(Color(0xFFD2B48C)).border(2.dp, Color(0xFF5D4037))
            .clickable(onClick = onClick).padding(20.dp)
    ) {
        Column {
            Text(text = scenario.title.uppercase(), color = Color(0xFF5D4037), fontFamily = font, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, color = Color(0xFF5D4037).copy(alpha = 0.8f), fontFamily = font, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DefaultLustrousCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = scenario.themeColor.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, scenario.themeColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = scenario.title, style = MaterialTheme.typography.headlineSmall, fontFamily = font, color = scenario.themeColor, fontWeight = FontWeight.Bold)
            Text(text = scenario.description, fontFamily = font, color = Color.White)
        }
    }
}
