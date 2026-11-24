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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.GeminiEmptyState
import com.example.ui.theme.GeminiFormulaBar
import com.example.ui.theme.GeminiGradientButton
import com.example.ui.theme.GeminiGradientText
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiInitialAvatar
import com.example.ui.theme.GeminiSectionHeader
import com.example.ui.theme.GeminiSheetHeader
import com.example.ui.theme.GeminiStatLabel
import com.example.ui.theme.GeminiTag
import com.example.ui.theme.RegisterGeminiFab
import com.example.ui.theme.bahi
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiGlass
import com.example.ui.theme.geminiPressable
import com.example.ui.theme.geminiTextFieldColors
import com.example.ui.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(
    sales: List<SaleTransaction>,
    products: List<ProductItem>,
    customers: List<Customer>,
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
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    onUpdateSale: (SaleTransaction) -> Unit = {},
    onDeleteSale: (SaleTransaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var saleToEdit by remember { mutableStateOf<SaleTransaction?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleTransaction?>(null) }
    val strings = LocalAppStrings.current

    // The single gradient FAB is rendered by MainActivity.
    RegisterGeminiFab(
        label = if (strings.isHindi) strings.recordSaleButton.removePrefix("+ ").trim() else "Add Order",
        onClick = { showRecordSaleDialog = true }
    )

    val totalSalesRevenue = sales.sumOf { it.totalPrice }
    val totalWeightSold = sales.sumOf { it.weightKg }
    val totalPiecesSold = sales.sumOf { it.pieces }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Title + subtitle, then an open three-metric strip.
        item {
            Text(
                text = strings.salesTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.salesSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            GeminiHairline()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GeminiStatLabel("Revenue")
                    Spacer(modifier = Modifier.height(6.dp))
                    GeminiGradientText(
                        text = "₹${String.format("%.2f", totalSalesRevenue)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.bahi.edge)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    GeminiStatLabel("Weight Sold")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${String.format("%.1f", totalWeightSold)} kg",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.bahi.edge)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    GeminiStatLabel("Pieces Sold")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$totalPiecesSold pcs",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            GeminiHairline()
        }

        // Sales History header + formula tag.
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GeminiSectionHeader(
                    title = "Sales History (${sales.size})",
                    modifier = Modifier.weight(1f)
                )
                GeminiTag(text = "Weight × ₹/kg", accent = true)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Sales list — hairline-divided rows.
        if (sales.isEmpty()) {
            item {
                GeminiEmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "No sales recorded yet",
                    message = "Tap Add Order to enter client, weight & pieces for auto-pricing."
                )
            }
        } else {
            items(sales, key = { it.id }) { sale ->
                SaleTransactionCard(
                    sale = sale,
                    onEdit = { saleToEdit = sale },
                    onDelete = { saleToDelete = sale }
                )
            }
        }
    }

    if (showRecordSaleDialog) {
        RecordSaleDialog(
            products = products,
            customers = customers,
            onDismiss = { showRecordSaleDialog = false },
            onConfirm = { custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date ->
                onRecordSale(custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date)
                showRecordSaleDialog = false
            },
            onAddCustomer = onAddCustomer
        )
    }

    // Confirm Delete Sale Dialog
    if (saleToDelete != null) {
        val saleItem = saleToDelete!!
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Delete Sale Record", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (strings.isHindi) "क्या आप ${saleItem.customerName} को ${saleItem.itemName} की ₹${String.format("%.2f", saleItem.totalPrice)} की बिक्री को हटाना चाहते हैं?" else "Are you sure you want to delete this sale of \"${saleItem.itemName}\" to ${saleItem.customerName} for ₹${String.format("%.2f", saleItem.totalPrice)}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(saleItem)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_sale_btn")
                ) {
                    Text(strings.deleteButton)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { saleToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_sale_btn")
                ) {
                    Text(strings.cancelButton)
                }
            }
        )
    }

    // Edit Sale Dialog
    if (saleToEdit != null) {
        EditSaleDialog(
            sale = saleToEdit!!,
            products = products,
            customers = customers,
            onDismiss = { saleToEdit = null },
            onConfirm = { updated ->
                onUpdateSale(updated)
                saleToEdit = null
            }
        )
    }
}

@Composable
fun SaleTransactionCard(
    sale: SaleTransaction,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sale_item_${sale.id}")
                .geminiPressable(onClick = onEdit)
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
                    text = "${sale.itemName} · ${String.format("%.1f", sale.weightKg)} kg × ₹${String.format("%.2f", sale.pricePerKg)}/kg · ${sale.pieces} pcs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sale.dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", sale.totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = geminiAccent()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("edit_sale_${sale.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Sale",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_sale_${sale.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Sale",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        GeminiHairline()
    }
}

@Composable
fun PaymentStatusBadge(amountPaid: Double, totalPrice: Double) {
    val dueAmount = totalPrice - amountPaid

    val (backgroundColor, textColor, text) = when {
        amountPaid >= totalPrice -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Paid / पूरा भुगतान")
        amountPaid > 0 -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Partial / आंशिक: ₹$amountPaid, Due: ₹${"%.2f".format(dueAmount)}")
        else -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Unpaid / बाकी: ₹${"%.2f".format(dueAmount)}")
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleDialog(
    products: List<ProductItem>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weight: Double,
        pricePerKg: Double,
        amountPaid: Double,
        date: String
    ) -> Unit,
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var manualCustomerName by remember { mutableStateOf("") }
    var isNewCustomerMode by remember { mutableStateOf(false) }
    var newCustomerPhone by remember { mutableStateOf("") }
    var newCustomerAddress by remember { mutableStateOf("") }

    var selectedProduct by remember { mutableStateOf<ProductItem?>(products.firstOrNull()) }
    var manualItemName by remember { mutableStateOf("") }
    var manualPricePerKgText by remember { mutableStateOf("") }

    var piecesText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(today) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    // Live Automatic Price Calculation on the basis of weight
    val inputWeight = weightText.toDoubleOrNull() ?: 0.0
    val inputPieces = piecesText.toIntOrNull() ?: 0
    val activePricePerKg = selectedProduct?.pricePerKg ?: manualPricePerKgText.toDoubleOrNull() ?: 0.0
    val calculatedTotalPrice = inputWeight * activePricePerKg

    // Inventory Deduction Preview
    val availablePieces = selectedProduct?.stockPieces ?: 0
    val availableWeight = selectedProduct?.stockWeightKg ?: 0.0
    val remainingPieces = availablePieces - inputPieces
    val remainingWeight = availableWeight - inputWeight
    val isWeightExceeded = selectedProduct != null && remainingWeight < 0

    fun submit() {
        val custName = if (isNewCustomerMode) manualCustomerName.trim() else selectedCustomer?.name ?: "Walk-in Buyer"
        val custId = if (isNewCustomerMode) {
            onAddCustomer(manualCustomerName.trim(), newCustomerPhone.trim(), newCustomerAddress.trim(), "")
            null
        } else selectedCustomer?.id

        val prodName = selectedProduct?.name ?: manualItemName.ifEmpty { "General Commodity" }
        val prodId = selectedProduct?.id
        val amountPaid = 0.0

        onConfirm(
            custId,
            custName,
            prodId,
            prodName,
            inputPieces,
            inputWeight,
            activePricePerKg,
            amountPaid,
            dateString
        )
    }

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = strings.recordCustomerSaleTitle,
            onClose = onDismiss
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Customer Selector or Creator
            if (!isNewCustomerMode) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: (if (strings.isHindi) "ग्राहक चुनें या जोड़ें" else "Select or add customer"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.clientLabelText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("sale_customer_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text("${customer.name} (${customer.phoneNumber})") },
                                    onClick = {
                                        selectedCustomer = customer
                                        customerDropdownExpanded = false
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
                            onClick = { isNewCustomerMode = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.addNewClientBtnText, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            } else {
                // Inline New Customer Fields
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
                            Text(strings.newCustomerDetailsTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            TextButton(onClick = { isNewCustomerMode = false }) {
                                Text(strings.pickExistingLabelText, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        OutlinedTextField(
                            value = manualCustomerName,
                            onValueChange = { manualCustomerName = it },
                            label = { Text(strings.customerNameLabelText) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("new_customer_name_input")
                        )

                        OutlinedTextField(
                            value = newCustomerPhone,
                            onValueChange = { newCustomerPhone = it },
                            label = { Text(strings.phoneNumberLabelText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("new_customer_phone_input")
                        )

                        OutlinedTextField(
                            value = newCustomerAddress,
                            onValueChange = { newCustomerAddress = it },
                            label = { Text(strings.addressLabelText) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("new_customer_address_input")
                        )
                    }
                }
            }

            // Product Selector from Main Profile
            ExposedDropdownMenuBox(
                expanded = productDropdownExpanded,
                onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.let { "${it.name} (₹${it.pricePerKg}/kg)" } ?: (if (strings.isHindi) "प्रोफ़ाइल से उत्पाद चुनें" else "Select product from profile"),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(strings.productCommodityLabelText) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("sale_product_dropdown")
                )

                ExposedDropdownMenu(
                    expanded = productDropdownExpanded,
                    onDismissRequest = { productDropdownExpanded = false }
                ) {
                    products.forEach { prod ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${prod.name} • ₹${prod.pricePerKg}/kg", fontWeight = FontWeight.SemiBold)
                                    Text("Stock: ${prod.stockPieces} pcs, ${String.format("%.1f", prod.stockWeightKg)} kg", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            onClick = {
                                selectedProduct = prod
                                productDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Pieces and Weight Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = piecesText,
                    onValueChange = { piecesText = it },
                    label = { Text(strings.piecesLabel) },
                    placeholder = { Text(if (strings.isHindi) "उदा. 10" else "e.g. 10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_pieces_input")
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(strings.weightLabel) },
                    placeholder = { Text(if (strings.isHindi) "उदा. 25.5" else "e.g. 25.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_weight_input")
                )
            }

            // Live Price Calculation (formula bar) + Inventory Deduction
            GeminiFormulaBar(
                formula = "${String.format("%.2f", inputWeight)} kg × ₹${String.format("%.2f", activePricePerKg)}/kg",
                total = "₹${String.format("%.2f", calculatedTotalPrice)}",
                totalLabel = if (strings.isHindi) "स्वचालित मूल्य" else "Auto Price"
            )

            if (selectedProduct != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isWeightExceeded) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (strings.isHindi) "स्टॉक से अधिक: ${String.format("%.1f", availableWeight)} किग्रा उपलब्ध है" else "Exceeds stock: ${String.format("%.1f", availableWeight)} kg available",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed
                        )
                    } else {
                        Text(
                            text = if (strings.isHindi) "इन्वेंट्री कटौती: ${availablePieces} → ${remainingPieces.coerceAtLeast(0)} पीस, ${String.format("%.1f", availableWeight)} → ${String.format("%.1f", remainingWeight.coerceAtLeast(0.0))} किग्रा" else "Inventory Deduction: ${availablePieces} → ${remainingPieces.coerceAtLeast(0)} pcs, ${String.format("%.1f", availableWeight)} → ${String.format("%.1f", remainingWeight.coerceAtLeast(0.0))} kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            GeminiGradientButton(
                label = strings.confirmSaleButtonText,
                onClick = { submit() },
                enabled = inputWeight > 0 && activePricePerKg > 0 &&
                        (if (isNewCustomerMode) manualCustomerName.isNotBlank() else selectedCustomer != null),
                modifier = Modifier.testTag("confirm_record_sale_btn")
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
