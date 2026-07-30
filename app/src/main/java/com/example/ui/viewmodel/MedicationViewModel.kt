package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAudioAdvisor
import com.example.audio.TextToSpeechHelper
import com.example.data.model.IntakeLog
import com.example.data.model.Medication
import com.example.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationViewModel(
    private val repository: MedicationRepository
) : ViewModel() {

    private val geminiAdvisor = GeminiAudioAdvisor()
    private var ttsHelper: TextToSpeechHelper? = null

    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val todayLogs: StateFlow<List<IntakeLog>> = _selectedDate
        .flatMapLatest { date -> repository.getLogsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allMedications: StateFlow<List<Medication>> = repository.allActiveMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogsHistory: StateFlow<List<IntakeLog>> = repository.allIntakeLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _voiceStyle = MutableStateFlow("FRIENDLY")
    val voiceStyle: StateFlow<String> = _voiceStyle.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _alertMode = MutableStateFlow("BOTH") // "BOTH", "VOICE_ONLY", "RINGTONE_ONLY", "VIBRATE"
    val alertMode: StateFlow<String> = _alertMode.asStateFlow()

    private val _ttsRate = MutableStateFlow(0.88f) // Default natural speech rate for Arabic
    val ttsRate: StateFlow<Float> = _ttsRate.asStateFlow()

    private val _ttsPitch = MutableStateFlow(1.0f)
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    private val _isGeneratingAiVoice = MutableStateFlow(false)
    val isGeneratingAiVoice: StateFlow<Boolean> = _isGeneratingAiVoice.asStateFlow()

    private val _aiCustomScript = MutableStateFlow<String?>(null)
    val aiCustomScript: StateFlow<String?> = _aiCustomScript.asStateFlow()

    private val _medicationTips = MutableStateFlow<String?>(null)
    val medicationTips: StateFlow<String?> = _medicationTips.asStateFlow()

    private val _isLoadingTips = MutableStateFlow(false)
    val isLoadingTips: StateFlow<Boolean> = _isLoadingTips.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _editingMedication = MutableStateFlow<Medication?>(null)
    val editingMedication: StateFlow<Medication?> = _editingMedication.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateDefaultsIfEmpty()
        }
    }

    private fun getTts(context: Context): TextToSpeechHelper {
        if (ttsHelper == null) {
            ttsHelper = TextToSpeechHelper(context).apply {
                speechRate = _ttsRate.value
                pitch = _ttsPitch.value
            }
        }
        return ttsHelper!!
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.generateLogsForDate(date)
        }
    }

    fun markDoseTaken(log: IntakeLog) {
        viewModelScope.launch {
            repository.markDoseTaken(log)
        }
    }

    fun markDoseSkipped(log: IntakeLog) {
        viewModelScope.launch {
            repository.markDoseSkipped(log)
        }
    }

    fun snoozeDose(log: IntakeLog, minutes: Int = 60) {
        viewModelScope.launch {
            repository.snoozeDose(log, minutes)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun playVoiceReminder(context: Context, text: String, voiceNotePath: String? = null) {
        val helper = getTts(context)
        helper.speechRate = _ttsRate.value
        helper.pitch = _ttsPitch.value
        helper.playCustomVoiceNoteOrTTS(
            voiceNotePath = voiceNotePath,
            text = text,
            alertMode = _alertMode.value,
            voiceStyle = _voiceStyle.value
        )
    }

    fun playTestRingtone(context: Context) {
        val helper = getTts(context)
        helper.playRingtoneSound()
    }

    fun stopVoiceReminder() {
        ttsHelper?.stop()
    }

    fun setAlertMode(mode: String) {
        _alertMode.value = mode
    }

    fun updateVoiceSettings(style: String, rate: Float, pitch: Float) {
        _voiceStyle.value = style
        _ttsRate.value = rate
        _ttsPitch.value = pitch
        ttsHelper?.apply {
            speechRate = rate
            this.pitch = pitch
        }
    }

    fun openAddMedicationDialog(medication: Medication? = null) {
        _editingMedication.value = medication
        _showAddEditDialog.value = true
    }

    fun closeAddMedicationDialog() {
        _showAddEditDialog.value = false
        _editingMedication.value = null
    }

    fun saveMedication(medication: Medication) {
        viewModelScope.launch {
            if (medication.id == 0L) {
                repository.addMedication(medication)
            } else {
                repository.updateMedication(medication)
            }
            closeAddMedicationDialog()
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun refillStock(medicationId: Long, amount: Int) {
        viewModelScope.launch {
            repository.refillStock(medicationId, amount)
        }
    }

    fun generateSmartAiScript(medication: Medication, scheduledTime: String) {
        viewModelScope.launch {
            _isGeneratingAiVoice.value = true
            val script = geminiAdvisor.generateSmartAudioReminder(
                medication = medication,
                scheduledTime = scheduledTime,
                patientTone = when (_voiceStyle.value) {
                    "FRIENDLY" -> "حميمي ومشجع ولطيف"
                    "DIRECT" -> "مباشر ودقيق"
                    "EMERGENCY" -> "عاجل ومهم جداً"
                    else -> "مشجع"
                }
            )
            _aiCustomScript.value = script
            _isGeneratingAiVoice.value = false
        }
    }

    fun clearAiCustomScript() {
        _aiCustomScript.value = null
    }

    fun fetchMedicationTips(medicationName: String) {
        viewModelScope.launch {
            _isLoadingTips.value = true
            val tips = geminiAdvisor.getMedicationTips(medicationName)
            _medicationTips.value = tips
            _isLoadingTips.value = false
        }
    }

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    fun openBackupDialog() {
        _backupStatusMessage.value = null
        _showBackupDialog.value = true
    }

    fun closeBackupDialog() {
        _showBackupDialog.value = false
        _backupStatusMessage.value = null
    }

    fun exportBackupData(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onExportReady(json)
            _backupStatusMessage.value = "تم تصدير النسخة الاحتياطية بنجاح!"
        }
    }

    fun importBackupData(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.importBackupJson(jsonString)
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _backupStatusMessage.value = "تمت استعادة $count أدوية بنجاح!"
                onComplete(true)
            } else {
                _backupStatusMessage.value = "عذراً، فشلت استعادة البيانات. يرجى التأكد من تنسيق الملف."
                onComplete(false)
            }
        }
    }

    fun clearMedicationTips() {
        _medicationTips.value = null
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper?.shutdown()
    }
}

class MedicationViewModelFactory(
    private val repository: MedicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
