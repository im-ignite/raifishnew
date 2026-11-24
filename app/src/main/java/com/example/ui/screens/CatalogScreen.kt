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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.ProductItem
import com.example.ui.theme.GeminiBottomSheet
import com.example.ui.theme.GeminiFormulaBar
import com.example.ui.theme.GeminiGradientButton
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiInitialAvatar
import com.example.ui.theme.GeminiSectionHeader
import com.example.ui.theme.GeminiSheetHeader
import com.example.ui.theme.RegisterGeminiFab
import com.example.ui.theme.bahi
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiGlass
import com.example.ui.theme.geminiPressable
import com.example.ui.theme.geminiSoftBlue
import com.example.ui.theme.geminiTextFieldColors
import com.example.ui.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<ProductItem>,
    googleAccount: GoogleAccountInfo,
    onAddProduct: (name: String, pricePerKg: Double, pieces: Int, weightKg: Double, category: String) -> Unit,
    onUpdateProduct: (ProductItem) -> Unit,
    onDeleteProduct: (ProductItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductItem?>(null) }

    // Quick Calculator tool state on profile
    var calcSelectedProduct by remember { mutableStateOf<ProductItem?>(products.firstOrNull()) }
    var calcPiecesText by remember { mutableStateOf("") }
    var calcWeightText by remember { mutableStateOf("") }
    var calcDropdownExpanded by remember { mutableStateOf(false) }

    val calcWeight = calcWeightText.toDoubleOrNull() ?: 0.0
    val calcPricePerKg = calcSelectedProduct?.pricePerKg ?: 0.0
    val calcResultPrice = calcWeight * calcPricePerKg

    // Contextual FAB (owned by MainActivity): "New Species" opens the add sheet.
    RegisterGeminiFab(
        label = if (strings.isHindi) strings.addProductButton else "New Species",
        onClick = { showAddDialog = true }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header: title + subtitle
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = googleAccount.displayName.ifEmpty { strings.productsCatalogTitle },
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = googleAccount.email.ifEmpty { strings.productsSubtitle },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live price calculator — one frosted glass card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .geminiGlass(RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(geminiSoftBlue(), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = geminiAccent(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.liveCalculatorTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.liveCalculatorSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Species selector (existing dropdown mechanism, restyled)
                ExposedDropdownMenuBox(
                    expanded = calcDropdownExpanded,
                    onExpandedChange = { calcDropdownExpanded = !calcDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = calcSelectedProduct?.let { "${it.name} (@ ₹${it.pricePerKg}/kg)" }
                            ?: if (strings.isHindi) "गणना के लिए उत्पाद चुनें" else "Select product to calculate",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.sellingProductLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calcDropdownExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("calc_product_picker")
                    )

                    ExposedDropdownMenu(
                        expanded = calcDropdownExpanded,
                        onDismissRequest = { calcDropdownExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} • ₹${prod.pricePerKg}/kg") },
                                onClick = {
                                    calcSelectedProduct = prod
                                    calcDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = calcWeightText,
                        onValueChange = { calcWeightText = it },
                        label = { Text(strings.weightLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier.weight(1f).testTag("calc_weight_input")
                    )

                    OutlinedTextField(
                        value = calcPiecesText,
                        onValueChange = { calcPiecesText = it },
                        label = { Text(strings.piecesLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier.weight(1f).testTag("calc_pieces_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live formula bar — total updates live from the inputs
                GeminiFormulaBar(
                    formula = "${String.format("%.2f", calcWeight)} kg × ₹${String.format("%.2f", calcPricePerKg)}/kg",
                    total = "₹${String.format("%.2f", calcResultPrice)}",
                    totalLabel = strings.autoPriceLabel
                )
            }
        }

        // Species list header + Add Species pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GeminiSectionHeader(
                        title = "${strings.productsCatalogTitle} (${products.size})"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(geminiSoftBlue(), CircleShape)
                        .clip(CircleShape)
                        .geminiPressable { showAddDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = geminiAccent(),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (strings.isHindi) strings.addProductButton else "Add Species",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = geminiAccent()
                        )
                    }
                }
            }
        }

        // Species rows, hairline-divided
        itemsIndexed(products, key = { _, product -> product.id }) { index, product ->
            Column(modifier = Modifier.fillMaxWidth()) {
                ProductCatalogCard(
                    product = product,
                    onEdit = { productToEdit = product },
                    onDelete = { onDeleteProduct(product) }
                )
                if (index < products.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                    GeminiHairline()
                }
            }
        }
    }

    // Add Product sheet
    if (showAddDialog) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pricePerKg, pcs, wt, cat ->
                onAddProduct(name, pricePerKg, pcs, wt, cat)
                showAddDialog = false
            }
        )
    }

    // Edit Product sheet
    productToEdit?.let { prod ->
        AddEditProductDialog(
            product = prod,
            onDismiss = { productToEdit = null },
            onConfirm = { name, pricePerKg, pcs, wt, cat ->
                onUpdateProduct(prod.copy(
                    name = name,
                    pricePerKg = pricePerKg,
                    stockPieces = pcs,
                    stockWeightKg = wt,
                    category = cat
                ))
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductCatalogCard(
    product: ProductItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_item_${product.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeminiInitialAvatar(
            text = product.name,
            size = 36.dp,
            fontSize = 13.sp,
            letters = 2
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.priceBasisWeightOnly,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹${String.format("%.2f", product.pricePerKg)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " /kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = geminiAccent(),
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.bahi.danger,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: ProductItem?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, pricePerKg: Double, pieces: Int, weight: Double, category: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(product?.name ?: "") }
    var pricePerKgText by remember { mutableStateOf(product?.pricePerKg?.toString() ?: "") }
    var piecesText by remember { mutableStateOf(product?.stockPieces?.toString() ?: "100") }
    var weightText by remember { mutableStateOf(product?.stockWeightKg?.toString() ?: "200.0") }
    var category by remember { mutableStateOf(product?.category ?: "Metals") }

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = if (product == null) strings.addProductButton else strings.editProductTitle,
            onClose = onDismiss
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (strings.isHindi) "बिक्री और इन्वेंट्री कटौती के दौरान स्वचालित गणना के लिए दर केवल वजन के आधार पर (₹/किग्रा) जोड़ी जाती है।" else "Price is added on weight basis only (₹/kg) for automatic calculation during sales and inventory deduction.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(strings.itemNameLabelText) },
            placeholder = { Text(strings.itemNamePlaceholderText) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = geminiTextFieldColors(),
            modifier = Modifier.fillMaxWidth().testTag("product_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = pricePerKgText,
            onValueChange = { pricePerKgText = it },
            label = { Text(strings.priceOnWeightBasisLabel) },
            placeholder = { Text(strings.pricePlaceholderText) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = geminiTextFieldColors(),
            modifier = Modifier.fillMaxWidth().testTag("product_price_per_kg_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text(strings.categoryLabel) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = geminiTextFieldColors(),
            modifier = Modifier.fillMaxWidth().testTag("product_category_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        GeminiGradientButton(
            label = strings.saveToProfileButton,
            onClick = {
                val price = pricePerKgText.toDoubleOrNull() ?: 0.0
                val pcs = piecesText.toIntOrNull() ?: 0
                val wt = weightText.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && price > 0) {
                    onConfirm(name.trim(), price, pcs, wt, category.trim())
                }
            },
            enabled = name.isNotBlank() && (pricePerKgText.toDoubleOrNull() ?: 0.0) > 0,
            modifier = Modifier.testTag("confirm_save_product_btn")
        )

        Spacer(modifier = Modifier.height(8.dp))

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
