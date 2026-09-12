package com.example.shiftsync.ui

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.shiftsync.*
import com.example.shiftsync.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

private enum class ReportPeriod(val label: String) { WEEK("This Week"), MONTH("This Month"), YEAR("This Year"), ALL("All Time") }

@Composable
fun ExportReportsScreen(settings: AppSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val entries = remember { loadEntries(prefs) }
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }
    val filtered = remember(period, entries) { filterEntries(entries, period) }
    val totalMinutes = filtered.sumOf { it.durationMinutes }
    val totalPay = filtered.sumOf { it.estimatedPay }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Export Reports", onBack)
        AppCard { Text("Period", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); Text(period.label + " ⌄", color = GreenAccent, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { period = ReportPeriod.entries[(period.ordinal + 1) % ReportPeriod.entries.size] }) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("SUMMARY", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text("${filtered.size} shifts", color = TextSecondary, fontSize = 12.sp) }
        AppCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) { Text(formatDuration(totalMinutes), color = ShiftBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Total Hours", color = TextSecondary) }; VerticalDivider(modifier = Modifier.height(56.dp), color = BorderColor); Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) { Text(formatCurrency(totalPay, settings.currencySymbol), color = GreenAccent, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Total Pay", color = TextSecondary) } } }
        AppCard { SimpleRow(Icons.Default.TableChart, GreenAccent, "Export as CSV", "Open in Numbers, Excel, or any spreadsheet app", trailing = { Icon(Icons.Default.OpenInNew, null, tint = GreenAccent) }, onClick = { shareCsv(context, settings, filtered, period) }) }
        AppCard { SimpleRow(Icons.Default.PictureAsPdf, ShiftBlue, "Export as PDF", "Formatted report for payslips or records", trailing = { Icon(Icons.Default.OpenInNew, null, tint = ShiftBlue) }, onClick = { sharePdf(context, settings, filtered, period) }) }
        Text("PREVIEW", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        filtered.forEach { entry -> AppCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(formatDate(entry.startedAtMillis), fontWeight = FontWeight.Bold); Text(entry.shiftType.label, color = TextSecondary); Text(if (entry.shiftType == ShiftType.VACATION) "Paid day off" else "${formatShiftTime(entry.startedAtMillis, settings.use24HourClock)}-${formatShiftTime(entry.startedAtMillis + entry.durationMinutes * 60000, settings.use24HourClock)}", color = TextSecondary, fontSize = 12.sp) }; Text(formatCurrency(entry.estimatedPay, settings.currencySymbol), color = GreenAccent, fontWeight = FontWeight.Bold) } } }
    }
}

@Composable
fun SecurityPrivacyScreen(onBack: () -> Unit, onCleared: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var showConfirm by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { importPrefsFromJson(context, prefs, it.readText()) } } }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Security & Privacy", onBack)
        AppCard { SimpleRow(Icons.Default.Shield, ShiftBlue, "Data stored locally", "All shift data lives only on this device. Nothing is sent to external servers."); HorizontalDivider(color = BorderColor); SimpleRow(Icons.Default.CheckCircle, GreenAccent, "No account required", "ShiftSync works without sign-up. Your data stays private and is never shared.") }
        AppCard { SimpleRow(Icons.Default.UploadFile, GreenAccent, "Export Backup (JSON)", "Save your shift records to transfer to a new device", trailing = { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted) }, onClick = { shareJsonBackup(context, prefs) }); HorizontalDivider(color = BorderColor); SimpleRow(Icons.Default.Download, ShiftBlue, "Import Backup", "Restore shift records from a previously exported file", trailing = { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted) }, onClick = { picker.launch(arrayOf("application/json")) }) }
        AppCard { SimpleRow(Icons.Default.Delete, RedAccent, "Clear All Data", "Deletes all shifts, settings, and profile info", trailing = { Icon(Icons.Default.KeyboardArrowRight, null, tint = RedAccent) }, onClick = { showConfirm = true }) }
        if (showConfirm) AlertDialog(onDismissRequest = { showConfirm = false }, confirmButton = { TextButton(onClick = { prefs.edit().clear().apply(); showConfirm = false; onCleared() }) { Text("Clear", color = RedAccent) } }, dismissButton = { TextButton({ showConfirm = false }) { Text("Cancel") } }, title = { Text("Clear all data?") }, text = { Text("This removes all shifts, settings, and profile information from this device.") })
    }
}

@Composable
fun SalaryCurrencyScreen(settings: AppSettings, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var currency by remember { mutableStateOf(settings.currencySymbol) }
    var paymentType by remember { mutableStateOf(settings.paymentType) }
    var salary by remember { mutableStateOf(settings.salaryAmount.toString()) }
    var workday by remember { mutableDoubleStateOf(settings.workDayHours) }
    var saved by remember { mutableStateOf(false) }
    fun persist() { prefs.edit().putString(KEY_CURRENCY_SYMBOL, currency).putString(KEY_PAYMENT_TYPE, paymentType.prefValue).putString(KEY_SALARY_AMOUNT, salary).putFloat(KEY_WORK_DAY_HOURS, workday.toFloat()).apply(); onSaved() }
    LaunchedEffect(saved) {
        if (saved) {
            delay(1000)
            saved = false
        }
    }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp)); HeaderWithBack("Salary & Currency", onBack)
        Text("CURRENCY", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        AppCard { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf("$" to "Dollar", "₪" to "Shekel", "€" to "Euro").forEach { (symbol, label) -> SegmentedOption("$symbol $label", currency == symbol, { currency = symbol }, Modifier.weight(1f)) } } }
        Text("PAY RATE", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        AppCard { SimpleRow(Icons.Default.DateRange, ShiftBlue, "Payment Type", paymentType.name.lowercase().replaceFirstChar { it.uppercase() }, trailing = { Text(paymentType.name.lowercase().replaceFirstChar { it.uppercase() } + " ⌄", color = ShiftBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { paymentType = if (paymentType == PaymentType.MONTHLY) PaymentType.HOURLY else PaymentType.MONTHLY }) }); HorizontalDivider(color = BorderColor); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AttachMoney, tint = ShiftBlue, contentDescription = null); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(if (paymentType == PaymentType.MONTHLY) "Monthly Salary" else "Hourly Rate", color = TextSecondary, fontSize = 12.sp); OutlinedTextField(value = salary, onValueChange = { salary = it }, prefix = { Text(currency) }, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardBackground, unfocusedContainerColor = CardBackground, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) } }; HorizontalDivider(color = BorderColor); SimpleRow(Icons.Default.Schedule, GreenAccent, "Work Day Hours", "Used for day-off pay calculation", trailing = { Stepper("${workday}h", { workday = (workday - 0.5).coerceAtLeast(1.0) }, { workday += 0.5 }, GreenAccent) }) }
        SaveChangesButton(saved = saved) {
            persist()
            saved = true
        }
    }
}

@Composable
fun MapPickerScreen(onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    Column(Modifier.fillMaxSize().background(DarkSheet).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("Cancel", color = ShiftBlue) }; Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("Pick Workplace", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }; Spacer(Modifier.width(60.dp)) }
        Surface(color = DarkSheetCard, shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, tint = Color.White.copy(.6f)); Spacer(Modifier.width(8.dp)); Text("Search address...", color = Color.White.copy(.6f)) } }
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(28.dp)).background(Color(0xFF1B2A41)).clickable { prefs.edit().putBoolean(KEY_WORKPLACE_SET, true).putString(KEY_WORKPLACE_LABEL, "Pinned workplace • 32.0853, 34.7818").putString(KEY_WORKPLACE_LAT, "32.0853").putString(KEY_WORKPLACE_LNG, "34.7818").apply(); onSaved() }) {
            Canvas(Modifier.fillMaxSize()) { drawRect(brush = Brush.linearGradient(listOf(Color(0xFF2C7DA0), Color(0xFF577590), Color(0xFF264653)))); for (i in 0..8) drawLine(Color.White.copy(.12f), start = androidx.compose.ui.geometry.Offset(0f, size.height / 8 * i), end = androidx.compose.ui.geometry.Offset(size.width, size.height / 8 * i), strokeWidth = 2f); for (i in 0..6) drawLine(Color.White.copy(.08f), start = androidx.compose.ui.geometry.Offset(size.width / 6 * i, 0f), end = androidx.compose.ui.geometry.Offset(size.width / 6 * i, size.height), strokeWidth = 2f) }
            Box(Modifier.align(Alignment.Center).size(46.dp).clip(RoundedCornerShape(20.dp)).background(ShiftBlue), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = Color.White) }
        }
        Surface(color = DarkSheetCard, shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.width(50.dp).height(5.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(.15f))); Spacer(Modifier.height(16.dp)); Icon(Icons.Default.TouchApp, null, tint = Color.White); Spacer(Modifier.height(8.dp)); Text("Tap anywhere on the map to pin your workplace", color = Color.White) } }
    }
}

@Composable fun TermsOfUseScreen(onBack: () -> Unit) = PolicyScreen("Terms of Use", onBack, listOf(
    Triple("Informational Tool Only", ShiftBlue, "ShiftSync is provided as a personal record-keeping tool to help you track your own shifts, hours, and estimated pay. It is not a substitute for your employer's official timekeeping or payroll system."),
    Triple("Your Responsibility", OrangeAccent, "You are solely responsible for verifying the accuracy of any hours, pay calculations, or records logged in this app before relying on them for payroll, invoicing, tax, or any other employment-related purpose."),
    Triple("Limitation of Liability", RedAccent, "The developer of ShiftSync assumes no liability for payroll errors, missed or misrecorded shifts, incorrect pay calculations, or any employment disputes arising from use of this app. The app is provided \"as is\" without warranties of any kind."),
    Triple("Location & Notifications", GreenAccent, "Optional location-based reminders and notifications are generated entirely on your device to help you remember to clock in or out. They are provided for convenience only and should not be relied upon as your sole record of attendance.")
))

@Composable fun PrivacyPolicyScreen(onBack: () -> Unit) = PolicyScreen("Privacy Policy", onBack, listOf(
    Triple("No Data Is Collected", GreenAccent, "ShiftSync does not collect, transmit, or sell any personal data. There are no servers, no accounts, and no analytics or advertising SDKs in this app."),
    Triple("Everything Stays On Your Device", ShiftBlue, "Shift entries, pay settings, your profile info, and your workplace location (if set) are stored only in this app's local storage on your device. This data is included in your standard device backups, which are controlled by your device settings - not by this app."),
    Triple("Location Data", GreenAccent, "If you enable Workplace Geofencing, your location is used solely to detect arrival/departure at the workplace you set, entirely on-device, so the app can remind you to clock in or out. It is never transmitted anywhere."),
    Triple("Your Choice to Export", OrangeAccent, "The only way data leaves this app is if you explicitly export a report or backup file yourself (e.g. via AirDrop, email, or Files).")
))

@Composable private fun PolicyScreen(title: String, onBack: () -> Unit, items: List<Triple<String, Color, String>>) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Spacer(Modifier.height(8.dp)); HeaderWithBack(title, onBack); items.forEach { (heading, tint, body) -> AppCard { Text(heading, color = tint, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(8.dp)); Text(body, color = TextSecondary, lineHeight = 20.sp) } } } }

private fun filterEntries(entries: List<ShiftEntry>, period: ReportPeriod): List<ShiftEntry> {
    val now = Calendar.getInstance()
    return entries.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.startedAtMillis }
        when (period) {
            ReportPeriod.WEEK -> cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            ReportPeriod.MONTH -> cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            ReportPeriod.YEAR -> cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            ReportPeriod.ALL -> true
        }
    }
}

private fun shareCsv(context: Context, settings: AppSettings, entries: List<ShiftEntry>, period: ReportPeriod) {
    val file = File(context.cacheDir, "shifts-${period.label.lowercase().replace(' ', '-')}.csv")
    file.writeText(buildString {
        appendLine("Date,Type,Start,End,Duration,Pay,Notes")
        entries.forEach { appendLine("${formatEntryDate(it.startedAtMillis)},${it.shiftType.label},${formatShiftTime(it.startedAtMillis, settings.use24HourClock)},${formatShiftTime(it.startedAtMillis + it.durationMinutes * 60000, settings.use24HourClock)},${formatDuration(it.durationMinutes)},${formatCurrency(it.estimatedPay, settings.currencySymbol)},${it.notes.replace(',', ';')}") }
    })
    shareFile(context, file, "text/csv")
}
private fun sharePdf(context: Context, settings: AppSettings, entries: List<ShiftEntry>, period: ReportPeriod) {
    val file = File(context.cacheDir, "shifts-${period.label.lowercase().replace(' ', '-')}.pdf")
    val pdf = PdfDocument(); val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create()); val canvas = page.canvas; val paint = Paint().apply { textSize = 18f; isFakeBoldText = true }; var y = 50f
    canvas.drawText("ShiftSync ${period.label} Report", 40f, y, paint); y += 30
    paint.textSize = 12f; paint.isFakeBoldText = false
    entries.forEach { canvas.drawText("${formatEntryDate(it.startedAtMillis)}  ${it.shiftType.label}  ${formatDuration(it.durationMinutes)}  ${formatCurrency(it.estimatedPay, settings.currencySymbol)}", 40f, y, paint); y += 18 }
    pdf.finishPage(page); file.outputStream().use { pdf.writeTo(it) }; pdf.close(); shareFile(context, file, "application/pdf")
}
private fun shareJsonBackup(context: Context, prefs: android.content.SharedPreferences) { val file = File(context.cacheDir, "shiftsync-backup.json"); file.writeText(exportPrefsToJson(prefs).toString(2)); shareFile(context, file, "application/json") }
private fun shareFile(context: Context, file: File, mimeType: String) { val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file); context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_STREAM, uri); type = mimeType; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share")) }
