package pl.pointblank.geekadventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.ui.theme.ThemeEngine

@Composable
fun LustrousScenarioCard(scenario: Scenario, onClick: () -> Unit) {
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
fun CardBackgroundIcon(iconRes: Int?, color: Color, alpha: Float = 0.15f) {
    if (iconRes != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color.copy(alpha = alpha),
                modifier = Modifier
                    .size(140.dp)
                    .offset(x = 30.dp, y = 30.dp)
            )
        }
    }
}

@Composable
fun CyberpunkCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse), label = "glow"
    )

    // Animacja spoczynkowa (pływanie pierwszego planu)
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float"
    )

    // Animacja spoczynkowa tła (oddychający zoom)
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.15f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    val cyberpunkShape = GenericShape { size, _ ->
        moveTo(0f, 0f); lineTo(size.width - 40f, 0f); lineTo(size.width, 40f)
        lineTo(size.width, size.height); lineTo(40f, size.height); lineTo(0f, size.height - 40f); close()
    }

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(
                elevation = 12.dp * glowIntensity,
                shape = cyberpunkShape,
                ambientColor = scenario.themeColor,
                spotColor = scenario.themeColor
            )
            .clip(cyberpunkShape) // TO ZATRZYMUJE OBRAZKI PRZED WYLEWANIEM SIĘ
            .background(Color(0xFF0D0D0D))
            .border(2.dp, Brush.linearGradient(listOf(scenario.themeColor, scenario.secondaryColor)), cyberpunkShape)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick)
    ) {
        if (scenario.backgroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.backgroundResId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distanceFromCenter = itemY - (screenHeightPx / 2f)
                        translationY = distanceFromCenter * 0.05f
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF050505).copy(alpha = 0.95f)),
                        startY = 0f, endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        if (scenario.foregroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.foregroundResId),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .graphicsLayer {
                        val scrollOffset = (itemY - (screenHeightPx / 2f)) * -0.12f
                        translationY = scrollOffset + floatAnim
                    }
            )
        } else if (scenario.backgroundResId == null) {
            CardBackgroundIcon(scenario.iconRes, scenario.themeColor, 0.2f)
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(text = scenario.title.uppercase(), style = MaterialTheme.typography.headlineSmall, color = scenario.themeColor, fontFamily = font, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f), fontFamily = font, maxLines = 2)
        }
    }
}

@Composable
fun FantasyCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "fantasy_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.15f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.medium) // ZABEZPIECZENIE PRZED WYLEWANIEM OBRAZKÓW
            .border(4.dp, Color(0xFFC0A060), MaterialTheme.shapes.medium)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick)
    ) {

        if (scenario.backgroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.backgroundResId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distanceFromCenter = itemY - (screenHeightPx / 2f)
                        translationY = distanceFromCenter * 0.05f
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF2B1B17).copy(alpha = 0.95f)),
                        startY = 0f, endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        if (scenario.foregroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.foregroundResId),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .graphicsLayer {
                        val scrollOffset = (itemY - (screenHeightPx / 2f)) * -0.12f
                        translationY = scrollOffset + floatAnim
                    }
            )
        } else if (scenario.backgroundResId == null) {
            CardBackgroundIcon(scenario.iconRes, Color.Black, 0.05f)
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(text = scenario.title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFFE5D5A0), fontFamily = font, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f), fontFamily = font, maxLines = 2)
        }
    }
}

@Composable
fun PiratesCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pirate_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.15f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.medium) // ZABEZPIECZENIE
            .border(3.dp, Color(0xFFC2B280), MaterialTheme.shapes.medium)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick)
    ) {
        if (scenario.backgroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.backgroundResId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distanceFromCenter = itemY - (screenHeightPx / 2f)
                        translationY = distanceFromCenter * 0.05f
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF003020).copy(alpha = 0.95f)),
                        startY = 0f, endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        if (scenario.foregroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.foregroundResId),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .graphicsLayer {
                        val scrollOffset = (itemY - (screenHeightPx / 2f)) * -0.12f
                        translationY = scrollOffset + floatAnim
                    }
            )
        } else if (scenario.backgroundResId == null) {
            CardBackgroundIcon(scenario.iconRes, Color.White, 0.1f)
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(text = scenario.title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFFC2B280), fontFamily = font, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f), fontFamily = font, maxLines = 2)
        }
    }
}

@Composable
fun HorrorCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "horror_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f, // Bardzo subtelny, niepokojący ruch
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "float"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.15f, targetValue = 1.16f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.medium) // ZABEZPIECZENIE
            .border(1.dp, scenario.themeColor.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick)
    ) {
        if (scenario.backgroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.backgroundResId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distanceFromCenter = itemY - (screenHeightPx / 2f)
                        translationY = distanceFromCenter * 0.05f
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.98f)),
                        startY = 0f, endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        if (scenario.foregroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.foregroundResId),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .graphicsLayer {
                        val scrollOffset = (itemY - (screenHeightPx / 2f)) * -0.12f
                        translationY = scrollOffset + floatAnim
                    }
            )
        } else if (scenario.backgroundResId == null) {
            CardBackgroundIcon(scenario.iconRes, scenario.themeColor, 0.1f)
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(text = scenario.title, style = MaterialTheme.typography.headlineSmall, color = scenario.themeColor, fontFamily = font, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f), fontFamily = font, maxLines = 2)
        }
    }
}

@Composable
fun ComicCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "comic_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.15f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.medium) // ZABEZPIECZENIE
            .background(scenario.themeColor)
            .border(3.dp, Color.Black, MaterialTheme.shapes.medium)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick)
    ) {
        if (scenario.backgroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.backgroundResId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distanceFromCenter = itemY - (screenHeightPx / 2f)
                        translationY = distanceFromCenter * 0.05f
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)))
            )
        )

        if (scenario.foregroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.foregroundResId),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-10).dp)
                    .graphicsLayer {
                        val scrollOffset = (itemY - (screenHeightPx / 2f)) * -0.12f
                        translationY = scrollOffset + floatAnim
                    }
            )
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Surface(color = Color.Yellow, border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)) {
                Text(text = scenario.title.uppercase(), color = Color.Black, fontFamily = font, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, color = Color.White, fontFamily = font, fontWeight = FontWeight.Bold, maxLines = 2)
        }
    }
}

@Composable
fun WesternCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "western_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.15f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.medium) // ZABEZPIECZENIE
            .background(Color(0xFFD2B48C))
            .border(2.dp, Color(0xFF5D4037), MaterialTheme.shapes.medium)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick)
    ) {
        if (scenario.backgroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.backgroundResId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distanceFromCenter = itemY - (screenHeightPx / 2f)
                        translationY = distanceFromCenter * 0.05f
                        scaleX = breathScale
                        scaleY = breathScale
                    }
            )
        }

        // Lekka sepia/kurz na dole
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF3E2723).copy(alpha = 0.9f)))
            )
        )

        if (scenario.foregroundResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = scenario.foregroundResId),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .graphicsLayer {
                        val scrollOffset = (itemY - (screenHeightPx / 2f)) * -0.12f
                        translationY = scrollOffset + floatAnim
                    }
            )
        } else if (scenario.backgroundResId == null) {
            CardBackgroundIcon(scenario.iconRes, Color.Black, 0.05f)
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(text = scenario.title.uppercase(), color = Color(0xFFEFEBE9), fontFamily = font, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = scenario.description, color = Color.White.copy(alpha = 0.9f), fontFamily = font, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}

@Composable
fun DefaultLustrousCard(scenario: Scenario, font: FontFamily, onClick: () -> Unit) {
    // 1. Zmienne do Parallaxa
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    var itemY by remember { mutableFloatStateOf(0f) }

    // 2. Animacja spoczynkowa (oddychanie tła)
    val infiniteTransition = rememberInfiniteTransition(label = "default_anim")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.10f,
        targetValue = 1.13f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Zwiększyłem lekko, żeby lepiej było widać grafikę
            .clip(MaterialTheme.shapes.medium)
            .onGloballyPositioned { coordinates ->
                itemY = coordinates.positionInWindow().y
            }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = scenario.themeColor),
        border = androidx.compose.foundation.BorderStroke(2.dp, scenario.themeColor.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // WARSTWA 1: Obrazek tła (np. bg_dino, bg_sea)
            if (scenario.backgroundResId != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = scenario.backgroundResId),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val distanceFromCenter = itemY - (screenHeightPx / 2f)
                            translationY = distanceFromCenter * 0.04f // Delikatny parallax
                            scaleX = breathScale
                            scaleY = breathScale
                        }
                )
            }

            // WARSTWA 2: Winieta / Przyciemnienie (szczególnie ważne dla czytelności tekstu)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                (if (scenario.isForKids) Color.White else Color.Black).copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // WARSTWA 3: Ikona rezerwowa (wyświetla się tylko gdy nie ma obrazka)
            if (scenario.backgroundResId == null) {
                CardBackgroundIcon(scenario.iconRes, Color.Black, 0.1f)
            }

            // WARSTWA 4: Tekst (Dostosowany do trybu Junior / Default)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = font,
                    color = if (scenario.isForKids) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (scenario.isForKids) 22.sp else 20.sp
                )
                Text(
                    text = scenario.description,
                    fontFamily = font,
                    color = (if (scenario.isForKids) Color.Black else Color.White).copy(alpha = 0.8f),
                    maxLines = 2,
                    fontSize = if (scenario.isForKids) 16.sp else 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}