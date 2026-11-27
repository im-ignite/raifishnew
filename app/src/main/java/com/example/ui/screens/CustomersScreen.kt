package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.PaymentTransaction
import com.example.data.model.SaleTransaction
import com.example.ui.components.EditSaleDialog
import com.example.ui.theme.GeminiBottomSheet
import com.example.ui.theme.GeminiEmptyState
import com.example.ui.theme.GeminiGradientButton
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiInitialAvatar
import com.example.ui.theme.GeminiPillSearch
import com.example.ui.theme.GeminiSheetHeader
import com.example.ui.theme.GeminiTag
import com.example.ui.theme.RegisterGeminiFab
import com.example.ui.theme.bahi
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiTextFieldColors
import com.example.ui.util.LocalAppStrings
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    sales: List<SaleTransaction>,
    onAddCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    onDeleteCustomer: (Customer) -> Unit = {},
    onUpdateSale: (SaleTransaction) -> Unit = {},
    onNavigateToSales: () -> Unit,
    onAddPayment: (Long, Double, String) -> Unit,
    onMarkSalePaid: (SaleTransaction) -> Unit = {},
    getPaymentsForCustomer: (Long) -> Flow<List<PaymentTransaction>>,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var saleToEdit by remember { mutableStateOf<SaleTransaction?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // FAB label: strings.addCustomerButton ("Add New Client" / Hindi) — the
    // localised string, as the task allows.
    RegisterGeminiFab(label = strings.addCustomerButton, onClick = { showAddDialog = true })

    val filteredCustomers = remember(customers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) customers else customers.filter {
            it.name.lowercase().contains(q) || it.phoneNumber.lowercase().contains(q)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
    ) {
        // Title + subtitle (reference Tab 5: "Clients & CRM")
        item {
            Text(
                text = strings.clientsTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.clientsSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Pill search
        item {
            GeminiPillSearch(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search client name or phone..."
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredCustomers.isEmpty()) {
            item {
                GeminiEmptyState(
                    icon = Icons.Default.Person,
                    title = if (searchQuery.isNotBlank()) "No matching clients found." else "No clients yet",
                    message = if (searchQuery.isNotBlank()) {
                        "Try a different name or phone number."
                    } else {
                        "Add your first client to start tracking their purchases."
                    },
                    actionLabel = if (searchQuery.isBlank()) strings.addCustomerButton else null,
                    onAction = if (searchQuery.isBlank()) ({ showAddDialog = true }) else null
                )
            }
        } else {
            items(filteredCustomers, key = { it.id }) { customer ->
                val customerSales = sales.filter { it.customerId == customer.id || it.customerName == customer.name }
                CustomerItemCard(
                    customer = customer,
                    salesHistory = customerSales,
                    onNewSale = onNavigateToSales,
                    onDelete = { customerToDelete = customer },
                    onEditSale = { saleToEdit = it },
                    onAddPayment = onAddPayment,
                    onMarkSalePaid = onMarkSalePaid,
                    getPaymentsForCustomer = getPaymentsForCustomer
                )
                GeminiHairline()
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address, notes ->
                onAddCustomer(name, phone, address, notes)
                showAddDialog = false
            }
        )
    }

    // Confirm Delete Client Dialog
    if (customerToDelete != null) {
        val client = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.bahi.danger,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.deleteClientTitle, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (strings.isHindi) "क्या आप ग्राहक \"${client.name}\" को हटाना चाहते हैं? यह CRM से उनकी प्रोफ़ाइल हटा देगा।" else "Are you sure you want to delete client \"${client.name}\"? This will remove their customer profile from the CRM.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(client)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.bahi.danger),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_customer_btn")
                ) {
                    Text(strings.deleteClientTitle)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { customerToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_customer_btn")
                ) {
                    Text(strings.cancelButton)
                }
            }
        )
    }

    // Edit Sale Dialog from customer profile
    if (saleToEdit != null) {
        EditSaleDialog(
            sale = saleToEdit!!,
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
fun CustomerItemCard(
    customer: Customer,
    salesHistory: List<SaleTransaction>,
    onNewSale: () -> Unit,
    onDelete: () -> Unit = {},
    onEditSale: (SaleTransaction) -> Unit = {},
    onAddPayment: (Long, Double, String) -> Unit,
    onMarkSalePaid: (SaleTransaction) -> Unit = {},
    getPaymentsForCustomer: (Long) -> Flow<List<PaymentTransaction>>,
    modifier: Modifier = Modifier
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showTransactionsDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "salesHistoryChevron"
    )

    val totalSpent = salesHistory.sumOf { it.totalPrice }
    val totalWeightBought = salesHistory.sumOf { it.weightKg }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("customer_card_${customer.id}")
            .padding(vertical = 16.dp)
    ) {
        // Customer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                GeminiInitialAvatar(
                    text = customer.name,
                    size = 44.dp,
                    fontSize = 15.sp,
                    letters = 1
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = customer.phoneNumber,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Call Phone Intent Button
                if (customer.phoneNumber.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${customer.phoneNumber}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Customer",
                            tint = geminiAccent(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Invoice Button — opens the invoice sheet with the payment
                // history (how much was paid, on which date).
                IconButton(
                    onClick = { showTransactionsDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Invoice",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add Payment Button
                IconButton(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Add Payment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Client Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_customer_${customer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Client",
                        tint = MaterialTheme.bahi.danger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (customer.address.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = customer.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spent / kg tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GeminiTag(
                text = "${strings.totalSpentLabel}: ₹${String.format("%.2f", totalSpent)}",
                accent = true
            )
            GeminiTag(
                text = "${String.format("%.1f", totalWeightBought)} kg ${strings.purchasedLabel}",
                accent = false
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expandable Sales History Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sales History (${salesHistory.size} orders)",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse sales history" else "Expand sales history",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        }

        // Sales History Items
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (salesHistory.isEmpty()) {
                    Text(
                        text = "No orders recorded for ${customer.name} yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Left-border history list (2dp, accent-soft edge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.bahi.edge, RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            salesHistory.forEach { sale ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${sale.itemName} · ${String.format("%.1f", sale.weightKg)} kg (${sale.dateString})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "₹${String.format("%.2f", sale.totalPrice)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { onEditSale(sale) },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("edit_client_sale_${sale.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Sale",
                                                    tint = geminiAccent(),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (sale.amountPaid < sale.totalPrice) {
                                                TextButton(
                                                    onClick = { onMarkSalePaid(sale) },
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                    modifier = Modifier.testTag("mark_paid_sale_${sale.id}")
                                                ) {
                                                    Text(
                                                        text = "Payment Done",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = geminiAccent()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    PaymentStatusBadge(amountPaid = sale.amountPaid, totalPrice = sale.totalPrice, isSynced = sale.isSynced)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                GeminiGradientButton(
                    label = if (strings.isHindi) "${customer.name} के लिए बिक्री जोड़ें" else "Add Sale for ${customer.name}",
                    onClick = onNewSale
                )
            }
        }
    }

    if (showPaymentDialog) {
        CustomerPaymentDialog(
            customerName = customer.name,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount ->
                val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                customer.id.let { id -> onAddPayment(id, amount, dateString) }
                showPaymentDialog = false
            }
        )
    }

    if (showTransactionsDialog) {
        val payments by getPaymentsForCustomer(customer.id).collectAsState(initial = emptyList())
        val totalBilled = totalSpent
        val totalPaid = payments.sumOf { it.amountPaid }
        val balanceDue = totalBilled - totalPaid
        val clipboardManager = LocalClipboardManager.current
        GeminiBottomSheet(onDismissRequest = { showTransactionsDialog = false }) {
            GeminiSheetHeader(
                title = if (strings.isHindi) "${customer.name} का चालान" else "Invoice for ${customer.name}",
                onClose = { showTransactionsDialog = false }
            )
            Spacer(modifier = Modifier.height(16.dp))
            InvoiceSummaryRow(
                label = if (strings.isHindi) "कुल बिल" else "Total billed",
                amount = totalBilled,
                amountColor = MaterialTheme.colorScheme.onSurface
            )
            InvoiceSummaryRow(
                label = if (strings.isHindi) "कुल भुगतान" else "Total paid",
                amount = totalPaid,
                amountColor = geminiAccent()
            )
            InvoiceSummaryRow(
                label = if (strings.isHindi) "बाकी" else "Balance due",
                amount = balanceDue,
                amountColor = if (balanceDue > 0.01) MaterialTheme.bahi.danger else MaterialTheme.bahi.success,
                bold = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            GeminiHairline()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (strings.isHindi) "भुगतान इतिहास (${payments.size})" else "Payment history (${payments.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (payments.isEmpty()) {
                Text(
                    text = strings.noPaymentsRecordedMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(payments) { payment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = payment.dateString,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${String.format("%.2f", payment.amountPaid)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = geminiAccent()
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GeminiGradientButton(
                label = if (strings.isHindi) "चालान कॉपी करें" else "Copy invoice",
                onClick = {
                    val invoiceText = buildString {
                        appendLine(if (strings.isHindi) "${customer.name} का चालान" else "Invoice for ${customer.name}")
                        appendLine("${if (strings.isHindi) "कुल बिल" else "Total billed"}: ₹${String.format("%.2f", totalBilled)}")
                        appendLine("${if (strings.isHindi) "कुल भुगतान" else "Total paid"}: ₹${String.format("%.2f", totalPaid)}")
                        appendLine("${if (strings.isHindi) "बाकी" else "Balance due"}: ₹${String.format("%.2f", balanceDue)}")
                        appendLine(if (strings.isHindi) "भुगतान इतिहास:" else "Payment history:")
                        payments.forEach { payment ->
                            appendLine("${payment.dateString}: ₹${String.format("%.2f", payment.amountPaid)}")
                        }
                    }
                    clipboardManager.setText(AnnotatedString(invoiceText))
                    Toast.makeText(
                        context,
                        if (strings.isHindi) "चालान कॉपी हो गया" else "Invoice copied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InvoiceSummaryRow(
    label: String,
    amount: Double,
    amountColor: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = amountColor
        )
    }
}

@Composable
fun CustomerPaymentDialog(
    customerName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val strings = LocalAppStrings.current
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = if (strings.isHindi) "$customerName के लिए भुगतान जोड़ें" else "Add Payment for $customerName",
            onClose = onDismiss
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                isError = false
            },
            label = { Text(strings.amountPaidLabelText) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isError,
            supportingText = { if (isError) Text(strings.validAmountErrorMsg) },
            shape = RoundedCornerShape(16.dp),
            colors = geminiTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        GeminiGradientButton(
            label = strings.confirmButton,
            onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    onConfirm(amount)
                } else {
                    isError = true
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = strings.addCustomerProfileTitle,
            onClose = onDismiss
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.customerNameLabelText) },
                placeholder = { Text(strings.customerNamePlaceholderText) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("cust_name_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(strings.phoneNumberLabelText) },
                placeholder = { Text(strings.phoneNumberPlaceholderText) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("cust_phone_input")
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(strings.addressLabelText) },
                placeholder = { Text(strings.addressPlaceholderText) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("cust_address_input")
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notesLabelText) },
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("cust_notes_input")
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        GeminiGradientButton(
            label = strings.saveCustomerButton,
            onClick = {
                if (name.isNotBlank()) {
                    onConfirm(name.trim(), phone.trim(), address.trim(), notes.trim())
                }
            },
            enabled = name.isNotBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    }
}
