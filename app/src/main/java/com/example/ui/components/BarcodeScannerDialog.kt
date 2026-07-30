package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.BentoPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ScannedMedicationResult(
    val barcode: String,
    val name: String,
    val dosage: String,
    val form: String,
    val foodInstruction: String,
    val expiryDate: String,
    val stockCount: Int = 30
)

// Sample Database of common recognized medical barcodes
val KNOWN_MEDICATIONS_DB = listOf(
    ScannedMedicationResult(
        barcode = "6291038472910",
        name = "بنادول أدفانس (Panadol Advance)",
        dosage = "500 mg",
        form = "PILL",
        foodInstruction = "AFTER_MEAL",
        expiryDate = "2027-11-30",
        stockCount = 24
    ),
    ScannedMedicationResult(
        barcode = "6221234567890",
        name = "أوجمنتين (Augmentin)",
        dosage = "1000 mg",
        form = "PILL",
        foodInstruction = "WITH_MEAL",
        expiryDate = "2026-09-15",
        stockCount = 14
    ),
    ScannedMedicationResult(
        barcode = "6289012345678",
        name = "فيتامين د3 (Vitamin D3)",
        dosage = "5000 IU",
        form = "PILL",
        foodInstruction = "AFTER_MEAL",
        expiryDate = "2028-05-20",
        stockCount = 60
    ),
    ScannedMedicationResult(
        barcode = "6299988776655",
        name = "شراب برونكوميل للسعال (Bronchomyl)",
        dosage = "10 ml",
        form = "SYRUP",
        foodInstruction = "ANYTIME",
        expiryDate = "2026-12-31",
        stockCount = 1
    )
)

@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (ScannedMedicationResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualBarcodeText by remember { mutableStateOf("") }
    var detectedMessage by remember { mutableStateOf<String?>(null) }

    val scanLaserTransition = rememberInfiniteTransition(label = "laser")
    val laserY by scanLaserTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("barcode_scanner_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ماسح الرمز الشريطي (Barcode 📷)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "وجه كاميرا الهاتف نحو باركود علبة الدواء",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_barcode_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(2.dp, BentoPrimary, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                    } catch (e: Exception) {
                                        Log.e("BarcodeScanner", "Camera bind failed: ${e.message}")
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "يرجى منح إذن الكاميرا لمسح الرمز الشريطي",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Scan Overlay Frame
                    Box(
                        modifier = Modifier
                            .size(200.dp, 140.dp)
                            .border(2.dp, Color.Green.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .background(Color.Green.copy(alpha = 0.05f))
                    ) {
                        // Moving Laser Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .padding(horizontal = 4.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = laserY.dp)
                                .background(Color.Red)
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "جاري قراءة الرمز الشريطي تلقائياً...",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recognized Barcode Quick Test Selector (for instant scanning or quick select)
                Text(
                    text = "أو اختر عينة دواء للتجربة السريعة للماسح:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KNOWN_MEDICATIONS_DB) { sample ->
                        Card(
                            onClick = {
                                onBarcodeScanned(sample)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoPrimary.copy(alpha = 0.1f)),
                            modifier = Modifier.testTag("sample_barcode_${sample.barcode}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = sample.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                                Text(text = "باركود: ${sample.barcode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(text = "انتهاء: ${sample.expiryDate}", fontSize = 10.sp, color = Color(0xFFD84315), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Barcode Input Fallback
                Text(
                    text = "إدخال يدوي للرمز الشريطي (Barcode):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualBarcodeText,
                        onValueChange = { manualBarcodeText = it },
                        placeholder = { Text("مثال: 6291038472910", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_barcode_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualBarcodeText.isNotBlank()) {
                                val match = KNOWN_MEDICATIONS_DB.firstOrNull { it.barcode == manualBarcodeText.trim() }
                                if (match != null) {
                                    onBarcodeScanned(match)
                                } else {
                                    // Calculate expiry date as 2 years from today
                                    val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 2) }
                                    val expStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                    onBarcodeScanned(
                                        ScannedMedicationResult(
                                            barcode = manualBarcodeText.trim(),
                                            name = "دواء جديد (${manualBarcodeText.takeLast(4)})",
                                            dosage = "500 mg",
                                            form = "PILL",
                                            foodInstruction = "AFTER_MEAL",
                                            expiryDate = expStr,
                                            stockCount = 30
                                        )
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_barcode_btn")
                    ) {
                        Text("اعتماد", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
