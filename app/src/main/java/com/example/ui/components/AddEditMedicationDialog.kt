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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.platform.LocalContext
import com.example.audio.AudioRecorderHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Medication
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationDialog(
    medication: Medication?,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit,
    onDelete: ((Medication) -> Unit)? = null
) {
    var name by remember { mutableStateOf(medication?.name ?: "") }
    var dosage by remember { mutableStateOf(medication?.dosage ?: "1 قرص") }
    var form by remember { mutableStateOf(medication?.form ?: "PILL") }
    var foodInstruction by remember { mutableStateOf(medication?.foodInstruction ?: "AFTER_MEAL") }
    var stockCountText by remember { mutableStateOf((medication?.stockCount ?: 30).toString()) }
    var lowStockThresholdText by remember { mutableStateOf((medication?.lowStockThreshold ?: 5).toString()) }
    var notes by remember { mutableStateOf(medication?.notes ?: "") }
    var selectedColor by remember { mutableStateOf(medication?.colorHex ?: "#00897B") }
    val context = LocalContext.current
    var voiceNotePath by remember { mutableStateOf(medication?.voiceNotePath) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    val audioRecorder = remember { AudioRecorderHelper(context) }

    val timesList = remember {
        val initialTimes = medication?.timesOfDay?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        mutableStateListOf<String>().apply {
            if (!initialTimes.isNullOrEmpty()) {
                addAll(initialTimes)
            } else {
                addAll(listOf("08:00", "20:00"))
            }
        }
    }

    val formOptions = listOf(
        "PILL" to "قرص / كبسولة",
        "SYRUP" to "شراب مائي",
        "INJECTION" to "حقنة طبية",
        "DROPS" to "قطرة",
        "INHALER" to "بخاخ استنشاق"
    )

    val foodOptions = listOf(
        "AFTER_MEAL" to "بعد الأكل",
        "BEFORE_MEAL" to "قبل الأكل",
        "WITH_MEAL" to "مع الأكل",
        "EMPTY_STOMACH" to "على معدة فارغة",
        "ANYTIME" to "دون شروط"
    )

    val colorList = listOf("#00897B", "#1E88E5", "#FF7043", "#8E24AA", "#43A047", "#D81B60")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_edit_med_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BentoPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (medication == null) "إضافة دواء جديد" else "تعديل تفاصيل الدواء",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medication Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الدواء (مثال: بندول 500 ملغ)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dosage & Stock Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("الجرعة (مثال: قرص واحد)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("med_dosage_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockCountText,
                        onValueChange = { stockCountText = it },
                        label = { Text("المخزون المتوفر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("med_stock_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Form Dropdown
                Text(
                    text = "الشكل الدوائي:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                var formExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = formExpanded,
                    onExpandedChange = { formExpanded = !formExpanded }
                ) {
                    OutlinedTextField(
                        value = formOptions.firstOrNull { it.first == form }?.second ?: "قرص",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("med_form_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = formExpanded,
                        onDismissRequest = { formExpanded = false }
                    ) {
                        formOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                    form = option.first
                                    formExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Food Instruction Dropdown
                Text(
                    text = "تعليمات الوجبة:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                var foodExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = foodExpanded,
                    onExpandedChange = { foodExpanded = !foodExpanded }
                ) {
                    OutlinedTextField(
                        value = foodOptions.firstOrNull { it.first == foodInstruction }?.second ?: "بعد الأكل",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = foodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("med_food_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = foodExpanded,
                        onDismissRequest = { foodExpanded = false }
                    ) {
                        foodOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                    foodInstruction = option.first
                                    foodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Times of Day Setup
                Text(
                    text = "أوقات التناول في اليوم (HH:mm):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                timesList.forEachIndexed { index, timeVal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = timeVal,
                            onValueChange = { newTime ->
                                timesList[index] = newTime
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        if (timesList.size > 1) {
                            IconButton(onClick = { timesList.removeAt(index) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف الوقت",
                                    tint = Color.Red.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { timesList.add("12:00") },
                    modifier = Modifier.testTag("add_time_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة موعد جرعة جديد")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color Selection Palette
                Text(
                    text = "لون التمييز في الجدول:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorList.forEach { hex ->
                        val colorVal = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice Note Recording Section
                Text(
                    text = "الملاحظة الصوتية الخاصة بالدواء (التنبيه الصوتي):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سجّل صوتك لتنبيهك عند وقت الجرعة بدل الصوت الآلي",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoBorder, RoundedCornerShape(16.dp))
                        .testTag("med_voice_note_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoPrimaryContainer.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = BentoPrimary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (voiceNotePath != null && java.io.File(voiceNotePath!!).exists()) "يوجد تسجيل صوتي مخصص" else "لا يوجد تسجيل صوتي مخصص",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (voiceNotePath != null && java.io.File(voiceNotePath!!).exists()) {
                                IconButton(
                                    onClick = {
                                        audioRecorder.stopPlayback()
                                        audioRecorder.stopRecording()
                                        isPlayingAudio = false
                                        isRecording = false
                                        try {
                                            java.io.File(voiceNotePath!!).delete()
                                        } catch (_: Exception) {}
                                        voiceNotePath = null
                                    },
                                    modifier = Modifier.testTag("delete_voice_note_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "حذف التسجيل الصوتي",
                                        tint = Color.Red.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isRecording) {
                                Button(
                                    onClick = {
                                        val path = audioRecorder.stopRecording()
                                        isRecording = false
                                        if (path != null) {
                                            voiceNotePath = path
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("stop_recording_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إيقاف التسجيل", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        audioRecorder.stopPlayback()
                                        isPlayingAudio = false
                                        val newFile = java.io.File(context.filesDir, "med_voice_${System.currentTimeMillis()}.3gp")
                                        val started = audioRecorder.startRecording(newFile)
                                        if (started) {
                                            isRecording = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("start_recording_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (voiceNotePath != null) "إعادة التسجيل" else "تسجيل بصوتك", fontSize = 12.sp)
                                }
                            }

                            if (voiceNotePath != null && java.io.File(voiceNotePath!!).exists() && !isRecording) {
                                OutlinedButton(
                                    onClick = {
                                        if (isPlayingAudio) {
                                            audioRecorder.stopPlayback()
                                            isPlayingAudio = false
                                        } else {
                                            val played = audioRecorder.playAudio(voiceNotePath!!) {
                                                isPlayingAudio = false
                                            }
                                            if (played) isPlayingAudio = true
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("play_voice_note_button")
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isPlayingAudio) "إيقاف" else "استماع", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية (اختياري)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (medication != null && onDelete != null) {
                        IconButton(
                            onClick = { onDelete(medication) },
                            modifier = Modifier.testTag("delete_med_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الدواء",
                                tint = Color.Red
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val stockVal = stockCountText.toIntOrNull() ?: 30
                                    val lowVal = lowStockThresholdText.toIntOrNull() ?: 5
                                    val timesJoined = timesList.joinToString(",")
                                    val updatedMed = Medication(
                                        id = medication?.id ?: 0L,
                                        name = name.trim(),
                                        dosage = dosage.trim(),
                                        form = form,
                                        foodInstruction = foodInstruction,
                                        timesOfDay = timesJoined,
                                        stockCount = stockVal,
                                        lowStockThreshold = lowVal,
                                        colorHex = selectedColor,
                                        notes = notes.trim(),
                                        voiceNotePath = voiceNotePath,
                                        isActive = true
                                    )
                                    onSave(updatedMed)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier.testTag("save_med_button")
                        ) {
                            Text("حفظ التغييرات", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
