package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.IntakeLog
import com.example.data.model.Medication
import com.example.ui.components.LowStockAlertBanner
import com.example.ui.components.PulsingCardWrapper
import com.example.ui.components.WeeklyCalendarView
import com.example.ui.components.pulseEffect
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoHeroCard
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.PendingPurple
import com.example.ui.theme.SkippedAmber
import com.example.ui.theme.TakenGreen
import com.example.ui.viewmodel.MedicationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MedicationViewModel,
    onNavigateToCabinet: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val allMeds by viewModel.allMedications.collectAsStateWithLifecycle()
    val aiScript by viewModel.aiCustomScript.collectAsStateWithLifecycle()
    val medicationTips by viewModel.medicationTips.collectAsStateWithLifecycle()
    val alertMode by viewModel.alertMode.collectAsStateWithLifecycle()

    val totalDoses = todayLogs.size
    val takenDoses = todayLogs.count { it.status == "TAKEN" }
    val adherencePercent = if (totalDoses > 0) (takenDoses.toFloat() / totalDoses * 100).toInt() else 0

    val nextDose = todayLogs.firstOrNull { it.status == "PENDING" || it.status == "SNOOZED" }
    val nextMed = if (nextDose != null) allMeds.firstOrNull { it.id == nextDose.medicationId } else null

    val lowStockMeds = remember(allMeds) {
        allMeds.filter { med ->
            val timesCount = med.timesOfDay.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
            val dailyDoses = if (timesCount > 0) timesCount else 1
            val daysRemaining = if (dailyDoses > 0) med.stockCount / dailyDoses else med.stockCount
            med.stockCount <= med.lowStockThreshold || daysRemaining <= 3
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("home_screen_column")
    ) {
        // Bento App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "صباح الخير، أحمد 👋",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimary
                )
                Text(
                    text = "تذكير الدواء",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = BentoPrimaryContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            com.example.util.ScheduleShareHelper.shareSchedule(
                                context = context,
                                medications = allMeds,
                                logs = todayLogs,
                                date = selectedDate
                            )
                        }
                        .testTag("share_schedule_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة الجدول الدوائي",
                        tint = BentoPrimary,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = BentoPrimaryContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onNavigateToVoiceSettings() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "الملف الشخصي",
                        tint = BentoOnPrimaryContainer,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }
            }
        }

        // Weekly Visual Schedule Calendar
        WeeklyCalendarView(
            selectedDate = selectedDate,
            onDateSelected = { newDate -> viewModel.selectDate(newDate) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Smart Low-Stock Alert Banner (if any)
        LowStockAlertBanner(
            lowStockMeds = lowStockMeds,
            onRefillClicked = { med -> viewModel.refillStock(med.id, 30) }
        )

        // AI Script Notification Banner (if generated)
        AnimatedVisibility(visible = aiScript != null) {
            aiScript?.let { script ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("ai_script_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoPrimaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BentoPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "التنبيه الصوتي المخصص بالذكاء الاصطناعي:",
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = script,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoOnPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.playVoiceReminder(context, script) },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استماع للتنبيه")
                            }
                            OutlinedButton(
                                onClick = { viewModel.clearAiCustomScript() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إغلاق")
                            }
                        }
                    }
                }
            }
        }

        // Medication Tips Banner
        AnimatedVisibility(visible = medicationTips != null) {
            medicationTips?.let { tips ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSecondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = BentoPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرشادات واستشارات حية حول الدواء:",
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tips,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearMedicationTips() }) {
                            Text("تم الاطلاع")
                        }
                    }
                }
            }
        }

        // ---------------- BENTO GRID CONTAINER ----------------
        // 1. Bento Hero Card (Next Scheduled Dose)
        PulsingCardWrapper(
            modifier = Modifier.fillMaxWidth(),
            glowColor = BentoPrimary,
            cornerRadius = 28.dp
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bento_hero_next_dose"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoHeroCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (nextDose?.status == "SNOOZED") "جرعة مؤجلة (تنبيه القادم)" else "الجرعة القادمة",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoOnPrimaryContainer.copy(alpha = 0.8f)
                                )
                                if (nextDose?.status == "SNOOZED") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SkippedAmber,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = "مؤجل",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = nextDose?.scheduledTime ?: "لا يوجد",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoOnPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoPrimaryContainer,
                            modifier = Modifier
                                .size(44.dp)
                                .pulseEffect(enabled = nextDose != null)
                        ) {
                            IconButton(
                                onClick = {
                                    if (nextDose != null) {
                                        viewModel.playVoiceReminder(
                                            context = context,
                                            text = nextDose.voicePromptText,
                                            voiceNotePath = nextMed?.voiceNotePath
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "تنبيه",
                                    tint = BentoOnPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (nextDose != null) {
                        Text(
                            text = "${nextDose.medicationName} (${nextDose.dosage})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnPrimaryContainer
                        )
                        val foodText = when (nextMed?.foodInstruction) {
                            "AFTER_MEAL" -> "بعد الأكل بـ 30 دقيقة"
                            "BEFORE_MEAL" -> "قبل الأكل"
                            "WITH_MEAL" -> "مع الوجبة"
                            "EMPTY_STOMACH" -> "على الريق"
                            else -> "حسب التعليمات"
                        }
                        Text(
                            text = foodText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoOnPrimaryContainer.copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.markDoseTaken(nextDose, context) },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoOnPrimaryContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .pulseEffect(enabled = true)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تم التناول", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { viewModel.snoozeDose(nextDose, 60, context) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("تأجيل ساعة", fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    } else {
                        Text(
                            text = "ممتاز! لقد أكملت جميع الجرعات المجدولة لهذا اليوم.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Bento Grid Row (Col 1: Adherence, Col 2: Voice Alert)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bento Card Left: Daily Adherence Ring
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                    .testTag("bento_daily_adherence_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { if (totalDoses > 0) takenDoses.toFloat() / totalDoses else 0f },
                            modifier = Modifier.fillMaxSize(),
                            color = BentoPrimary,
                            strokeWidth = 6.dp,
                            trackColor = BentoPrimaryContainer
                        )
                        Text(
                            text = "$adherencePercent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "الالتزام اليومي",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }
            }

            // Bento Card Right: Smart Voice Status
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clickable { onNavigateToVoiceSettings() }
                    .testTag("bento_voice_alert_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSecondaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Surface(
                            shape = CircleShape,
                            color = BentoPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = BentoPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "تنبيه ذكي صوتي",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnPrimaryContainer
                        )
                        val modeText = when (alertMode) {
                            "BOTH" -> "مفعّل: صوت عربي + رنين"
                            "VOICE_ONLY" -> "مفعّل: ناطق صوتي فقط"
                            "RINGTONE_ONLY" -> "مفعّل: نغمة رنين فقط"
                            "VIBRATE" -> "مفعّل: اهتزاز صامت"
                            else -> "مفعّل: نطق باللغة العربية"
                        }
                        Text(
                            text = modeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Bento Card: Schedule Timeline List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                .testTag("bento_schedule_list_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الجدول الزمني للجرعات اليومية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(onClick = onNavigateToCabinet) {
                        Text("الخزانة", color = BentoPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (todayLogs.isEmpty()) {
                    Text(
                        text = "لا توجد جرعات مجدولة لهذا التاريخ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        todayLogs.forEach { log ->
                            val med = allMeds.firstOrNull { it.id == log.medicationId }
                            BentoScheduleLogRow(
                                log = log,
                                medication = med,
                                onMarkTaken = { viewModel.markDoseTaken(log, context) },
                                onSnooze = { viewModel.snoozeDose(log, 60, context) },
                                onPlayVoice = { viewModel.playVoiceReminder(context, log.voicePromptText, med?.voiceNotePath) },
                                onGenerateAiScript = {
                                    if (med != null) {
                                        viewModel.generateSmartAiScript(med, log.scheduledTime)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelectorHeader(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar"))

    val currentDate = try {
        sdf.parse(selectedDate) ?: Date()
    } catch (e: Exception) {
        Date()
    }

    val cal = Calendar.getInstance()
    cal.time = currentDate

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BentoSecondaryContainer)
            .padding(vertical = 4.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                cal.add(Calendar.DAY_OF_MONTH, -1)
                onDateSelected(sdf.format(cal.time))
            },
            modifier = Modifier.size(36.dp).testTag("prev_day_button")
        ) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "اليوم السابق", tint = BentoPrimary)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = displayFormat.format(currentDate),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BentoPrimary
            )
        }

        IconButton(
            onClick = {
                cal.add(Calendar.DAY_OF_MONTH, 1)
                onDateSelected(sdf.format(cal.time))
            },
            modifier = Modifier.size(36.dp).testTag("next_day_button")
        ) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "اليوم التالي", tint = BentoPrimary)
        }
    }
}

@Composable
fun BentoScheduleLogRow(
    log: IntakeLog,
    medication: Medication?,
    onMarkTaken: () -> Unit,
    onSnooze: () -> Unit,
    onPlayVoice: () -> Unit,
    onGenerateAiScript: () -> Unit
) {
    val isTaken = log.status == "TAKEN"
    val isSkipped = log.status == "SKIPPED"
    val isSnoozed = log.status == "SNOOZED"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isTaken) TakenGreen.copy(alpha = 0.08f)
                else if (isSnoozed) SkippedAmber.copy(alpha = 0.12f)
                else BentoPrimaryContainer.copy(alpha = 0.2f)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isTaken) TakenGreen else if (isSnoozed || isSkipped) SkippedAmber else BentoPrimary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.medicationName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSnoozed) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(تم التأجيل)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkippedAmber
                        )
                    }
                }
                Text(
                    text = "${log.dosage} • الموعد: ${log.scheduledTime}" + if (isSnoozed) " (تنبيه بعد ساعة)" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onPlayVoice, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (medication?.voiceNotePath != null) Icons.Default.Mic else Icons.Default.VolumeUp,
                    contentDescription = if (medication?.voiceNotePath != null) "تشغيل التسجيل الصوتي الخاص" else "قراءة التنبيه",
                    tint = BentoPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onGenerateAiScript, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "ذكاء اصطناعي",
                    tint = BentoPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isTaken) {
                IconButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("snooze_log_btn_${log.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "تأجيل ساعة واحدة",
                        tint = SkippedAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onMarkTaken, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تم التناول",
                        tint = TakenGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
