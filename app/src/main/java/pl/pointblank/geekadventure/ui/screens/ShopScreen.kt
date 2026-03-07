package pl.pointblank.geekadventure.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pl.pointblank.geekadventure.util.BillingManager
import pl.pointblank.geekadventure.viewmodel.GameViewModel
import pl.pointblank.geekadventure.ui.components.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDialog(viewModel: GameViewModel, onDismiss: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val isAdLoaded by viewModel.isAdLoaded.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val context = LocalContext.current

    // Stan przechowujący tekst odliczania
    var timerText by remember { mutableStateOf("") }

    // Efekt odliczania
    LaunchedEffect(userStats) {
        while (true) {
            val stats = userStats ?: break
            if (stats.actionPoints >= 10) {
                timerText = "PEŁNA"
                break
            }

            val now = System.currentTimeMillis()
            val nextRefillTime = stats.lastRefillTime + (30 * 60 * 1000) // 30 minut
            val remainingMillis = nextRefillTime - now

            if (remainingMillis <= 0) {
                // Jeśli czas minął, wymuś odświeżenie energii w ViewModelu
                viewModel.onResume()
                timerText = "00:00"
            } else {
                val minutes = (remainingMillis / 1000) / 60
                val seconds = (remainingMillis / 1000) % 60
                timerText = String.format("%02d:%02d", minutes, seconds)
            }
            delay(1000) // Odczekaj sekundę
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SKLEP GEEKA",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                // Mały wskaźnik energii w tytule
                Text(
                    "${userStats?.actionPoints ?: 0}/10 ⚡",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontFamily = pl.pointblank.geekadventure.ui.theme.OrbitronFont
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("DARMOWE ZASOBY", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        if (timerText.isNotEmpty() && timerText != "PEŁNA") {
                            Text(
                                "Następna: $timerText",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
                item {
                    ShopItemRow(
                        icon = Icons.Default.PlayCircle,
                        title = "Obejrzyj reklamę",
                        description = "+3 pkt Energii",
                        price = "ZA DARMO",
                        color = Color(0xFF4CAF50),
                        enabled = isAdLoaded,
                        onClick = {
                            context.findActivity()?.let { activity ->
                                viewModel.showRewardedAd(activity)
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text("DOŁADOWANIA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                }
                
                val energyPack = products.find { it.productId == BillingManager.ENERGY_PACK }
                val crystalPack = products.find { it.productId == BillingManager.CRYSTAL_PACK }
                val premiumSub = products.find { it.productId == BillingManager.PREMIUM_SUB }

                item {
                    ShopItemRow(
                        icon = Icons.Default.Bolt,
                        title = "Pakiet Energii",
                        description = "Pełne odnowienie (10 pkt)",
                        price = energyPack?.oneTimePurchaseOfferDetails?.formattedPrice ?: "Ładowanie...",
                        color = Color(0xFFFFD600),
                        onClick = { 
                            context.findActivity()?.let { activity ->
                                viewModel.buyProduct(activity, BillingManager.ENERGY_PACK) 
                            }
                        }
                    )
                }

                item {
                    ShopItemRow(
                        icon = Icons.Default.History,
                        title = "5 Chronokryształów",
                        description = "Cofaj czas częściej",
                        price = crystalPack?.oneTimePurchaseOfferDetails?.formattedPrice ?: "Ładowanie...",
                        color = Color(0xFF00E5FF),
                        onClick = { 
                            context.findActivity()?.let { activity ->
                                viewModel.buyProduct(activity, BillingManager.CRYSTAL_PACK) 
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text("SUBSKRYPCJA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                }

                item {
                    ShopItemRow(
                        icon = Icons.Default.Star,
                        title = "Geek Master Premium",
                        description = "Nielimitowana energia + Gemini Pro",
                        price = premiumSub?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice?.let { "$it / msc" } ?: "Ładowanie...",
                        color = Color(0xFFFFD700),
                        onClick = { 
                            context.findActivity()?.let { activity ->
                                viewModel.buyProduct(activity, BillingManager.PREMIUM_SUB) 
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ZAMKNIJ", color = Color.White)
            }
        }
    )
}

@Composable
fun ShopItemRow(
    icon: ImageVector,
    title: String,
    description: String,
    price: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) color else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else Color.Gray
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                price,
                fontWeight = FontWeight.Black,
                color = if (enabled) color else Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
