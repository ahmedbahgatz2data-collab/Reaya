package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.MedicationRepository
import com.example.ui.components.AddEditMedicationDialog
import com.example.ui.screens.CabinetScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.VoiceSettingsScreen
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.MedReminderTheme
import com.example.ui.viewmodel.MedicationViewModel
import com.example.ui.viewmodel.MedicationViewModelFactory

import com.example.widget.NextDoseWidgetProvider
import com.example.worker.MedicationWorkScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule WorkManager background reminders & update widgets
        MedicationWorkScheduler.scheduleAllReminders(applicationContext)
        NextDoseWidgetProvider.updateAllWidgets(applicationContext)

        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember {
                MedicationRepository(
                    medicationDao = database.medicationDao(),
                    intakeLogDao = database.intakeLogDao()
                )
            }
            val viewModelFactory = remember { MedicationViewModelFactory(repository) }
            val viewModel: MedicationViewModel = viewModel(factory = viewModelFactory)
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MedReminderTheme(darkTheme = isDarkMode) {
                MedReminderAppContent(viewModel = viewModel)
            }
        }
    }
}

enum class ScreenTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("الرئيسية", Icons.Default.Home, "tab_home"),
    CABINET("الجدول والخزانة", Icons.Default.CalendarMonth, "tab_cabinet"),
    HISTORY("السجل", Icons.Default.History, "tab_history"),
    VOICE_SETTINGS("الإعدادات والحساب", Icons.Default.Settings, "tab_settings")
}

@Composable
fun MedReminderAppContent(
    viewModel: MedicationViewModel
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }

    val showAddDialog by viewModel.showAddEditDialog.collectAsStateWithLifecycle()
    val editingMedication by viewModel.editingMedication.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BentoBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tab -> currentTab = tab }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCabinet = { currentTab = ScreenTab.CABINET },
                    onNavigateToVoiceSettings = { currentTab = ScreenTab.VOICE_SETTINGS }
                )
                ScreenTab.CABINET -> CabinetScreen(viewModel = viewModel)
                ScreenTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                ScreenTab.VOICE_SETTINGS -> VoiceSettingsScreen(viewModel = viewModel)
            }
        }

        if (showAddDialog) {
            AddEditMedicationDialog(
                medication = editingMedication,
                onDismiss = { viewModel.closeAddMedicationDialog() },
                onSave = { med -> viewModel.saveMedication(med, context) },
                onDelete = { med -> viewModel.deleteMedication(med, context) }
            )
        }
    }
}

@Composable
fun BentoBottomNavigation(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BentoBorder, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 12.dp,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BentoPrimary,
            windowInsets = androidx.compose.foundation.layout.WindowInsets.navigationBars
        ) {
            ScreenTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = BentoPrimaryContainer
                    ),
                    modifier = Modifier.testTag(tab.tag)
                )
            }
        }
    }
}
