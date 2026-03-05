package pl.pointblank.geekadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.pointblank.geekadventure.data.ScenarioRepository
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.viewmodel.GameViewModel
import pl.pointblank.geekadventure.ui.components.LustrousScenarioCard
import pl.pointblank.geekadventure.data.local.UserStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(viewModel: GameViewModel, onScenarioSelected: (Scenario, Boolean, Boolean) -> Unit) {
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
                            style = MaterialTheme.typography.headlineMedium
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
                    fontWeight = FontWeight.Light
                )
            }
            items(ScenarioRepository.scenarios) { scenario ->
                Box {
                    LustrousScenarioCard(
                        scenario = scenario, 
                        onClick = { 
                            selectedScenario = scenario
                            showDialog = true
                        }
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
                                Text("PREMIUM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShop) {
        ShopDialog(viewModel = viewModel, onDismiss = { showShop = false })
    }

    if (showDialog && selectedScenario != null) {
        val scenario = selectedScenario!!
        val isLocked = scenario.isPremium && userStats?.isPremiumUser != true
        
        val style = scenario.visualStyle
        
        val dialogBg = when(style) {
            ScenarioStyle.CYBERPUNK -> Color(0xFF0D0D0D)
            ScenarioStyle.FANTASY -> Color(0xFFF5E6D3)
            ScenarioStyle.HORROR -> Color(0xFF050505)
            else -> MaterialTheme.colorScheme.surface
        }
        
        val dialogText = if (style == ScenarioStyle.FANTASY) Color(0xFF4B2C20) else Color.White
        val dialogAccent = if (isLocked) Color.Gray else scenario.themeColor

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = dialogBg,
            shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp),
            title = { 
                Text(
                    if (isLocked) "SCENARIUSZ ZABLOKOWANY" else scenario.title.uppercase(), 
                    color = dialogAccent, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (style == ScenarioStyle.CYBERPUNK) 2.sp else 0.sp
                ) 
            },
            text = {
                Column {
                    if (isLocked) {
                        Text(
                            "Ten scenariusz jest dostępny tylko dla subskrybentów Geek Master. Wesprzyj nas i graj bez limitów!",
                            color = dialogText,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(scenario.description, color = dialogText)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = dialogAccent.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!isLocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generuj obrazy AI do scen", color = dialogText, modifier = Modifier.weight(1f))
                            Switch(
                                checked = enableImages,
                                onCheckedChange = { enableImages = it },
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
                    val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity
                    Button(
                        onClick = { viewModel.buyProduct(activity, pl.pointblank.geekadventure.util.BillingManager.PREMIUM_SUB) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                    ) {
                        Text("Zasubskrybuj")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (hasSave) {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    onScenarioSelected(scenario, enableImages, true)
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = dialogAccent)
                            ) {
                                Text("Kontynuuj")
                            }
                        }
                        Button(
                            onClick = {
                                showDialog = false
                                onScenarioSelected(scenario, enableImages, false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dialogAccent, contentColor = Color.White),
                            shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
                        ) {
                            Text(if (hasSave) "Nowa gra" else "Rozpocznij przygodę")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = dialogText.copy(alpha = 0.6f))
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
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
            value = "${stats?.actionPoints ?: 0}/20",
            color = Color(0xFFFFD600)
        )
        
        VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        
        StatItem(
            icon = Icons.Default.History,
            label = "Kryształy",
            value = "${stats?.chronocrystals ?: 0}",
            color = Color(0xFF00E5FF)
        )

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
