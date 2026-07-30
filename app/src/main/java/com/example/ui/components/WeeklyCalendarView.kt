package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dateString: String,      // "yyyy-MM-dd"
    val dayNameArabic: String,   // "السبت"
    val dayNumber: String,       // "30"
    val isToday: Boolean,
    val isSelected: Boolean
)

@Composable
fun WeeklyCalendarView(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var weekOffset by remember { mutableIntStateOf(0) }

    val daysOfWeek = remember(weekOffset, selectedDate) {
        val calendar = Calendar.getInstance()
        // Set to first day of current week (e.g. Saturday or Sunday depending on Arabic locale)
        calendar.firstDayOfWeek = Calendar.SATURDAY
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)

        val todayFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDayName = SimpleDateFormat("EEEE", Locale("ar"))
        val sdfDayNum = SimpleDateFormat("d", Locale.getDefault())

        val days = mutableListOf<CalendarDay>()
        for (i in 0..6) {
            val dString = sdfDate.format(calendar.time)
            val name = sdfDayName.format(calendar.time)
            val num = sdfDayNum.format(calendar.time)
            val isToday = dString == todayFormatted
            val isSelected = dString == selectedDate

            days.add(
                CalendarDay(
                    dateString = dString,
                    dayNameArabic = name,
                    dayNumber = num,
                    isToday = isToday,
                    isSelected = isSelected
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    // Format week header label e.g. "26 يوليو - 1 أغسطس"
    val weekRangeHeader = remember(daysOfWeek) {
        if (daysOfWeek.isNotEmpty()) {
            val first = daysOfWeek.first().dateString
            val last = daysOfWeek.last().dateString
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfHeader = SimpleDateFormat("d MMMM", Locale("ar"))
            try {
                val d1 = sdf.parse(first)
                val d2 = sdf.parse(last)
                if (d1 != null && d2 != null) {
                    "${sdfHeader.format(d1)} - ${sdfHeader.format(d2)}"
                } else ""
            } catch (e: Exception) {
                ""
            }
        } else ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
            .testTag("weekly_calendar_view"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Calendar Header with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = BentoPrimaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الجدول الأسبوعي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (weekRangeHeader.isNotEmpty()) {
                            Text(
                                text = weekRangeHeader,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (weekOffset != 0) {
                        TextButton(
                            onClick = {
                                weekOffset = 0
                                onDateSelected(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                            },
                            modifier = Modifier.testTag("calendar_today_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BentoPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اليوم", style = MaterialTheme.typography.labelMedium, color = BentoPrimary)
                        }
                    }

                    IconButton(
                        onClick = { weekOffset-- },
                        modifier = Modifier.size(32.dp).testTag("calendar_prev_week")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "الأسبوع السابق",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { weekOffset++ },
                        modifier = Modifier.size(32.dp).testTag("calendar_next_week")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "الأسبوع التالي",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Days Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysOfWeek.forEach { day ->
                    val isSel = day.isSelected
                    val isToday = day.isToday

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .background(
                                color = if (isSel) BentoPrimary else if (isToday) BentoPrimaryContainer.copy(alpha = 0.4f) else BentoSecondaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = if (isSel) 2.dp else if (isToday) 1.5.dp else 0.dp,
                                color = if (isSel) BentoPrimary else if (isToday) BentoPrimary.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onDateSelected(day.dateString) }
                            .padding(vertical = 10.dp, horizontal = 2.dp)
                            .testTag("calendar_day_${day.dateString}")
                    ) {
                        Text(
                            text = day.dayNameArabic.take(4), // Short day name e.g. "السبت"
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = day.dayNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Visual indicator dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (isSel) Color.White else if (isToday) BentoPrimary else BentoBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
