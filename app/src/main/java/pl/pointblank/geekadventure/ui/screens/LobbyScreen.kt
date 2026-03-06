package pl.pointblank.geekadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.pointblank.geekadventure.data.ScenarioRepository
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.viewmodel.GameViewModel
import pl.pointblank.geekadventure.ui.components.LustrousScenarioCard
import pl.pointblank.geekadventure.ui.components.UserStatsBar
import pl.pointblank.geekadventure.ui.components.findActivity
import pl.pointblank.geekadventure.ui.theme.ThemeEngine
import pl.pointblank.geekadventure.ui.theme.OrbitronFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: GameViewModel, 
    isTablet: Boolean, // Nowy parametr
    onScenarioSelected: (Scenario, Boolean, Boolean) -> Unit
) {
    var selectedScenario by remember { mutableStateOf<Scenario?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showShop by remember { mutableStateOf(false) }
    var enableImages by remember { mutableStateOf(false) }
    var hasSave by remember { mutableStateOf(false) }
    
    val userStats by viewModel.userStats.collectAsState()

    LaunchedEffect(selectedScenario) {
        selectedScenario?.let {
            hasSave = viewModel.hasSavedGame(it.id)
        }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "GEEK ADVENTURE",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = OrbitronFont,
                            letterSpacing = 4.sp
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Box(modifier = Modifier.clickable { showShop = true }) {
                    UserStatsBar(userStats)
                }
            }
        }
    ) { paddingValues ->
        if (isTablet) {
            // UKŁAD DLA TABLETÓW (SIATKA)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp), // Dynamiczna liczba kolumn
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(ScenarioRepository.scenarios) { scenario ->
                    ScenarioItem(scenario, userStats?.isPremiumUser ?: false) {
                        selectedScenario = scenario
                        showDialog = true
                    }
                }
            }
        } else {
            // UKŁAD DLA TELEFONÓW (LISTA)
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Wybierz swoją przygodę:",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontWeight = FontWeight.Light,
                        fontFamily = OrbitronFont
                    )
                }
                items(ScenarioRepository.scenarios) { scenario ->
                    ScenarioItem(scenario, userStats?.isPremiumUser ?: false) {
                        selectedScenario = scenario
                        showDialog = true
                    }
                }
            }
        }
    }

    if (showShop) {
        ShopDialog(viewModel = viewModel, onDismiss = { showShop = false })
    }

    if (showDialog && selectedScenario != null) {
        // ... (Dialog pozostaje bez zmian, ale zyskał już czcionki we wcześniejszych krokach)
        LobbyDialog(
            selectedScenario!!, 
            userStats?.isPremiumUser ?: false, 
            hasSave,
            enableImages,
            onEnableImagesChange = { enableImages = it },
            onDismiss = { showDialog = false },
            onStart = { scenario, images, resume ->
                onScenarioSelected(scenario, images, resume)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun ScenarioItem(scenario: Scenario, isPremiumUser: Boolean, onClick: () -> Unit) {
    Box {
        LustrousScenarioCard(
            scenario = scenario, 
            onClick = onClick
        )
        if (scenario.isPremium) {
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd),
                color = Color(0xFFFFD700),
                contentColor = Color.Black,
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PREMIUM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontFamily = OrbitronFont)
                }
            }
        }
    }
}

// Wydzielony dialog dla czytelności LobbyScreen
@Composable
fun LobbyDialog(
    scenario: Scenario,
    isPremiumUser: Boolean,
    hasSave: Boolean,
    enableImages: Boolean,
    onEnableImagesChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onStart: (Scenario, Boolean, Boolean) -> Unit,
    viewModel: GameViewModel
) {
    val isLocked = scenario.isPremium && !isPremiumUser
    val style = scenario.visualStyle
    val themeData = ThemeEngine.getTheme(style, scenario.themeColor, scenario.secondaryColor)
    val font = themeData.fontFamily

    val dialogBg = when(style) {
        ScenarioStyle.CYBERPUNK -> Color(0xFF0D0D0D)
        ScenarioStyle.FANTASY -> Color(0xFFF5E6D3)
        ScenarioStyle.HORROR -> Color(0xFF050505)
        ScenarioStyle.PIRATES -> Color(0xFF004D40)
        ScenarioStyle.WESTERN -> Color(0xFFD2B48C)
        ScenarioStyle.SUPERHERO -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val dialogText = when(style) {
        ScenarioStyle.FANTASY, ScenarioStyle.WESTERN -> Color(0xFF2B1B17)
        ScenarioStyle.SUPERHERO -> Color.Black
        else -> Color.White
    }

    val dialogAccent = if (isLocked) Color.Gray else when(style) {
        ScenarioStyle.FANTASY -> Color(0xFF8B4513)
        ScenarioStyle.WESTERN -> Color(0xFF5D4037)
        ScenarioStyle.SUPERHERO -> Color.Yellow
        ScenarioStyle.PIRATES -> Color(0xFFC2B280)
        else -> scenario.themeColor
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp),
        title = { 
            Text(
                if (isLocked) "SCENARIUSZ ZABLOKOWANY" else scenario.title.uppercase(), 
                color = dialogAccent, 
                fontWeight = FontWeight.Black,
                fontFamily = font,
                letterSpacing = if (style == ScenarioStyle.CYBERPUNK) 2.sp else 0.sp
            ) 
        },
        text = {
            Column {
                if (isLocked) {
                    Text(
                        "Ten scenariusz jest dostępny tylko dla subskrybentów Geek Master. Wesprzyj nas i graj bez limitów!",
                        color = dialogText,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )
                } else {
                    Text(scenario.description, color = dialogText, fontFamily = font)
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dialogAccent.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!isLocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generuj obrazy AI do scen", color = dialogText, modifier = Modifier.weight(1f), fontFamily = font)
                        Switch(
                            checked = enableImages,
                            onCheckedChange = onEnableImagesChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = dialogAccent,
                                checkedTrackColor = dialogAccent.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isLocked) {
                val context = LocalContext.current
                Button(
                    onClick = { 
                        context.findActivity()?.let { activity ->
                            viewModel.buyProduct(activity, pl.pointblank.geekadventure.util.BillingManager.PREMIUM_SUB) 
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Zasubskrybuj", fontFamily = OrbitronFont)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasSave) {
                        TextButton(
                            onClick = { onStart(scenario, enableImages, true) },
                            colors = ButtonDefaults.textButtonColors(contentColor = dialogAccent)
                        ) {
                            Text("Kontynuuj", fontFamily = font)
                        }
                    }
                    Button(
                        onClick = { onStart(scenario, enableImages, false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dialogAccent, 
                            contentColor = if (style == ScenarioStyle.SUPERHERO) Color.Black else Color.White
                        ),
                        shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
                    ) {
                        Text(if (hasSave) "Nowa gra" else "Rozpocznij", fontFamily = font)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = dialogText.copy(alpha = 0.6f))
            ) {
                Text("Anuluj", fontFamily = font)
            }
        }
    )
}
