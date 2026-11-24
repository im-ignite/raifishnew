package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.GeminiEmptyState
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiSectionHeader
import com.example.ui.theme.RegisterGeminiFab
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiSoftBlue
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedRecordsScreen(
    deletedCustomersFlow: Flow<List<Customer>>,
    deletedProductsFlow: Flow<List<ProductItem>>,
    deletedSalesFlow: Flow<List<SaleTransaction>>,
    deletedBatchesFlow: Flow<List<DailyBatchEntry>>,
    onRestoreCustomer: (Customer) -> Unit,
    onRestoreProduct: (ProductItem) -> Unit,
    onRestoreSale: (SaleTransaction) -> Unit,
    onRestoreBatch: (DailyBatchEntry) -> Unit,
    onBack: () -> Unit
) {
    val customers by deletedCustomersFlow.collectAsState(initial = emptyList())
    val products by deletedProductsFlow.collectAsState(initial = emptyList())
    val sales by deletedSalesFlow.collectAsState(initial = emptyList())
    val batches by deletedBatchesFlow.collectAsState(initial = emptyList())

    // No FAB on the Recycle Bin screen.
    RegisterGeminiFab(null, null)

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recycle Bin",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
        ) {
            if (customers.isEmpty() && products.isEmpty() && sales.isEmpty() && batches.isEmpty()) {
                item {
                    GeminiEmptyState(
                        icon = Icons.Default.Restore,
                        title = "Nothing here yet",
                        message = "Recycle bin is empty."
                    )
                }
            }

            if (sales.isNotEmpty()) {
                item {
                    SectionHeader("Deleted Sales", badge = "${sales.size}")
                }
                items(sales) { sale ->
                    DeletedItemRow(
                        title = "${sale.customerName} - ${sale.itemName} (${sale.pieces} pcs)",
                        subtitle = sale.dateString,
                        onRestore = { onRestoreSale(sale) }
                    )
                }
            }

            if (customers.isNotEmpty()) {
                item {
                    SectionHeader("Deleted Customers", badge = "${customers.size}")
                }
                items(customers) { c ->
                    DeletedItemRow(
                        title = c.name,
                        subtitle = c.phoneNumber,
                        onRestore = { onRestoreCustomer(c) }
                    )
                }
            }

            if (products.isNotEmpty()) {
                item {
                    SectionHeader("Deleted Products", badge = "${products.size}")
                }
                items(products) { p ->
                    DeletedItemRow(
                        title = p.name,
                        subtitle = p.category,
                        onRestore = { onRestoreProduct(p) }
                    )
                }
            }

            if (batches.isNotEmpty()) {
                item {
                    SectionHeader("Deleted Batches", badge = "${batches.size}")
                }
                items(batches) { b ->
                    DeletedItemRow(
                        title = b.name,
                        subtitle = b.dateString,
                        onRestore = { onRestoreBatch(b) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, badge: String? = null) {
    GeminiSectionHeader(
        title = title,
        badge = badge,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun DeletedItemRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Soft-blue "Restore" pill
            Row(
                modifier = Modifier
                    .background(geminiSoftBlue(), CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onRestore)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = geminiAccent(),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Restore",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = geminiAccent()
                )
            }
        }
        GeminiHairline()
    }
}
