package pl.pointblank.geekadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Face
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
    isTablet: Boolean,
    onScenarioSelected: (Scenario, Boolean, Boolean) -> Unit
) {
    var selectedScenario by remember { mutableStateOf<Scenario?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showShop by remember { mutableStateOf(false) }
    var hasSave by remember { mutableStateOf(false) }
    
    val userStats by viewModel.userStats.collectAsState()

    val geekScenarios = ScenarioRepository.scenarios.filter { !it.isForKids }
    val kidsScenarios = ScenarioRepository.scenarios.filter { it.isForKids }

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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader("DLA GEEKÓW", Color.Cyan)
                }
                items(geekScenarios) { scenario ->
                    ScenarioItem(scenario, userStats?.isPremiumUser ?: false) {
                        selectedScenario = scenario
                        showDialog = true
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(32.dp))
                    SectionHeader("STREFA JUNIORA", Color(0xFFFF4081), Icons.Default.Face)
                }
                items(kidsScenarios) { scenario ->
                    ScenarioItem(scenario, userStats?.isPremiumUser ?: false) {
                        selectedScenario = scenario
                        showDialog = true
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SectionHeader("DLA GEEKÓW", Color.Cyan) }
                items(geekScenarios) { scenario ->
                    ScenarioItem(scenario, userStats?.isPremiumUser ?: false) {
                        selectedScenario = scenario
                        showDialog = true
                    }
                }
                item { 
                    Spacer(Modifier.height(24.dp))
                    SectionHeader("STREFA JUNIORA", Color(0xFFFF4081), Icons.Default.Face) 
                }
                items(kidsScenarios) { scenario ->
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
        LobbyDialog(
            selectedScenario!!, 
            userStats?.isPremiumUser ?: false, 
            hasSave,
            onDismiss = { showDialog = false },
            onStart = { scenario, resume ->
                onScenarioSelected(scenario, false, resume)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun SectionHeader(title: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            fontFamily = OrbitronFont,
            color = color,
            letterSpacing = 2.sp
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

@Composable
fun LobbyDialog(
    scenario: Scenario,
    isPremiumUser: Boolean,
    hasSave: Boolean,
    onDismiss: () -> Unit,
    onStart: (Scenario, Boolean) -> Unit,
    viewModel: GameViewModel
) {
    val isLocked = scenario.isPremium && !isPremiumUser
    val style = scenario.visualStyle
    val themeData = ThemeEngine.getTheme(style, scenario.themeColor, scenario.secondaryColor)
    val font = themeData.fontFamily

    // POPRAWA KOLORÓW DIALOGU DLA MISJI DZIECIĘCYCH
    val dialogBg = when {
        scenario.isForKids -> Color.White // Zawsze białe tło dla dzieci
        style == ScenarioStyle.CYBERPUNK -> Color(0xFF0D0D0D)
        style == ScenarioStyle.FANTASY -> Color(0xFFF5E6D3)
        style == ScenarioStyle.HORROR -> Color(0xFF050505)
        style == ScenarioStyle.PIRATES -> Color(0xFF004D40)
        style == ScenarioStyle.WESTERN -> Color(0xFFD2B48C)
        style == ScenarioStyle.SUPERHERO -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val dialogText = when {
        scenario.isForKids -> Color.Black // Zawsze czarny tekst dla dzieci
        style == ScenarioStyle.FANTASY || style == ScenarioStyle.WESTERN -> Color(0xFF2B1B17)
        style == ScenarioStyle.SUPERHERO -> Color.Black
        else -> Color.White
    }

    val dialogAccent = if (isLocked) Color.Gray else when {
        scenario.isForKids -> Color(0xFFFF4081) // Różowy akcent dla dzieci (czytelny na białym)
        style == ScenarioStyle.FANTASY -> Color(0xFF8B4513)
        style == ScenarioStyle.WESTERN -> Color(0xFF5D4037)
        style == ScenarioStyle.SUPERHERO -> Color.Yellow
        style == ScenarioStyle.PIRATES -> Color(0xFFC2B280)
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
                        fontFamily = font,
                        fontSize = if (scenario.isForKids) 18.sp else 14.sp // WIĘKSZA CZCIONKA DLA DZIECI
                    )
                } else {
                    Text(
                        text = scenario.description, 
                        color = dialogText, 
                        fontFamily = font,
                        fontSize = if (scenario.isForKids) 18.sp else 14.sp // WIĘKSZA CZCIONKA DLA DZIECI
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dialogAccent.copy(alpha = 0.3f))
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
                            onClick = { onStart(scenario, true) },
                            colors = ButtonDefaults.textButtonColors(contentColor = dialogAccent)
                        ) {
                            Text("Kontynuuj", fontFamily = font, fontSize = if (scenario.isForKids) 18.sp else 14.sp)
                        }
                    }
                    Button(
                        onClick = { onStart(scenario, false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dialogAccent, 
                            contentColor = if (style == ScenarioStyle.SUPERHERO || scenario.isForKids) Color.White else Color.White
                        ),
                        shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
                    ) {
                        Text(if (hasSave) "Nowa gra" else "Rozpocznij", fontFamily = font, fontSize = if (scenario.isForKids) 18.sp else 14.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = dialogText.copy(alpha = 0.6f))
            ) {
                Text("Anuluj", fontFamily = font, fontSize = if (scenario.isForKids) 18.sp else 14.sp)
            }
        }
    )
}
