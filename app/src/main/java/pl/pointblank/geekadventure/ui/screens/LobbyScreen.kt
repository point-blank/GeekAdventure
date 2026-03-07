package pl.pointblank.geekadventure.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
        containerColor = Color(0xFF050505), // CIEMNE TŁO
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF050505)) // Ciemne tło
                    .statusBarsPadding() // FIX: Odsuwa cały nagłówek pod systemowy zegar i baterię!
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "GEEK ADVENTURE",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = OrbitronFont,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp, // Lekko powiększony
                    letterSpacing = 6.sp, // Szerszy rozstaw liter wygląda bardziej kinowo
                    style = androidx.compose.ui.text.TextStyle(
                        // Pionowy gradient daje efekt "wypukłego metalu"
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF176), Color(0xFFFF8F00))
                        ),
                        shadow = Shadow(
                            color = Color(0xFFFF8F00), // Mocny pomarańczowy glow
                            offset = Offset(0f, 0f), // Glow ze środka, a nie rzucanie cienia
                            blurRadius = 30f // Zwiększony promień rozmycia - teraz będzie "świecić"
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.clickable { showShop = true }) {
                    UserStatsBar(userStats)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dodajemy holograficzną, neonową linię oddzielającą HUD od listy gier!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF00E5FF).copy(alpha = 0.6f), // Cyan
                                    Color(0xFFFF00FF).copy(alpha = 0.6f), // Magenta
                                    Color.Transparent
                                )
                            )
                        )
                )
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
                    SectionHeader("DLA GEEKÓW", Color(0xFF00E5FF))
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
                item { SectionHeader("DLA GEEKÓW", Color(0xFF00E5FF)) }
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

    val context = LocalContext.current

    // Definiujemy ścięty kształt specjalnie dla Cyberpunka
    val cyberpunkDialogShape = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width - 40f, 0f)
        lineTo(size.width, 40f)
        lineTo(size.width, size.height)
        lineTo(40f, size.height)
        lineTo(0f, size.height - 40f)
        close()
    }

    val dialogShape = if (style == ScenarioStyle.CYBERPUNK) cyberpunkDialogShape else RoundedCornerShape(16.dp)

    val dialogBg = when {
        scenario.isForKids -> Color.White
        style == ScenarioStyle.CYBERPUNK -> Color(0xFF0D0D0D)
        style == ScenarioStyle.FANTASY -> Color(0xFF2B1B17) // Zmienione na ciemniejsze, epickie tło
        style == ScenarioStyle.HORROR -> Color(0xFF050505)
        style == ScenarioStyle.PIRATES -> Color(0xFF002214) // Bardzo ciemna zieleń
        style == ScenarioStyle.WESTERN -> Color(0xFF3E2723)
        style == ScenarioStyle.SUPERHERO -> Color(0xFF1A1A1A)
        else -> Color(0xFF1A1A1A)
    }

    val dialogText = when {
        scenario.isForKids -> Color.Black
        else -> Color.White.copy(alpha = 0.9f)
    }

    val dialogAccent = if (isLocked) Color.Gray else when {
        scenario.isForKids -> Color(0xFFFF4081)
        style == ScenarioStyle.FANTASY -> Color(0xFFE5D5A0) // Złoty
        style == ScenarioStyle.WESTERN -> Color(0xFFFFCC80)
        style == ScenarioStyle.SUPERHERO -> Color.Yellow
        style == ScenarioStyle.PIRATES -> Color(0xFFC2B280) // Stare złoto
        else -> scenario.themeColor
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = dialogShape,
            color = dialogBg,
            border = BorderStroke(2.dp, dialogAccent.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, dialogShape, spotColor = dialogAccent)
        ) {
            Column {
                // SEKCJA 1: Obrazkowy nagłówek z gradientem (Mission Briefing Header)
                if (scenario.backgroundResId != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = scenario.backgroundResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient płynnie łączący obrazek z tłem dialogu
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, dialogBg),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )

                        // Opcjonalnie: ikona scenariusza pośrodku
                        if (scenario.iconRes != null) {
                            Icon(
                                painter = painterResource(id = scenario.iconRes),
                                contentDescription = null,
                                tint = dialogAccent.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // SEKCJA 2: Treść dialogu
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLocked) "ZABLOKOWANE" else scenario.title.uppercase(),
                        color = dialogAccent,
                        fontWeight = FontWeight.Black,
                        fontFamily = font,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mały ozdobny divider
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .background(dialogAccent.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLocked) {
                        Text(
                            text = "Ten scenariusz wymaga statusu Geek Master. Odblokuj pełen potencjał i graj bez limitów!",
                            color = dialogText,
                            fontFamily = font,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        )
                    } else {
                        Text(
                            text = scenario.description,
                            color = dialogText,
                            fontFamily = font,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // SEKCJA 3: Gamingowe Przyciski
                    if (isLocked) {
                        Button(
                            onClick = {
                                context.findActivity()?.let { activity ->
                                    viewModel.buyProduct(activity, pl.pointblank.geekadventure.util.BillingManager.PREMIUM_SUB)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                            shape = dialogShape, // Używamy tego samego kształtu co okno!
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ZDOBĄDŹ PREMIUM", fontFamily = OrbitronFont, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Przyciski akcji ułożone w kolumnę dla lepszej klikalności
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (hasSave) {
                                Button(
                                    onClick = { onStart(scenario, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = dialogAccent, contentColor = dialogBg),
                                    shape = dialogShape,
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("KONTYNUUJ PRZYGODĘ", fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                OutlinedButton(
                                    onClick = { onStart(scenario, false) },
                                    border = BorderStroke(1.dp, dialogAccent),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = dialogAccent),
                                    shape = dialogShape,
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("NOWA GRA", fontFamily = font, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onStart(scenario, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = dialogAccent, contentColor = dialogBg),
                                    shape = dialogShape,
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("ROZPOCZNIJ", fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            TextButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.textButtonColors(contentColor = dialogText.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ANULUJ", fontFamily = font)
                            }
                        }
                    }
                }
            }
        }
    }
}
