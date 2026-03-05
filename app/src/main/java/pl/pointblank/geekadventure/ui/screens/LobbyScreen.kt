package pl.pointblank.geekadventure.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.pointblank.geekadventure.data.ScenarioRepository
import pl.pointblank.geekadventure.model.Scenario
import pl.pointblank.geekadventure.model.ScenarioStyle
import pl.pointblank.geekadventure.viewmodel.GameViewModel
import pl.pointblank.geekadventure.ui.components.LustrousScenarioCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(viewModel: GameViewModel, onScenarioSelected: (Scenario, Boolean, Boolean) -> Unit) {
    var selectedScenario by remember { mutableStateOf<Scenario?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var enableImages by remember { mutableStateOf(false) }
    var hasSave by remember { mutableStateOf(false) }

    LaunchedEffect(selectedScenario) {
        selectedScenario?.let {
            hasSave = viewModel.hasSavedGame(it.id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "GEEK ADVENTURE",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineMedium
                    ) 
                }
            )
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
                LustrousScenarioCard(
                    scenario = scenario, 
                    onClick = { 
                        selectedScenario = scenario
                        showDialog = true
                    }
                )
            }
        }
    }

    if (showDialog && selectedScenario != null) {
        val scenario = selectedScenario!!
        val style = scenario.visualStyle
        
        val dialogBg = when(style) {
            ScenarioStyle.CYBERPUNK -> Color(0xFF0D0D0D)
            ScenarioStyle.FANTASY -> Color(0xFFF5E6D3)
            ScenarioStyle.HORROR -> Color(0xFF050505)
            else -> MaterialTheme.colorScheme.surface
        }
        
        val dialogText = if (style == ScenarioStyle.FANTASY) Color(0xFF4B2C20) else Color.White
        val dialogAccent = scenario.themeColor

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = dialogBg,
            shape = if (style == ScenarioStyle.CYBERPUNK) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp),
            title = { 
                Text(
                    scenario.title.uppercase(), 
                    color = dialogAccent, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (style == ScenarioStyle.CYBERPUNK) 2.sp else 0.sp
                ) 
            },
            text = {
                Column {
                    Text(scenario.description, color = dialogText)
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = dialogAccent.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
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
            },
            confirmButton = {
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
