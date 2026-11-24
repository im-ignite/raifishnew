package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Customer
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.ui.theme.GeminiBottomSheet
import com.example.ui.theme.GeminiFormulaBar
import com.example.ui.theme.GeminiGradientButton
import com.example.ui.theme.GeminiSheetHeader
import com.example.ui.theme.geminiTextFieldColors
import com.example.ui.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSaleDialog(
    sale: SaleTransaction,
    products: List<ProductItem> = emptyList(),
    customers: List<Customer> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (SaleTransaction) -> Unit
) {
    val strings = LocalAppStrings.current
    var customerName by remember { mutableStateOf(sale.customerName) }
    var selectedCustomerId by remember { mutableStateOf(sale.customerId) }
    var itemName by remember { mutableStateOf(sale.itemName) }
    var selectedProductId by remember { mutableStateOf(sale.productId) }
    var piecesText by remember { mutableStateOf(sale.pieces.toString()) }
    var weightText by remember { mutableStateOf(sale.weightKg.toString()) }
    var pricePerKgText by remember { mutableStateOf(sale.pricePerKg.toString()) }
    var amountPaidText by remember { mutableStateOf(sale.amountPaid.toString()) }
    var dateString by remember { mutableStateOf(sale.dateString) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val inputWeight = weightText.toDoubleOrNull() ?: 0.0
    val inputPricePerKg = pricePerKgText.toDoubleOrNull() ?: 0.0
    val calculatedTotalPrice = inputWeight * inputPricePerKg

    val isValid = customerName.isNotBlank() &&
        itemName.isNotBlank() &&
        (weightText.toDoubleOrNull() ?: 0.0) > 0 &&
        (pricePerKgText.toDoubleOrNull() ?: 0.0) > 0

    val confirmEdit: () -> Unit = {
        val pieces = piecesText.toIntOrNull() ?: sale.pieces
        val weight = weightText.toDoubleOrNull() ?: sale.weightKg
        val pricePerKg = pricePerKgText.toDoubleOrNull() ?: sale.pricePerKg
        val amountPaid = amountPaidText.toDoubleOrNull() ?: sale.amountPaid

        if (customerName.isNotBlank() && itemName.isNotBlank() && weight > 0 && pricePerKg > 0) {
            val updated = sale.copy(
                customerId = selectedCustomerId,
                customerName = customerName.trim(),
                productId = selectedProductId,
                itemName = itemName.trim(),
                pieces = pieces,
                weightKg = weight,
                pricePerKg = pricePerKg,
                amountPaid = amountPaid,
                totalPrice = weight * pricePerKg,
                dateString = dateString.trim().ifEmpty { sale.dateString },
                isSynced = false
            )
            onConfirm(updated)
        }
    }

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = strings.editSaleRecordTitle,
            onClose = onDismiss
        )
        Text(
            text = if (strings.isHindi) "बिक्री मेट्रिक्स, ग्राहक विवरण, वजन और मूल्य (₹) संशोधित करें।" else "Modify sale metrics, client details, weight, and price in Rupees (₹).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Customer selection / edit
            if (customers.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            selectedCustomerId = null
                        },
                        label = { Text(strings.clientNameLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("edit_sale_customer_input")
                    )

                    ExposedDropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} (${cust.phoneNumber})") },
                                onClick = {
                                    customerName = cust.name
                                    selectedCustomerId = cust.id
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(strings.clientNameLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_sale_customer_input")
                )
            }

            // Item Name / Product selection
            if (products.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = {
                            itemName = it
                            selectedProductId = null
                        },
                        label = { Text(strings.itemCommodityLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("edit_sale_item_input")
                    )

                    ExposedDropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} • ₹${prod.pricePerKg}/kg") },
                                onClick = {
                                    itemName = prod.name
                                    selectedProductId = prod.id
                                    pricePerKgText = prod.pricePerKg.toString()
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text(strings.itemCommodityLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_sale_item_input")
                )
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_sale_pieces_input")
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(strings.weightLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_sale_weight_input")
                )
            }

            // Price per kg in Rupees (₹)
            OutlinedTextField(
                value = pricePerKgText,
                onValueChange = { pricePerKgText = it },
                label = { Text(strings.pricePerKgLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_sale_price_per_kg_input")
            )

            // Date
            OutlinedTextField(
                value = dateString,
                onValueChange = { dateString = it },
                label = { Text(strings.dateLabel) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_sale_date_input")
            )

            OutlinedTextField(
                value = amountPaidText,
                onValueChange = { amountPaidText = it },
                label = { Text(strings.amountPaidLabelText) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = geminiTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_sale_amount_paid_input")
            )

            // Live price calculation in Rupees
            GeminiFormulaBar(
                formula = if (strings.isHindi) {
                    "${String.format("%.2f", inputWeight)} किग्रा × ₹${String.format("%.2f", inputPricePerKg)}/kg"
                } else {
                    "${String.format("%.2f", inputWeight)} kg × ₹${String.format("%.2f", inputPricePerKg)}/kg"
                },
                total = "₹${String.format("%.2f", calculatedTotalPrice)}",
                totalLabel = "Total Bill"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        GeminiGradientButton(
            label = strings.updateSaleButton,
            onClick = confirmEdit,
            enabled = isValid,
            modifier = Modifier.testTag("confirm_edit_sale_btn")
        )
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cancel_edit_sale_btn")
        ) {
            Text(strings.cancelButton)
        }
    }
}
