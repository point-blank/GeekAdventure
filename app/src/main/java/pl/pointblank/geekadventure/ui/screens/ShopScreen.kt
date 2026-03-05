package pl.pointblank.geekadventure.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
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
import pl.pointblank.geekadventure.util.BillingManager
import pl.pointblank.geekadventure.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDialog(viewModel: GameViewModel, onDismiss: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val isAdLoaded by viewModel.isAdLoaded.collectAsState()
    val activity = LocalContext.current as Activity

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "SKLEP GEEKA",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // SEKCJA REKLAM
                item {
                    Text("DARMOWE ZASOBY", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                }
                item {
                    ShopItemRow(
                        icon = Icons.Default.PlayCircle,
                        title = "Obejrzyj reklamę",
                        description = "+1 pkt Energii",
                        price = "ZA DARMO",
                        color = Color(0xFF4CAF50),
                        enabled = isAdLoaded,
                        onClick = {
                            viewModel.showRewardedAd(activity)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // SEKCJA MIKROPŁATNOŚCI
                item {
                    Text("DOŁADOWANIA", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                }
                
                // Szukamy produktów w liście z BillingManagera
                val energyPack = products.find { it.productId == BillingManager.ENERGY_PACK }
                val crystalPack = products.find { it.productId == BillingManager.CRYSTAL_PACK }
                val premiumSub = products.find { it.productId == BillingManager.PREMIUM_SUB }

                item {
                    ShopItemRow(
                        icon = Icons.Default.Bolt,
                        title = "Pakiet Energii",
                        description = "Pełne odnowienie (20 pkt)",
                        price = energyPack?.oneTimePurchaseOfferDetails?.formattedPrice ?: "-- zł",
                        color = Color(0xFFFFD600),
                        onClick = { viewModel.buyProduct(activity, BillingManager.ENERGY_PACK) }
                    )
                }

                item {
                    ShopItemRow(
                        icon = Icons.Default.History,
                        title = "5 Chronokryształów",
                        description = "Cofaj czas częściej",
                        price = crystalPack?.oneTimePurchaseOfferDetails?.formattedPrice ?: "-- zł",
                        color = Color(0xFF00E5FF),
                        onClick = { viewModel.buyProduct(activity, BillingManager.CRYSTAL_PACK) }
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
                        price = premiumSub?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice?.let { "$it / msc" } ?: "-- zł",
                        color = Color(0xFFFFD700),
                        onClick = { viewModel.buyProduct(activity, BillingManager.PREMIUM_SUB) }
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
