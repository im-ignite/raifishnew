package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.ui.components.EditSaleDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GeminiBottomSheet
import com.example.ui.theme.GeminiDateSwitcher
import com.example.ui.theme.GeminiEmptyState
import com.example.ui.theme.GeminiFormulaBar
import com.example.ui.theme.GeminiGradientButton
import com.example.ui.theme.GeminiGradientText
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiInitialAvatar
import com.example.ui.theme.GeminiPillSearch
import com.example.ui.theme.GeminiSectionHeader
import com.example.ui.theme.GeminiSheetHeader
import com.example.ui.theme.GeminiStatLabel
import com.example.ui.theme.RegisterGeminiFab
import com.example.ui.theme.bahi
import com.example.ui.theme.geminiGlass
import com.example.ui.theme.geminiPressable
import com.example.ui.theme.geminiTextFieldColors
import com.example.ui.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDailyScreen(
    sales: List<SaleTransaction>,
    customers: List<Customer>,
    products: List<ProductItem>,
    onRecordSale: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        amountPaid: Double,
        dateString: String
    ) -> Unit,
    onUpdateSale: (SaleTransaction) -> Unit,
    onDeleteSale: (SaleTransaction) -> Unit,
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var searchQuery by remember { mutableStateOf("") }
    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var editingSale by remember { mutableStateOf<SaleTransaction?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleTransaction?>(null) }

    // The single gradient FAB is rendered by MainActivity.
    RegisterGeminiFab(
        label = strings.recordFishSaleButton.removePrefix("+ ").trim(),
        onClick = { showRecordSaleDialog = true }
    )

    // Helper functions for date stepping
    fun stepDate(daysDelta: Int) {
        try {
            val parsed = dateFormat.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = parsed
                add(Calendar.DAY_OF_YEAR, daysDelta)
            }
            selectedDate = dateFormat.format(cal.time)
        } catch (_: Exception) {
            selectedDate = todayDate
        }
    }

    // Filter sales for the selected date and search filter
    val dateSales = sales.filter { it.dateString == selectedDate }
    val filteredSales = if (searchQuery.isBlank()) {
        dateSales
    } else {
        val q = searchQuery.trim().lowercase()
        dateSales.filter {
            it.customerName.lowercase().contains(q) ||
            it.itemName.lowercase().contains(q)
        }
    }

    // Aggregates for the selected date
    val dayTotalSales = dateSales.sumOf { it.totalPrice }
    val dayTotalWeight = dateSales.sumOf { it.weightKg }
    val dayTotalPieces = dateSales.sumOf { it.pieces }
    val dayUniqueClients = dateSales.map { it.customerName }.distinct().size

    val dateLabel = if (selectedDate == todayDate) {
        "$selectedDate (${strings.todayButton})"
    } else {
        selectedDate
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 1. Borderless date switcher — tapping the date jumps to today.
        item {
            GeminiDateSwitcher(
                label = dateLabel,
                onPrevious = { stepDate(-1) },
                onNext = { stepDate(1) },
                onJumpToToday = { selectedDate = todayDate }
            )
        }

        // 2. Open two-column stat strip with hairline dividers (no boxes).
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GeminiStatLabel(strings.daySalesAmountLabel)
                    Spacer(modifier = Modifier.height(6.dp))
                    GeminiGradientText(
                        text = "₹${String.format("%.2f", dayTotalSales)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dayUniqueClients ${strings.dayClientsLabel.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(64.dp)
                        .background(MaterialTheme.bahi.edge)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp)
                ) {
                    GeminiStatLabel(strings.dayFishWeightLabel)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", dayTotalWeight),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dayTotalPieces ${strings.dayPiecesLabel.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GeminiHairline()
        }

        // 3. Pill glass search for this date.
        item {
            Spacer(modifier = Modifier.height(16.dp))
            GeminiPillSearch(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = strings.filterSearchPlaceholderText,
                modifier = Modifier.testTag("date_sales_search_input")
            )
        }

        // 4. Section header: "Client Fish Sales" + count badge.
        item {
            Spacer(modifier = Modifier.height(20.dp))
            GeminiSectionHeader(
                title = strings.dateSalesTitle,
                badge = "${filteredSales.size} sales"
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 5. Client sales list for the selected date — hairline-divided rows.
        if (filteredSales.isEmpty()) {
            item {
                GeminiEmptyState(
                    icon = Icons.Default.WaterDrop,
                    title = "No fish sales recorded",
                    message = "Selling directly from your pond? Tap Record Sale to track weight and payments."
                )
            }
        } else {
            items(filteredSales, key = { it.id }) { sale ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sale_item_${sale.id}")
                            .geminiPressable { editingSale = sale }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GeminiInitialAvatar(
                            text = sale.customerName,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sale.customerName,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${sale.itemName} · ${String.format("%.1f", sale.weightKg)} kg · ${sale.pieces} pcs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%.2f", sale.totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${String.format("%.2f", sale.pricePerKg)}/kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { saleToDelete = sale },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    GeminiHairline()
                }
            }
        }
    }

    // Sheet: Record Fish Sale for Selected Date
    if (showRecordSaleDialog) {
        RecordFishSaleDialog(
            selectedDate = selectedDate,
            products = products,
            customers = customers,
            onDismiss = { showRecordSaleDialog = false },
            onConfirm = { custId, custName, prodId, itemName, pcs, wt, rate, amountPaid, dateStr ->
                onRecordSale(custId, custName, prodId, itemName, pcs, wt, rate, amountPaid, dateStr)
                showRecordSaleDialog = false
            },
            onAddNewCustomer = onAddCustomer
        )
    }

    // Dialog: Edit Sale
    editingSale?.let { sale ->
        EditSaleDialog(
            sale = sale,
            products = products,
            customers = customers,
            onDismiss = { editingSale = null },
            onConfirm = { updated ->
                onUpdateSale(updated)
                editingSale = null
            }
        )
    }

    // Dialog: Confirm Delete
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text(strings.deleteSaleRecordTitleText) },
            text = {
                Text(if (strings.isHindi) "क्या आप ${sale.dateString} को ${sale.customerName} को बेचे गए ${sale.weightKg} किग्रा ${sale.itemName} के बिक्री रिकॉर्ड को हटाना चाहते हैं?" else "Are you sure you want to delete the sale of ${sale.weightKg} kg ${sale.itemName} to ${sale.customerName} on ${sale.dateString}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(sale)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(strings.deleteButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }

    // Dialog: Pick Any Specific Date using Calendar Picker.
    // (Kept for parity with the previous design; the borderless date
    // switcher above now jumps to today on tap, so this dialog currently
    // has no on-screen trigger.)
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                dateFormat.parse(selectedDate)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = dateFormat.format(Date(millis))
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text(strings.selectButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFishSaleDialog(
    selectedDate: String,
    products: List<ProductItem>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        amountPaid: Double,
        dateString: String
    ) -> Unit,
    onAddNewCustomer: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var isNewCustomerMode by remember { mutableStateOf(customers.isEmpty()) }
    var isRegularCustomerMode by remember { mutableStateOf(false) }
    var manualCustomerName by remember { mutableStateOf("") }
    var manualCustomerPhone by remember { mutableStateOf("") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var isManualFishMode by remember { mutableStateOf(products.isEmpty()) }
    var manualFishName by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    var weightInput by remember { mutableStateOf("") }
    var piecesInput by remember { mutableStateOf("1") }
    var amountPaidInput by remember { mutableStateOf("") }
    var rateInput by remember {
        mutableStateOf(selectedProduct?.pricePerKg?.toString() ?: "120.0")
    }
    var saleDate by remember { mutableStateOf(selectedDate) }

    // Live calculations
    val inputWeight = weightInput.toDoubleOrNull() ?: 0.0
    val inputPieces = piecesInput.toIntOrNull() ?: 1
    val inputRate = rateInput.toDoubleOrNull() ?: 0.0
    val calculatedTotal = inputWeight * inputRate

    val canSubmit = (selectedCustomer != null || manualCustomerName.isNotBlank() || isRegularCustomerMode) &&
            (selectedProduct != null || manualFishName.isNotBlank()) &&
            inputWeight > 0.0 &&
            inputRate > 0.0

    fun submit() {
        val finalCustName = if (isRegularCustomerMode) {
            "Regular Customer"
        } else if (isNewCustomerMode || selectedCustomer == null) {
            manualCustomerName.trim()
        } else {
            selectedCustomer!!.name
        }

        val finalFishName = if (isManualFishMode || selectedProduct == null) {
            manualFishName.trim()
        } else {
            selectedProduct!!.name
        }

        if (isNewCustomerMode && manualCustomerName.isNotBlank() && !isRegularCustomerMode) {
            onAddNewCustomer(manualCustomerName.trim(), manualCustomerPhone.trim(), "", "")
        }

        onConfirm(
            if (isNewCustomerMode || isRegularCustomerMode) null else selectedCustomer?.id,
            finalCustName,
            if (isManualFishMode) null else selectedProduct?.id,
            finalFishName,
            inputPieces,
            inputWeight,
            inputRate,
            amountPaidInput.toDoubleOrNull() ?: 0.0,
            saleDate.trim()
        )
    }

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = if (strings.isHindi) "${saleDate} के लिए मछली बिक्री दर्ज करें" else "Record Fish Sale for $saleDate",
            onClose = onDismiss
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Customer Selector
            if (isRegularCustomerMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .geminiGlass(RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.retailCustomerLabelText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { isRegularCustomerMode = false; isNewCustomerMode = false }) {
                                Text(strings.changeButton, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(strings.regularCustomerSubText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (!isNewCustomerMode && customers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: (if (strings.isHindi) "ग्राहक चुनें *" else "Select Client *"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.clientLabelText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("pond_sale_customer_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.phoneNumber})") },
                                    onClick = {
                                        selectedCustomer = cust
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { isRegularCustomerMode = true; isNewCustomerMode = false },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (strings.isHindi) "नियमित ग्राहक" else "Regular Customer", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(
                            onClick = { isNewCustomerMode = true; isRegularCustomerMode = false },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.addNewClientBtnText, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .geminiGlass(RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (strings.isHindi) "नये ग्राहक की जानकारी" else "New Client Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            if (customers.isNotEmpty()) {
                                TextButton(onClick = { isNewCustomerMode = false; isRegularCustomerMode = false }) {
                                    Text(if (strings.isHindi) "मौजूदा चुनें" else "Pick Existing", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = manualCustomerName,
                            onValueChange = { manualCustomerName = it },
                            label = { Text(strings.customerNameLabelText) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("new_client_name_input")
                        )

                        OutlinedTextField(
                            value = manualCustomerPhone,
                            onValueChange = { manualCustomerPhone = it },
                            label = { Text(strings.phoneNumberLabelText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("new_client_phone_input")
                        )
                    }
                }
            }

            // Fish Variety Selector
            if (!isManualFishMode && products.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} (₹${it.pricePerKg}/kg)" } ?: (if (strings.isHindi) "मछली का प्रकार चुनें *" else "Select Fish Type *"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.fishVarietyLabelText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("pond_sale_product_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} • ₹${prod.pricePerKg}/kg") },
                                    onClick = {
                                        selectedProduct = prod
                                        rateInput = prod.pricePerKg.toString()
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { isManualFishMode = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (strings.isHindi) "+ अन्य मछली किस्म टाइप करें" else "+ Type Other Fish Species", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = manualFishName,
                        onValueChange = { manualFishName = it },
                        label = { Text(strings.fishSpeciesNameLabelText) },
                        placeholder = { Text(strings.itemNamePlaceholderText) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (products.isNotEmpty()) {
                        TextButton(onClick = { isManualFishMode = false }) {
                            Text(if (strings.isHindi) "मछली सूची से चुनें" else "Choose from Fish Species List", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Weight (kg) & Pieces
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text(strings.weightLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier.weight(1.2f).testTag("pond_sale_weight_input")
                )

                OutlinedTextField(
                    value = piecesInput,
                    onValueChange = { piecesInput = it },
                    label = { Text(strings.piecesLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier.weight(0.8f).testTag("pond_sale_pieces_input")
                )
            }

            // Rate per kg & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text(if (strings.isHindi) "दर (₹/किग्रा) *" else "Rate (₹/kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier.weight(1f).testTag("pond_sale_rate_input")
                )

                OutlinedTextField(
                    value = saleDate,
                    onValueChange = { saleDate = it },
                    label = { Text(strings.dateLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier.weight(1f).testTag("pond_sale_date_input")
                )
            }

            // Live bill preview
            GeminiFormulaBar(
                formula = "${String.format("%.1f", inputWeight)} kg × ₹${String.format("%.2f", inputRate)}/kg",
                total = "₹${String.format("%.2f", calculatedTotal)}",
                totalLabel = if (strings.isHindi) "कुल बिल" else "Total Bill"
            )

            Spacer(modifier = Modifier.height(4.dp))
            GeminiGradientButton(
                label = strings.saveSaleRecordButton,
                onClick = { submit() },
                enabled = canSubmit,
                modifier = Modifier.testTag("save_pond_sale_btn")
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onDismiss) {
                    Text(strings.cancelButton)
                }
            }
        }
    }
}
