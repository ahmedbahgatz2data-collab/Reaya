package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoHeroCard
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.viewmodel.MedicationViewModel

@Composable
fun VoiceSettingsScreen(
    viewModel: MedicationViewModel
) {
    val context = LocalContext.current
    val voiceStyle by viewModel.voiceStyle.collectAsStateWithLifecycle()
    val alertMode by viewModel.alertMode.collectAsStateWithLifecycle()
    val ttsRate by viewModel.ttsRate.collectAsStateWithLifecycle()
    val ttsPitch by viewModel.ttsPitch.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val alertModes = listOf(
        Triple("BOTH", "الناطق الصوتي + نغمة الرنين معاً", Icons.Default.NotificationsActive) to "تشغيل نغمة المنبه أولاً متبوعة بنطق التذكير بصوت عربي فصيح.",
        Triple("VOICE_ONLY", "الناطق الصوتي فقط", Icons.Default.RecordVoiceOver) to "قراءة تفاصيل الدواء والجرعة والتعليمات بالصوت العربي بدون رنين.",
        Triple("RINGTONE_ONLY", "نغمة الرنين / المنبه فقط", Icons.Default.MusicNote) to "إصدار صوت النغمة والتنبيه الصوتي التقليدي عند حلول الموعد.",
        Triple("VIBRATE", "اهتزاز صامت", Icons.Default.Vibration) to "التنبيه بالاهتزاز فقط دون إصدار أصوات."
    )

    val stylesList = listOf(
        "FRIENDLY" to "أسلوب حميمي ومشجع" to "تنبيه لطيف ومبهج يناسب كبار السن والأطفال لرفع المعنويات.",
        "DIRECT" to "أسلوب مباشر ودقيق" to "تنبيه موجز يتضمن اسم الدواء والجرعة مباشرة.",
        "EMERGENCY" to "أسلوب عاجل ومهم" to "تنبيه بصوت قوي ومشدد للجرعات التي لا تحتمل التأخير."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("voice_settings_screen")
    ) {
        // App Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "إعدادات التنبيه والناطق الصوتي",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "اختر بين الناطق الصوتي الفصيح ونغمة الرنين وسرعة النطق",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Surface(
                shape = CircleShape,
                color = BentoPrimaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dark Mode Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                .testTag("dark_mode_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF2C2834) else BentoPrimaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BentoPrimary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isDarkMode) "الوضع الليلي نشط (Dark Mode)" else "الوضع النهاري (Light Mode)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "توفير استهلاك البطارية وتحسين مريحة العين أثناء الاستخدام الليلي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BentoPrimary,
                        checkedTrackColor = BentoPrimaryContainer
                    ),
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }
        }

        // Hero Bento Card for Voice Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_hero_bento"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BentoHeroCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BentoOnPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "المحرك الصوتي والطلاقة العربية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnPrimaryContainer
                        )
                    }

                    Surface(
                        color = BentoOnPrimaryContainer.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "مُحسّن بطلاقة",
                            color = BentoOnPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "تم تحسين الناطق الصوتي لنطق الأرقام والمواعيد والجرعات باللغة العربية الفصحى مع التشكيل الطبيعي وعلامات الوقف.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoOnPrimaryContainer.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val sampleText = "تنبيه صحي: حان الآن موعد تناول دواء بندول فورت، الجرعة قرص واحد بعد الطعام في تمام الساعة الثامنة صباحاً."
                            viewModel.playVoiceReminder(context, sampleText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoOnPrimaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("test_voice_sample_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تجربة التنبيه الحالية", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.playTestRingtone(context) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = BentoOnPrimaryContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نغمة فقط", color = BentoOnPrimaryContainer)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: ALERT MODE SELECTION (طريقة التنبيه)
        Text(
            text = "اختر طريقة أسلوب التنبيه عند حلول الموعد:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        alertModes.forEach { (modeInfo, description) ->
            val modeKey = modeInfo.first
            val modeTitle = modeInfo.second
            val modeIcon = modeInfo.third
            val isSelected = alertMode == modeKey

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BentoPrimary else BentoBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { viewModel.setAlertMode(modeKey) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) BentoPrimaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.setAlertMode(modeKey) },
                        colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) BentoPrimary else BentoSecondaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else BentoPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = modeTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: VOICE PERSONALITY & TONE
        Text(
            text = "اختر شخصية ونبرة الناطق الصوتي:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        stylesList.forEach { (pairData, description) ->
            val key = pairData.first
            val title = pairData.second
            val isSelected = voiceStyle == key

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BentoPrimary else BentoBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable {
                        viewModel.updateVoiceSettings(key, ttsRate, ttsPitch)
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) BentoPrimaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.updateVoiceSettings(key, ttsRate, ttsPitch) },
                        colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: PITCH & SPEED CONTROLS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "سرعة وطبقة نطق الكلام",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "سرعة الكلام بالطريقة الطبيعية: ${String.format("%.2f", ttsRate)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = ttsRate,
                    onValueChange = { newRate ->
                        viewModel.updateVoiceSettings(voiceStyle, newRate, ttsPitch)
                    },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = BentoPrimary,
                        activeTrackColor = BentoPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "درجة طبقة الصوت (Pitch): ${String.format("%.2f", ttsPitch)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = ttsPitch,
                    onValueChange = { newPitch ->
                        viewModel.updateVoiceSettings(voiceStyle, ttsRate, newPitch)
                    },
                    valueRange = 0.7f..1.3f,
                    colors = SliderDefaults.colors(
                        thumbColor = BentoPrimary,
                        activeTrackColor = BentoPrimary
                    )
                )
            }
        }
    }
}
