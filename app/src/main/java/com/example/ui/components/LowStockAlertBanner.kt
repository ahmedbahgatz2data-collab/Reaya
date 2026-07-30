package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication

@Composable
fun LowStockAlertBanner(
    lowStockMeds: List<Medication>,
    onRefillClicked: (Medication) -> Unit
) {
    if (lowStockMeds.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("low_stock_banner_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        lowStockMeds.forEach { med ->
            val timesCount = med.timesOfDay.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
            val dailyDoses = if (timesCount > 0) timesCount else 1
            val daysRemaining = if (dailyDoses > 0) med.stockCount / dailyDoses else med.stockCount

            PulsingCardWrapper(
                modifier = Modifier.fillMaxWidth(),
                glowColor = Color(0xFFE53935),
                cornerRadius = 20.dp
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_card_${med.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE53935),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "تنبيه المخزون",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تنبيه نفاذ مخزون: ${med.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                                Text(
                                    text = if (daysRemaining <= 0) "المخزون نفذ تماماً! يرجى الشراء فوراً."
                                    else "متبقي ${med.stockCount} جرعة تكفي لـ $daysRemaining أيام فقط (معدل $dailyDoses/يوم).",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4E342E)
                                )
                            }
                        }

                        Button(
                            onClick = { onRefillClicked(med) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .testTag("refill_stock_btn_${med.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+30 تعبئة", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
