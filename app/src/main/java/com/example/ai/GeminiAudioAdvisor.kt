package com.example.ai

import com.example.data.model.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAudioAdvisor {

    suspend fun generateSmartAudioReminder(
        medication: Medication,
        scheduledTime: String,
        patientTone: String = "حميمي ومشجع"
    ): String = withContext(Dispatchers.IO) {
        val formArabic = when (medication.form) {
            "PILL" -> "قرص"
            "SYRUP" -> "شراب مائي"
            "INJECTION" -> "حقنة طبية"
            "DROPS" -> "قطرات"
            "INHALER" -> "بخاخ"
            else -> "جرعة"
        }
        val foodArabic = when (medication.foodInstruction) {
            "BEFORE_MEAL" -> "قبل الأكل"
            "AFTER_MEAL" -> "بعد الأكل"
            "WITH_MEAL" -> "مع وجبة الطعام"
            "EMPTY_STOMACH" -> "على معدة فارغة"
            else -> ""
        }
        val foodPhrase = if (foodArabic.isNotEmpty()) "، وتذكر أن تأخذه $foodArabic" else ""

        when (patientTone) {
            "عاجل ومهم جداً" -> "تنبيه هام جداً! حانت الآن الساعة $scheduledTime، يرجى الالتزام الفوري بتناول $formArabic من دواء ${medication.name} بجرعة ${medication.dosage}$foodPhrase."
            "مباشر ودقيق" -> "تذكير موعد الدواء: $scheduledTime. دواء ${medication.name}، الجرعة: ${medication.dosage} $formArabic$foodPhrase."
            else -> "مرحباً! أتمنى لك دوام الصحة والعافية. حان الآن موعد تناول دواء ${medication.name}، الجرعة هي ${medication.dosage} $formArabic$foodPhrase."
        }
    }

    suspend fun getMedicationTips(medicationName: String): String = withContext(Dispatchers.IO) {
        "إرشادات استخدام دواء $medicationName:\n1. التزم بالجرعة المحددة في الموعد بالضبط دون زيادة أو نقصان.\n2. اشرب كمية كافية من الماء عند تناول الدواء.\n3. استشر الطبيب فور ظهور أي أعراض جانبية غير معتادة."
    }
}
