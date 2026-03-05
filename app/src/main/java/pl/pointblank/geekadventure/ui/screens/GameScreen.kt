package pl.pointblank.geekadventure.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.pointblank.geekadventure.data.local.LoreEntry
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.util.ResponseParser
import pl.pointblank.geekadventure.viewmodel.GameState
import pl.pointblank.geekadventure.viewmodel.GameViewModel

@Composable
fun MarkdownText(text: String, style: androidx.compose.ui.text.TextStyle, color: Color) {
    val annotatedString = buildAnnotatedString {
        var cursor = 0
        val regex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = regex.findAll(text)

        matches.forEach { match ->
            append(text.substring(cursor, match.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }

    Text(
        text = annotatedString,
        style = style,
        lineHeight = 28.sp,
        color = color
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val loreEntries by viewModel.loreEntries.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    var userInput by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val scenario = when (val state = uiState) {
        is GameState.Success -> state.scenario
        is GameState.Processing -> state.scenario
        else -> null
    }
    
    val themeColor = scenario?.themeColor ?: MaterialTheme.colorScheme.primary
    val secondaryColor = scenario?.secondaryColor ?: MaterialTheme.colorScheme.surface
    val style = scenario?.visualStyle ?: ScenarioStyle.DEFAULT

    val parsed = if (uiState is GameState.Success) {
        ResponseParser.parse((uiState as GameState.Success).latestResponse)
    } else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ThematicDrawerContent(
                themeColor = themeColor, 
                style = style, 
                parsed = parsed, 
                loreEntries = loreEntries,
                userStats = userStats,
                onUndo = {
                    scope.launch {
                        drawerState.close()
                        viewModel.undoLastAction()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                ThematicTopBar(scenario, themeColor, style, onBack) {
                    scope.launch { drawerState.open() }
                }
            }
        ) { paddingValues ->
            ThematicBackground(style, themeColor, secondaryColor, paddingValues) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (val state = uiState) {
                            is GameState.Loading -> LoadingView(themeColor)
                            is GameState.Processing -> ProcessingView(themeColor)
                            is GameState.Error -> ErrorView(state.message, onBack)
                            is GameState.Success -> {
                                GameContent(state, themeColor, style)
                            }
                        }
                    }
                    
                    if (uiState is GameState.Success) {
                        InteractionArea(
                            parsed = parsed!!,
                            themeColor = themeColor,
                            style = style,
                            userInput = userInput,
                            onUserInputChange = { userInput = it },
                            onSend = { 
                                viewModel.sendPrompt(it)
                                userInput = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThematicTopBar(scenario: Scenario?, themeColor: Color, style: ScenarioStyle, onBack: () -> Unit, onMenuClick: () -> Unit) {
    val containerColor = when (style) {
        ScenarioStyle.CYBERPUNK -> Color.Black
        ScenarioStyle.HORROR -> Color(0xFF050505)
        ScenarioStyle.FANTASY -> Color(0xFF4B2C20)
        else -> themeColor
    }

    val titleColor = when (style) {
        ScenarioStyle.CYBERPUNK -> themeColor
        ScenarioStyle.FANTASY -> Color(0xFFD4AF37)
        else -> Color.White
    }

    Column {
        TopAppBar(
            title = { 
                Text(
                    text = scenario?.title?.uppercase() ?: "GEEK ADVENTURE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (style == ScenarioStyle.CYBERPUNK) 2.sp else 0.sp
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
            },
            actions = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Statystyki")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = titleColor,
                navigationIconContentColor = titleColor,
                actionIconContentColor = titleColor
            )
        )
        // Klimatyczna linia pod Toolbarem
        if (style == ScenarioStyle.CYBERPUNK) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(themeColor, Color.Transparent))))
        } else if (style == ScenarioStyle.FANTASY) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFD4AF37).copy(alpha = 0.5f)))
        }
    }
}

@Composable
fun ThematicBackground(style: ScenarioStyle, themeColor: Color, secondaryColor: Color, padding: PaddingValues, content: @Composable () -> Unit) {
    val backgroundColor = when (style) {
        ScenarioStyle.CYBERPUNK -> Color(0xFF050505)
        ScenarioStyle.HORROR -> Color(0xFF0A0A0A)
        ScenarioStyle.FANTASY -> Color(0xFFFDF5E6)
        else -> MaterialTheme.colorScheme.background
    }

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(backgroundColor)
            .drawBehind {
                if (style == ScenarioStyle.CYBERPUNK) {
                    for (i in 0..size.height.toInt() step 8) {
                        drawLine(Color.White.copy(alpha = 0.03f), Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()))
                    }
                }
            }
    ) {
        content()
    }
}

@Composable
fun GameContent(state: GameState.Success, themeColor: Color, style: ScenarioStyle) {
    val scrollState = rememberScrollState()
    val parsed = ResponseParser.parse(state.latestResponse)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        ThematicFrame(style, themeColor) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (parsed.chapterTitle != null) {
                    Text(
                        text = parsed.chapterTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (style == ScenarioStyle.FANTASY) Color(0xFF4B2C20) else themeColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                MarkdownText(
                    text = parsed.cleanText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (style == ScenarioStyle.FANTASY) Color(0xFF2B1B17) else Color.White
                )

                if (parsed.mechanicsTag != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = themeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = parsed.mechanicsTag,
                            modifier = Modifier.padding(8.dp),
                            color = themeColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (parsed.imagePrompt != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ImagePlaceholder(parsed.imagePrompt, themeColor)
        }
        
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun ThematicFrame(style: ScenarioStyle, themeColor: Color, content: @Composable () -> Unit) {
    val shape = when (style) {
        ScenarioStyle.CYBERPUNK -> GenericShape { size, _ ->
            moveTo(0f, 20f); lineTo(20f, 0f); lineTo(size.width - 20f, 0f); lineTo(size.width, 20f)
            lineTo(size.width, size.height - 20f); lineTo(size.width - 20f, size.height); lineTo(20f, size.height); lineTo(0f, size.height - 20f)
            close()
        }
        else -> RoundedCornerShape(12.dp)
    }

    val borderBrush = when (style) {
        ScenarioStyle.CYBERPUNK -> Brush.linearGradient(listOf(themeColor, Color.Magenta))
        ScenarioStyle.FANTASY -> Brush.verticalGradient(listOf(Color(0xFFD4AF37), Color(0xFF8B4513)))
        else -> SolidColor(themeColor.copy(alpha = 0.3f))
    }

    val backgroundColor = when (style) {
        ScenarioStyle.CYBERPUNK -> Color.Black.copy(alpha = 0.8f)
        ScenarioStyle.FANTASY -> Color(0xFFF5E6D3).copy(alpha = 0.9f)
        ScenarioStyle.HORROR -> Color(0xFF1A1A1A).copy(alpha = 0.9f)
        else -> Color.DarkGray.copy(alpha = 0.1f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(if (style == ScenarioStyle.DEFAULT) 1.dp else 2.dp, borderBrush)
    ) {
        content()
    }
}

@Composable
fun InteractionArea(
    parsed: ResponseParser.ParsedResponse,
    themeColor: Color,
    style: ScenarioStyle,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    val areaBackground = when (style) {
        ScenarioStyle.CYBERPUNK -> Color.Black
        ScenarioStyle.FANTASY -> Color(0xFFEBDCB2)
        ScenarioStyle.HORROR -> Color(0xFF050505)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = areaBackground,
        modifier = Modifier.fillMaxWidth(),
        border = if (style == ScenarioStyle.CYBERPUNK) androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val options = parsed.options.takeIf { it.isNotEmpty() } ?: listOf("A: Kontynuuj")

            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { option ->
                        ThematicButton(option, themeColor, style) { onSend(option) }
                    }
                    if (rowOptions.size < 2) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = onUserInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wpisz własną akcję...", color = if (style == ScenarioStyle.FANTASY) Color.DarkGray else Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { if (userInput.isNotBlank()) onSend(userInput) }) {
                        Icon(Icons.Default.Send, contentDescription = "Wyślij", tint = themeColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColor,
                    cursorColor = themeColor,
                    unfocusedBorderColor = themeColor.copy(alpha = 0.5f),
                    focusedTextColor = if (style == ScenarioStyle.FANTASY) Color.Black else Color.White,
                    unfocusedTextColor = if (style == ScenarioStyle.FANTASY) Color.Black else Color.White
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun RowScope.ThematicButton(text: String, themeColor: Color, style: ScenarioStyle, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (style == ScenarioStyle.CYBERPUNK) Color.Black else themeColor,
            contentColor = if (style == ScenarioStyle.CYBERPUNK) themeColor else Color.White
        ),
        shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else MaterialTheme.shapes.medium,
        border = if (style == ScenarioStyle.CYBERPUNK) androidx.compose.foundation.BorderStroke(1.dp, themeColor) else null
    ) {
        Text(text, maxLines = 2, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoadingView(color: Color) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = color)
    }
}

@Composable
fun ProcessingView(color: Color) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = color)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Mistrz Gry układa opowieść...", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
    }
}

@Composable
fun ErrorView(message: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Błąd: $message", color = Color.Red)
            Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Wróć do menu")
            }
        }
    }
}

@Composable
fun ImagePlaceholder(prompt: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wizualizacja sceny...", style = MaterialTheme.typography.labelMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(modifier = Modifier.height(8.dp))
                Text("\"$prompt\"", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp), color = Color.Gray)
            }
        }
    }
}

@Composable
fun ThematicDrawerContent(
    themeColor: Color, 
    style: ScenarioStyle, 
    parsed: ResponseParser.ParsedResponse?, 
    loreEntries: List<LoreEntry>,
    userStats: pl.pointblank.geekadventure.data.local.UserStats?,
    onUndo: () -> Unit
) {
    val bgColor = if (style == ScenarioStyle.FANTASY) Color(0xFFF5E6D3) else Color(0xFF121212)
    val textColor = if (style == ScenarioStyle.FANTASY) Color(0xFF4B2C20) else Color.White

    ModalDrawerSheet(
        drawerContainerColor = bgColor
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("ZASOBY", style = MaterialTheme.typography.headlineMedium, color = themeColor, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(16.dp))
            
            UserStatsBar(userStats)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if ((userStats?.chronocrystals ?: 0) > 0) {
                Button(
                    onClick = onUndo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("COFNIJ CZAS (1 Kryształ)")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("STATYSTYKI POSTACI", style = MaterialTheme.typography.titleLarge, color = themeColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            val stats = parsed?.gameState ?: ResponseParser.PlayerStats()
            StatRow(Icons.Default.Person, "Klasa", stats.`class`, themeColor, textColor)
            StatRow(Icons.Default.Favorite, "HP", "${stats.hp}/100", themeColor, textColor)
            StatRow(Icons.Default.MonetizationOn, "Złoto", "${stats.gold} szt.", themeColor, textColor)
            
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = themeColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("DZIENNIK LORE", style = MaterialTheme.typography.titleLarge, color = themeColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (loreEntries.isEmpty()) {
                Text("Odkrywaj świat, aby zapisać fakty.", color = textColor.copy(alpha = 0.6f))
            } else {
                loreEntries.forEach { entry ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(entry.key, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                        Text(entry.description, style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color.White, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Spróbuj ponownie")
            }
        }
    }
}

@Composable
fun UserStatsBar(stats: pl.pointblank.geekadventure.data.local.UserStats?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            icon = Icons.Default.Bolt,
            label = "Energia",
            value = "${stats?.actionPoints ?: 0}/20",
            color = Color(0xFFFFD600)
        )
        
        VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.1f))
        
        StatItem(
            icon = Icons.Default.History,
            label = "Kryształy",
            value = "${stats?.chronocrystals ?: 0}",
            color = Color(0xFF00E5FF)
        )
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color, textColor: Color) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}
