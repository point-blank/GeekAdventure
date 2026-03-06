package pl.pointblank.geekadventure.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.pointblank.geekadventure.data.local.LoreEntry
import pl.pointblank.geekadventure.data.local.UserStats
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.ui.components.*
import pl.pointblank.geekadventure.ui.theme.ThemeEngine
import pl.pointblank.geekadventure.ui.theme.GameThemeData
import pl.pointblank.geekadventure.util.ResponseParser
import pl.pointblank.geekadventure.viewmodel.GameState
import pl.pointblank.geekadventure.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val loreEntries by viewModel.loreEntries.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    var userInput by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDamageFlash by remember { mutableStateOf(false) }
    val damageAlpha by animateFloatAsState(
        targetValue = if (showDamageFlash) 0.5f else 0f,
        animationSpec = tween(300),
        label = "damage",
        finishedListener = { if (showDamageFlash) showDamageFlash = false }
    )

    val scenario = when (val state = uiState) {
        is GameState.Success -> state.scenario
        is GameState.Processing -> state.scenario
        else -> null
    }

    val parsed = if (uiState is GameState.Success) {
        val response = (uiState as GameState.Success).latestResponse
        LaunchedEffect(response) {
            if (response.contains(Regex("\\[(HP|Zdrowie|Życie):\\s*-\\d+"))) {
                showDamageFlash = true
                delay(300)
                showDamageFlash = false
            }
        }
        ResponseParser.parse(response)
    } else null

    val style = scenario?.visualStyle ?: ScenarioStyle.DEFAULT
    val baseThemeColor = scenario?.themeColor ?: MaterialTheme.colorScheme.primary
    val baseSecondaryColor = scenario?.secondaryColor ?: MaterialTheme.colorScheme.secondary
    
    val theme = remember(style, baseThemeColor) {
        ThemeEngine.getTheme(style, baseThemeColor, baseSecondaryColor)
    }

    val animatedBgColor by animateColorAsState(targetValue = theme.backgroundColor, label = "bg")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ThematicDrawerContent(
                themeColor = theme.primaryColor,
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
            containerColor = animatedBgColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = scenario?.title?.uppercase() ?: "GEEK ADVENTURE",
                            fontFamily = theme.fontFamily,
                            fontWeight = FontWeight.ExtraBold, // DODANO
                            letterSpacing = 3.sp, // ZWIĘKSZONO
                            color = theme.primaryColor,
                            fontSize = 20.sp // DODANO
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.contentColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = theme.contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .vignette(Color.Black.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (val state = uiState) {
                            is GameState.Loading -> LoadingView(theme.primaryColor)
                            is GameState.Processing -> ProcessingView(theme.primaryColor)
                            is GameState.Error -> ErrorView(state.message) {
                                if (scenario != null) {
                                    viewModel.initGame(scenario, resume = true)
                                } else {
                                    onBack()
                                }
                            }
                            is GameState.Success -> {
                                ImmersiveGameContent(
                                    parsed = parsed!!,
                                    theme = theme
                                )
                            }
                        }
                    }

                    if (uiState is GameState.Success) {
                        ImmersiveInteractionArea(
                            parsed = parsed!!,
                            theme = theme,
                            userInput = userInput,
                            onUserInputChange = { userInput = it },
                            onSend = {
                                viewModel.sendPrompt(it)
                                userInput = ""
                            }
                        )
                    }
                }

                if (damageAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = damageAlpha))
                    )
                }
            }
        }
    }
}

@Composable
fun ImmersiveGameContent(
    parsed: ResponseParser.ParsedResponse,
    theme: GameThemeData
) {
    val scrollState = rememberScrollState()
    
    LaunchedEffect(parsed.cleanText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (parsed.chapterTitle != null) {
            Text(
                text = parsed.chapterTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = theme.primaryColor,
                fontFamily = theme.fontFamily,
                fontWeight = FontWeight.Black, // ZWIĘKSZONO
                fontSize = 24.sp, // DODANO
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        if (parsed.imagePrompt != null) {
            ShimmerImagePlaceholder(theme)
            Spacer(modifier = Modifier.height(24.dp))
        }

        TypewriterText(
            text = parsed.cleanText,
            theme = theme
        )

        if (parsed.mechanicsTag != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                color = theme.primaryColor.copy(alpha = 0.1f),
                shape = theme.containerShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = parsed.mechanicsTag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = theme.primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = theme.fontFamily,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(160.dp))
    }
}

@Composable
fun ImmersiveInteractionArea(
    parsed: ResponseParser.ParsedResponse,
    theme: GameThemeData,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color.Transparent, theme.backgroundColor.copy(alpha = 0.95f), theme.backgroundColor),
                    startY = 0f,
                    endY = 100f
                )
            )
            .padding(16.dp)
    ) {
        Column {
            val options = parsed.options.takeIf { it.isNotEmpty() } ?: listOf("A: Kontynuuj")
            
            options.forEachIndexed { index, option ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 100L + 500)
                    visible = true
                }
                
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(300))
                ) {
                    ImmersiveButton(
                        text = option,
                        theme = theme,
                        onClick = { onSend(option) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = onUserInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "Wpisz własną akcję...", 
                        color = theme.contentColor.copy(alpha = 0.4f),
                        fontFamily = theme.fontFamily
                    ) 
                },
                trailingIcon = {
                    IconButton(onClick = { if (userInput.isNotBlank()) onSend(userInput) }) {
                        Icon(Icons.Default.Send, contentDescription = "Wyślij", tint = theme.primaryColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primaryColor,
                    cursorColor = theme.primaryColor,
                    unfocusedBorderColor = theme.contentColor.copy(alpha = 0.3f),
                    focusedTextColor = theme.contentColor,
                    unfocusedTextColor = theme.contentColor,
                    focusedContainerColor = theme.surfaceColor.copy(alpha = 0.5f),
                    unfocusedContainerColor = theme.surfaceColor.copy(alpha = 0.3f)
                ),
                shape = theme.containerShape,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = theme.fontFamily, fontSize = 16.sp) // ZWIĘKSZONO
            )
        }
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
            Text("Mistrz Gry układa opowieść...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun ThematicDrawerContent(
    themeColor: Color, 
    style: ScenarioStyle, 
    parsed: ResponseParser.ParsedResponse?, 
    loreEntries: List<LoreEntry>,
    userStats: UserStats?,
    onUndo: () -> Unit
) {
    val theme = ThemeEngine.getTheme(style, themeColor, themeColor) // Pobieramy pełny motyw dla czcionek
    
    val bgColor = when(style) {
        ScenarioStyle.FANTASY -> Color(0xFFF5E6D3)
        ScenarioStyle.WESTERN -> Color(0xFFD2B48C)
        ScenarioStyle.SUPERHERO -> Color.White
        else -> Color(0xFF121212)
    }
    
    val textColor = when(style) {
        ScenarioStyle.FANTASY, ScenarioStyle.WESTERN, ScenarioStyle.SUPERHERO -> Color(0xFF2B1B17)
        else -> Color.White
    }

    ModalDrawerSheet(
        drawerContainerColor = bgColor
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "ZASOBY", 
                style = MaterialTheme.typography.headlineMedium, 
                color = themeColor, 
                fontWeight = FontWeight.Black,
                fontFamily = theme.fontFamily // DODANO
            )
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
                    Text("COFNIJ CZAS (1 Kryształ)", fontFamily = theme.fontFamily)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "STATYSTYKI POSTACI", 
                style = MaterialTheme.typography.titleLarge, 
                color = themeColor, 
                fontWeight = FontWeight.Bold,
                fontFamily = theme.fontFamily // DODANO
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val stats = parsed?.gameState ?: ResponseParser.PlayerStats()
            StatRow(Icons.Default.Person, "Klasa", stats.`class`, themeColor, textColor, theme.fontFamily)
            StatRow(Icons.Default.Favorite, "HP", "${stats.hp}/100", themeColor, textColor, theme.fontFamily)
            StatRow(Icons.Default.MonetizationOn, "Złoto", "${stats.gold} szt.", themeColor, textColor, theme.fontFamily)
            
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = themeColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "DZIENNIK LORE", 
                style = MaterialTheme.typography.titleLarge, 
                color = themeColor, 
                fontWeight = FontWeight.Bold,
                fontFamily = theme.fontFamily // DODANO
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (loreEntries.isEmpty()) {
                Text("Odkrywaj świat, aby zapisać fakty.", color = textColor.copy(alpha = 0.6f), fontFamily = theme.fontFamily)
            } else {
                loreEntries.forEach { entry ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(entry.key, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor, fontFamily = theme.fontFamily)
                        Text(entry.description, style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.8f), fontFamily = theme.fontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color, textColor: Color, font: androidx.compose.ui.text.font.FontFamily) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f), fontFamily = font)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor, fontFamily = font)
        }
    }
}
