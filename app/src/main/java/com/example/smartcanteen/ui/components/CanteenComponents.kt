package com.example.smartcanteen.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.data.Sale
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, textColor: Color = Color.Unspecified) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.5f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AnimatedSalesChart(sales: List<Sale>) {
    val daySdf = SimpleDateFormat("EEE", Locale.getDefault())
    val chartData = remember(sales) {
        val dataList = mutableListOf<Triple<Double, String, String>>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_YEAR, -i)

            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + (24 * 60 * 60 * 1000)

            val daySales = sales.filter { it.timestamp in dayStart until dayEnd }
            val totalForDay = daySales.sumOf { it.totalPrice }
            val topProduct = daySales.groupBy { it.itemName }
                .maxByOrNull { entry -> entry.value.sumOf { s -> s.quantitySold } }?.key ?: ""
            val dayLabel = daySdf.format(Date(dayStart))

            dataList.add(Triple(totalForDay, topProduct, dayLabel))
        }
        dataList.toList()
    }

    val maxVal = (chartData.map { it.first }.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            chartData.forEach { (revenue, topProduct, dayLabel) ->
                val heightPercent by animateFloatAsState(
                    targetValue = (revenue / maxVal).toFloat().coerceIn(0.05f, 1f),
                    animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "chart"
                )

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier.height(50.dp).fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (topProduct.isNotEmpty()) {
                            Text(
                                text = topProduct,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .rotate(-45f)
                                    .padding(bottom = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight(heightPercent)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
