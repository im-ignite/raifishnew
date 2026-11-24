package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyBatchEntry
import com.example.data.model.SaleTransaction
import com.example.ui.components.WeightAndSalesTrendChart
import com.example.ui.theme.GeminiGradientText
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiInitialAvatar
import com.example.ui.theme.GeminiSegmentedControl
import com.example.ui.theme.GeminiStatLabel
import com.example.ui.theme.GeminiTag
import com.example.ui.theme.RegisterGeminiFab
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiFabBrush
import com.example.ui.theme.geminiGlass
import com.example.ui.theme.geminiPressable
import com.example.ui.theme.geminiSoftBlue
import com.example.ui.theme.geminiTextFieldColors
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.VisualReportMetrics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    metrics: VisualReportMetrics,
    selectedPreset: DateRangePreset,
    batches: List<DailyBatchEntry>,
    sales: List<SaleTransaction>,
    onSelectPreset: (DateRangePreset) -> Unit,
    onExportCsvForDrive: () -> Unit,
    specificSearchDate: String = "",
    onSearchSpecificDate: (String) -> Unit = {},
    onClearSpecificDate: () -> Unit = {},
    onOpenRecycleBin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val yesterdayDate = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedMetric by remember { mutableStateOf<String?>(null) }

    // No FAB on this tab.
    RegisterGeminiFab(null, null)

    // The four reference presets live in the segmented control; This Month /
    // All Time stay reachable as pills below it, and Specific Date is reached
    // through the date search (the ViewModel maps a picked date to it).
    val mainPresets = listOf(
        DateRangePreset.TODAY,
        DateRangePreset.YESTERDAY,
        DateRangePreset.LAST_7_DAYS,
        DateRangePreset.LAST_30_DAYS
    )
    val mainLabels = listOf(
        strings.presetToday,
        strings.presetYesterday,
        strings.preset7Days,
        strings.preset30Days
    )
    val selectedMainIndex = mainPresets.indexOf(selectedPreset)

    val filterNotice = when (selectedPreset) {
        DateRangePreset.TODAY -> "Fish sales for Today ($todayDate)"
        DateRangePreset.YESTERDAY -> "Fish sales for Yesterday ($yesterdayDate)"
        DateRangePreset.SPECIFIC_DATE -> "Sales on Date: ${specificSearchDate.ifBlank { "N/A" }}"
        DateRangePreset.LAST_7_DAYS -> "Fish sales for Last 7 Days"
        DateRangePreset.LAST_30_DAYS -> "Fish sales for Last 30 Days"
        DateRangePreset.THIS_MONTH -> "Fish sales for This Month"
        DateRangePreset.ALL_TIME -> "All recorded fish sales"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header: title + subtitle on the left, Bin + Share CSV on the right
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.reportsTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.reportsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Recycle Bin glass pill (existing onOpenRecycleBin callback)
                    Box(
                        modifier = Modifier
                            .geminiGlass(CircleShape)
                            .clip(CircleShape)
                            .geminiPressable(onClick = onOpenRecycleBin)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Recycle Bin",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Share CSV gradient mini-button
                    Box(
                        modifier = Modifier
                            .background(geminiFabBrush(), CircleShape)
                            .clip(CircleShape)
                            .geminiPressable(onClick = onExportCsvForDrive)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("export_drive_csv_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.shareCsvButton,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Segmented period selector + extra presets
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                GeminiSegmentedControl(
                    options = mainLabels,
                    selectedIndex = selectedMainIndex,
                    onSelect = { index -> onSelectPreset(mainPresets[index]) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DateRangePreset.THIS_MONTH to strings.presetMonth,
                        DateRangePreset.ALL_TIME to strings.presetAllTime
                    ).forEach { (preset, label) ->
                        val isSelected = preset == selectedPreset
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) geminiAccent() else geminiSoftBlue(),
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { onSelectPreset(preset) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else geminiAccent()
                            )
                        }
                    }
                }
            }
        }

        // Specific-date search row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.searchSpecificDateTitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.presetToday,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = geminiAccent(),
                            modifier = Modifier.clickable { onSearchSpecificDate(todayDate) }
                        )
                        Text(
                            text = " · ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = strings.presetYesterday,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onSearchSpecificDate(yesterdayDate) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.searchSpecificDateSub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date input — existing DatePickerDialog mechanism, restyled
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .testTag("specific_date_input")
                ) {
                    OutlinedTextField(
                        value = specificSearchDate.ifBlank { strings.selectDateLabel },
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedPreset == DateRangePreset.SPECIFIC_DATE && specificSearchDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onClearSpecificDate) {
                            Text(strings.clearFilterButton, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Range label row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filterNotice,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${metrics.totalTransactions} logs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = geminiAccent()
                )
            }
        }

        // Open 2×2 metric grid (tap a cell for its breakdown) + hairline
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .geminiPressable {
                                expandedMetric = if (expandedMetric == "sales") null else "sales"
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        GeminiStatLabel(strings.totalSalesKpi)
                        Spacer(modifier = Modifier.height(4.dp))
                        GeminiGradientText(
                            text = "₹${String.format("%.2f", metrics.totalSalesAmount)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .geminiPressable {
                                expandedMetric = if (expandedMetric == "weight") null else "weight"
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        GeminiStatLabel(strings.totalWeightKpi)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%.1f", metrics.totalWeightKg)} kg",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .geminiPressable {
                                expandedMetric = if (expandedMetric == "pieces") null else "pieces"
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        GeminiStatLabel(strings.totalPiecesKpi)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${metrics.totalPieces} pcs",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .geminiPressable {
                                expandedMetric = if (expandedMetric == "orders") null else "orders"
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        GeminiStatLabel(strings.transactionsKpi)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${metrics.clientSummaries.size}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                GeminiHairline()

                // Expanded per-fish breakdown (existing logic, kept)
                if (expandedMetric != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .geminiGlass(RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val title = when (expandedMetric) {
                            "weight" -> "Weight Breakdown"
                            "pieces" -> "Fish Pieces Breakdown"
                            "sales" -> "Sales Revenue Breakdown"
                            "orders" -> "Orders Overview"
                            else -> "Detailed Overview"
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        GeminiHairline()

                        val groupedByFish = metrics.filteredSales.groupBy { it.itemName }
                        groupedByFish.forEach { (fishName, salesList) ->
                            val totalFishWeight = salesList.sumOf { it.weightKg }
                            val totalFishPieces = salesList.sumOf { it.pieces }
                            val totalFishSales = salesList.sumOf { it.totalPrice }
                            val orderCount = salesList.size

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fishName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val subtext = when (expandedMetric) {
                                        "weight" -> "$totalFishWeight kg total"
                                        "pieces" -> "$totalFishPieces pieces total"
                                        "sales" -> "₹${String.format("%.2f", totalFishSales)} revenue"
                                        "orders" -> "$orderCount orders"
                                        else -> ""
                                    }
                                    Text(
                                        text = subtext,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            GeminiHairline()
                        }
                    }
                }
            }
        }

        // Sales logs list, hairline-divided
        if (metrics.filteredSales.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sales records in this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            itemsIndexed(metrics.filteredSales, key = { _, sale -> sale.id }) { index, sale ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${sale.customerName} · ${sale.itemName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${sale.dateString} · ${sale.weightKg} kg (${sale.pieces} pcs)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${String.format("%.2f", sale.totalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < metrics.filteredSales.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                        GeminiHairline()
                    }
                }
            }
        }

        // Client-wise sales breakdown (existing logic, restyled)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .geminiGlass(RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = geminiAccent(),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.clientBreakdownTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    GeminiTag(text = "${metrics.clientSummaries.size} Clients")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (metrics.clientSummaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noSalesOnDateMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        metrics.clientSummaries.forEach { client ->
                            var expanded by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .geminiGlass(RoundedCornerShape(14.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .clickable { expanded = !expanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        GeminiInitialAvatar(
                                            text = client.customerName,
                                            size = 40.dp,
                                            fontSize = 14.sp
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = client.customerName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = client.itemsSummary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${String.format("%.1f", client.totalWeightKg)} kg | ${client.totalPieces} pcs",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = geminiAccent(),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format("%.2f", client.totalAmount)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = geminiAccent()
                                        )
                                        Text(
                                            text = "${client.salesCount} sale(s)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (expanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .padding(bottom = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        client.transactions.forEach { tx ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = tx.itemName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${tx.weightKg} kg @ ₹${tx.pricePerKg}/kg",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = "₹${tx.totalPrice}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = geminiAccent()
                                                )
                                            }
                                            GeminiHairline()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Visual chart: weight and sales trends over time (call unchanged)
        item {
            WeightAndSalesTrendChart(
                points = metrics.chartPoints,
                modifier = Modifier.testTag("visual_report_chart")
            )
        }

        // Performance breakdown & averages (existing logic, restyled)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .geminiGlass(RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "Fish Sales Averages & Weight Ratios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Calculated from direct client harvest transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(geminiSoftBlue(), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Avg Weight / Fish",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.2f", metrics.avgWeightPerPiece)} kg",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = geminiAccent()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(geminiSoftBlue(), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Avg Rate / kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val avgVal = if (metrics.totalWeightKg > 0) metrics.totalSalesAmount / metrics.totalWeightKg else 0.0
                        Text(
                            text = "₹${String.format("%.2f", avgVal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = geminiAccent()
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.parse(if (specificSearchDate.isNotBlank()) specificSearchDate else todayDate)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            onSearchSpecificDate(selected)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(strings.selectButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancelButton)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
