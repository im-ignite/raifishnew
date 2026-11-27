package com.example.data.repository

import android.content.Context
import android.content.Intent
import com.example.data.auth.GoogleAuthManager
import com.example.data.cloud.CloudDataStore
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.DailyBatchEntry
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.PaymentTransaction
import com.example.data.model.ProductItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SyncStatus
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class TradeRepository(
    val database: AppDatabase,
    private val notificationHelper: NotificationHelper,
    private val context: Context
) {
    private val batchDao = database.dailyBatchDao()
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val saleDao = database.saleDao()
    private val paymentDao = database.paymentDao()

    val authManager = GoogleAuthManager(context)
    val cloudDataStore = CloudDataStore(context)

    val googleAccount: StateFlow<GoogleAccountInfo> = authManager.accountInfo

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus = _syncStatus.asStateFlow()

    private val _lastSyncLog = MutableStateFlow("Google Account cloud backup ready")
    val lastSyncLog = _lastSyncLog.asStateFlow()

    private val repoScope = CoroutineScope(Dispatchers.IO)

    // Sync serialization: only one cloud sync runs at a time. Overlapping
    // requests set a flag instead of starting another sync, and the running
    // sync does one extra pass afterwards to catch them.
    private val syncMutex = Mutex()
    private val syncPending = AtomicBoolean(false)

    init {
        // If user is already linked with Google account, trigger silent initial sync
        repoScope.launch {
            val account = authManager.accountInfo.value
            if (account.isLinked && account.email.isNotBlank()) {
                pullDataFromCloud(account.email)
                triggerCloudSync(notifyUser = false)
            }
        }
    }

    // Flows
    val allBatches: Flow<List<DailyBatchEntry>> = batchDao.getAllBatches()
    val allProducts: Flow<List<ProductItem>> = productDao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allSales: Flow<List<SaleTransaction>> = saleDao.getAllSales()

    fun getBatchesBetween(startDate: String, endDate: String): Flow<List<DailyBatchEntry>> {
        return batchDao.getBatchesBetween(startDate, endDate)
    }

    fun getSalesBetween(startDate: String, endDate: String): Flow<List<SaleTransaction>> {
        return saleDao.getSalesBetween(startDate, endDate)
    }

    fun getSalesForCustomer(customerId: Long): Flow<List<SaleTransaction>> {
        return saleDao.getSalesForCustomer(customerId)
    }

    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentTransaction>> {
        return paymentDao.getPaymentsForCustomer(customerId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        saleDao.deleteAllSales()
        batchDao.deleteAllBatches()
        customerDao.deleteAllCustomers()
        productDao.deleteAllProducts()
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.clearUserCloudData(account.email)
        }
    }

    // Daily Batch Operations
    suspend fun addBatch(
        dateString: String,
        name: String,
        pieces: Int,
        weightKg: Double,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val entry = DailyBatchEntry(
            dateString = dateString,
            name = name,
            pieces = pieces,
            weightKg = weightKg,
            notes = notes,
            isSynced = true
        )
        val id = batchDao.insertBatch(entry)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveBatchToCloud(account.email, entry.copy(id = id))
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteBatch(entry: DailyBatchEntry) = withContext(Dispatchers.IO) {
        val deletedEntry = entry.copy(isDeleted = true)
        batchDao.updateBatch(deletedEntry)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveBatchToCloud(account.email, deletedEntry)
        }
        autoSyncIfEnabled()
    }

    suspend fun restoreBatch(entry: DailyBatchEntry) = withContext(Dispatchers.IO) {
        val restoredEntry = entry.copy(isDeleted = false)
        batchDao.updateBatch(restoredEntry)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveBatchToCloud(account.email, restoredEntry)
        }
        autoSyncIfEnabled()
    }

    // Product & Inventory Operations
    suspend fun addProduct(
        name: String,
        pricePerKg: Double,
        stockPieces: Int,
        stockWeightKg: Double,
        category: String
    ) = withContext(Dispatchers.IO) {
        val product = ProductItem(
            name = name,
            pricePerKg = pricePerKg,
            stockPieces = stockPieces,
            stockWeightKg = stockWeightKg,
            category = category
        )
        val id = productDao.insertProduct(product)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveProductToCloud(account.email, product.copy(id = id))
        }
        autoSyncIfEnabled()
    }

    suspend fun updateProduct(product: ProductItem) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveProductToCloud(account.email, product)
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteProduct(product: ProductItem) = withContext(Dispatchers.IO) {
        val deletedProduct = product.copy(isDeleted = true)
        productDao.updateProduct(deletedProduct)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveProductToCloud(account.email, deletedProduct)
        }
        autoSyncIfEnabled()
    }

    suspend fun restoreProduct(product: ProductItem) = withContext(Dispatchers.IO) {
        val restoredProduct = product.copy(isDeleted = false)
        productDao.updateProduct(restoredProduct)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveProductToCloud(account.email, restoredProduct)
        }
        autoSyncIfEnabled()
    }

    // Customer CRM Operations
    suspend fun addCustomer(
        name: String,
        phoneNumber: String,
        address: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val customer = Customer(
            name = name,
            phoneNumber = phoneNumber,
            address = address,
            notes = notes
        )
        val id = customerDao.insertCustomer(customer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveCustomerToCloud(account.email, customer.copy(id = id))
        }
        autoSyncIfEnabled()
        id
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveCustomerToCloud(account.email, customer)
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        val deletedCustomer = customer.copy(isDeleted = true)
        customerDao.updateCustomer(deletedCustomer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveCustomerToCloud(account.email, deletedCustomer)
        }
        autoSyncIfEnabled()
    }

    suspend fun restoreCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        val restoredCustomer = customer.copy(isDeleted = false)
        customerDao.updateCustomer(restoredCustomer)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveCustomerToCloud(account.email, restoredCustomer)
        }
        autoSyncIfEnabled()
    }

    // Sales & Automatic Inventory Deduction
    suspend fun recordSale(
        customerId: Long?,
        customerName: String,
        productId: Long?,
        itemName: String,
        pieces: Int,
        weightKg: Double,
        pricePerKg: Double,
        amountPaid: Double,
        dateString: String
    ) = withContext(Dispatchers.IO) {
        val totalPrice = weightKg * pricePerKg // Strictly weight-based calculation
        val sale = SaleTransaction(
            customerId = customerId,
            customerName = customerName,
            productId = productId,
            itemName = itemName,
            pieces = pieces,
            weightKg = weightKg,
            pricePerKg = pricePerKg,
            totalPrice = totalPrice,
            dateString = dateString,
            amountPaid = amountPaid,
            isSynced = false
        )
        val id = saleDao.insertSale(sale)
        val createdSale = sale.copy(id = id)

        // Automatic Inventory Deduction
        if (productId != null && productId > 0) {
            productDao.deductInventory(productId, pieces, weightKg)
            val updatedProd = productDao.getProductByIdDirect(productId)
            if (updatedProd != null && authManager.accountInfo.value.isLinked) {
                cloudDataStore.saveProductToCloud(authManager.accountInfo.value.email, updatedProd)
            }
        }

        val account = authManager.accountInfo.value
        val confirmed = !account.isLinked ||
            cloudDataStore.saveSaleToCloud(account.email, createdSale)
        if (confirmed) {
            saleDao.updateSale(createdSale.copy(isSynced = true))
        }

        notificationHelper.sendSaleRecordedNotification(customerName, totalPrice, weightKg)
        autoSyncIfEnabled()
    }

    suspend fun updateSale(sale: SaleTransaction) = updateSalesBatch(listOf(sale))

    // Update several sales together (one big payment clearing many orders):
    // all local updates and cloud writes happen in one pass, then a single
    // sync runs at the end instead of one sync per order.
    suspend fun updateSalesBatch(updatedSales: List<SaleTransaction>) = withContext(Dispatchers.IO) {
        if (updatedSales.isEmpty()) return@withContext
        val account = authManager.accountInfo.value
        for (sale in updatedSales) {
            val calculatedTotal = sale.weightKg * sale.pricePerKg
            val pendingSale = sale.copy(
                totalPrice = calculatedTotal,
                isSynced = false
            )
            saleDao.updateSale(pendingSale)
            // Confirm the cloud write before marking the row synced, so the UI
            // can show "syncing" until Firebase really has it. Offline
            // accounts have nothing to confirm, so they stay marked synced.
            val confirmed = !account.isLinked ||
                cloudDataStore.saveSaleToCloud(account.email, pendingSale)
            if (confirmed) {
                saleDao.updateSale(pendingSale.copy(isSynced = true))
            }
        }
        autoSyncIfEnabled()
    }

    suspend fun addPayment(payment: PaymentTransaction) = withContext(Dispatchers.IO) {
        val id = paymentDao.insertPayment(payment)
        val createdPayment = payment.copy(id = id)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.savePaymentToCloud(account.email, createdPayment)
        }
        autoSyncIfEnabled()
    }

    suspend fun deleteSale(sale: SaleTransaction) = withContext(Dispatchers.IO) {
        val deletedSale = sale.copy(isDeleted = true)
        saleDao.updateSale(deletedSale)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveSaleToCloud(account.email, deletedSale)
        }
        autoSyncIfEnabled()
    }

    suspend fun restoreSale(sale: SaleTransaction) = withContext(Dispatchers.IO) {
        val restoredSale = sale.copy(isDeleted = false)
        saleDao.updateSale(restoredSale)
        val account = authManager.accountInfo.value
        if (account.isLinked) {
            cloudDataStore.saveSaleToCloud(account.email, restoredSale)
        }
        autoSyncIfEnabled()
    }

    private fun autoSyncIfEnabled() {
        if (authManager.accountInfo.value.isLinked && authManager.accountInfo.value.autoSyncEnabled) {
            repoScope.launch {
                triggerCloudSync(notifyUser = false)
            }
        }
    }

    // Google Account Linking & Auth
    fun getDeviceGoogleAccounts(): List<String> {
        return authManager.getDeviceGoogleAccounts()
    }

    fun createSystemAccountPickerIntent(): Intent {
        return authManager.createSystemAccountPickerIntent()
    }

    suspend fun signInWithGoogleAccount(email: String, customName: String? = null) = withContext(Dispatchers.IO) {
        val info = authManager.completeSignInWithEmail(email, customName)
        // Clear local cache before loading account data so only this account's cloud data is loaded
        saleDao.deleteAllSales()
        batchDao.deleteAllBatches()
        customerDao.deleteAllCustomers()
        productDao.deleteAllProducts()

        pullDataFromCloud(info.email)
        triggerCloudSync(notifyUser = false)
    }

    suspend fun signInWithCredentialManager(activityContext: Context, webClientId: String? = null): Result<GoogleAccountInfo> = withContext(Dispatchers.IO) {
        val res = authManager.signInWithCredentialManager(activityContext, webClientId)
        res.onSuccess { info ->
            saleDao.deleteAllSales()
            batchDao.deleteAllBatches()
            customerDao.deleteAllCustomers()
            productDao.deleteAllProducts()

            pullDataFromCloud(info.email)
            triggerCloudSync(notifyUser = false)
        }
        res
    }

    fun signOutGoogleAccount() {
        repoScope.launch {
            saleDao.deleteAllSales()
            batchDao.deleteAllBatches()
            customerDao.deleteAllCustomers()
            productDao.deleteAllProducts()
        }
        authManager.signOut()
        _lastSyncLog.value = "Signed out. Cloud data safely saved in Google account."
    }

    fun toggleAutoSync(enabled: Boolean) {
        authManager.toggleAutoSync(enabled)
    }

    // Cross-device Cloud Sync Engine: Pull data from online store
    suspend fun pullDataFromCloud(email: String): Int = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext 0
        try {
            val payload = cloudDataStore.fetchCloudData(email) ?: return@withContext 0
            var restoredCount = 0

            if (payload.products.isNotEmpty()) {
                productDao.deleteAllProducts()
                productDao.insertAll(payload.products)
                restoredCount += payload.products.size
            }
            if (payload.customers.isNotEmpty()) {
                customerDao.deleteAllCustomers()
                customerDao.insertAll(payload.customers)
                restoredCount += payload.customers.size
            }
            if (payload.batches.isNotEmpty()) {
                batchDao.deleteAllBatches()
                batchDao.insertAll(payload.batches)
                restoredCount += payload.batches.size
            }
            if (payload.sales.isNotEmpty()) {
                saleDao.deleteAllSales()
                saleDao.insertAll(payload.sales)
                restoredCount += payload.sales.size
            }
            if (payload.payments.isNotEmpty()) {
                // Merge only: local payments are never wiped on sign-in,
                // so cloud payments are added without deleting local ones.
                paymentDao.insertAll(payload.payments)
                restoredCount += payload.payments.size
            }

            if (restoredCount > 0) {
                authManager.updateSyncMetadata(System.currentTimeMillis(), restoredCount)
                _lastSyncLog.value = "Restored $restoredCount records from Google Cloud"
            }
            return@withContext restoredCount
        } catch (e: Exception) {
            return@withContext 0
        }
    }

    // Dual-write Cloud Synchronization Engine.
    // Only one sync runs at a time: overlapping requests set syncPending and
    // are covered by one extra pass afterwards, instead of racing each other.
    suspend fun triggerCloudSync(notifyUser: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        if (!syncMutex.tryLock()) {
            syncPending.set(true)
            return@withContext false
        }
        try {
            var result = runSyncWithRetries(notifyUser)
            while (syncPending.getAndSet(false)) {
                result = runSyncWithRetries(false)
            }
            return@withContext result
        } finally {
            syncMutex.unlock()
        }
    }

    private suspend fun runSyncWithRetries(notifyUser: Boolean): Boolean {
        var result = runSync(notifyUser)
        var attempt = 1
        while (!result && attempt < 3) {
            delay(2000L * attempt)
            attempt++
            result = runSync(false)
        }
        return result
    }

    private suspend fun runSync(notifyUser: Boolean): Boolean {
        val account = authManager.accountInfo.value
        if (!account.isLinked || account.email.isBlank()) {
            _lastSyncLog.value = "Please sign in with Google to sync data online"
            return false
        }

        _syncStatus.value = SyncStatus.SYNCING
        _lastSyncLog.value = "Syncing with Google Cloud..."

        try {
            // 1. Upload local records FIRST, so a download afterwards can
            // never bring back older numbers and undo a fresh payment.
            val currentProducts = productDao.getAllProductsIncludingDeletedList()
            val currentCustomers = customerDao.getAllCustomersIncludingDeletedList()
            val currentBatches = batchDao.getAllBatchesIncludingDeletedList()
            val currentSales = saleDao.getAllSalesIncludingDeletedList()
            val currentPayments = paymentDao.getAllPaymentsIncludingDeletedList()

            val success = cloudDataStore.uploadToCloud(
                email = account.email,
                products = currentProducts,
                customers = currentCustomers,
                batches = currentBatches,
                sales = currentSales,
                payments = currentPayments
            )

            // 2. Then pull remote updates (rows added on another device, if any).
            // Never let the download overwrite a local row that is still
            // waiting to be confirmed — that row is newer than the cloud copy.
            val cloudPayload = cloudDataStore.fetchCloudData(account.email)
            if (cloudPayload != null) {
                if (cloudPayload.products.isNotEmpty()) productDao.insertAll(cloudPayload.products)
                if (cloudPayload.customers.isNotEmpty()) customerDao.insertAll(cloudPayload.customers)
                if (cloudPayload.batches.isNotEmpty()) {
                    val localBatches = batchDao.getAllBatchesList().associateBy { it.id }
                    val freshBatches = cloudPayload.batches.filter { cloud ->
                        val local = localBatches[cloud.id]
                        local == null || local.isSynced
                    }
                    if (freshBatches.isNotEmpty()) batchDao.insertAll(freshBatches)
                }
                if (cloudPayload.sales.isNotEmpty()) {
                    val localSales = saleDao.getAllSalesList().associateBy { it.id }
                    val freshSales = cloudPayload.sales.filter { cloud ->
                        val local = localSales[cloud.id]
                        local == null || local.isSynced
                    }
                    if (freshSales.isNotEmpty()) saleDao.insertAll(freshSales)
                }
                if (cloudPayload.payments.isNotEmpty()) paymentDao.insertAll(cloudPayload.payments)
            }

            val now = System.currentTimeMillis()
            if (success) {
                batchDao.markAllAsSynced(now)
                saleDao.markAllAsSynced()
            }

            val totalSynced = currentProducts.size + currentCustomers.size + currentBatches.size + currentSales.size + currentPayments.size
            authManager.updateSyncMetadata(now, totalSynced)

            _syncStatus.value = SyncStatus.SUCCESS
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))
            _lastSyncLog.value = "Synced with Google Cloud at $timeStr ($totalSynced records)"

            if (notifyUser) {
                notificationHelper.sendSyncNotification(
                    "Google Cloud Synced",
                    "Stored $totalSynced records online for ${account.email}"
                )
            }
            return success
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            _lastSyncLog.value = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            return false
        } finally {
            delay(1200)
            _syncStatus.value = SyncStatus.IDLE
        }
    }

    // CSV Generation for Manual Google Drive Export & Backup
    fun generateCsvContent(
        batches: List<DailyBatchEntry>,
        sales: List<SaleTransaction>,
        products: List<ProductItem>,
        customers: List<Customer>
    ): String {
        val sb = StringBuilder()
        sb.append("RAI FISH Cloud Export Data\n")
        sb.append("Generated At,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("Google Account,${authManager.accountInfo.value.email}\n\n")

        // 1. Daily Batches
        sb.append("--- DAILY INVENTORY & BATCH METRICS ---\n")
        sb.append("ID,Date,Batch Name,Pieces,Weight (kg),Notes,Synced Status\n")
        batches.forEach { b ->
            sb.append("${b.id},\"${b.dateString}\",\"${b.name.replace("\"", "\"\"")}\",${b.pieces},${b.weightKg},\"${b.notes.replace("\"", "\"\"")}\",${if (b.isSynced) "YES" else "PENDING"}\n")
        }
        sb.append("\n")

        // 2. Sales Transactions
        sb.append("--- SALES & CUSTOMER TRANSACTIONS ---\n")
        sb.append("Sale ID,Date,Customer Name,Item Name,Pieces,Weight (kg),Price per kg (₹),Total Amount (₹),Sync Status\n")
        sales.forEach { s ->
            sb.append("${s.id},\"${s.dateString}\",\"${s.customerName.replace("\"", "\"\"")}\",\"${s.itemName.replace("\"", "\"\"")}\",${s.pieces},${s.weightKg},${s.pricePerKg},${s.totalPrice},${if (s.isSynced) "YES" else "PENDING"}\n")
        }
        sb.append("\n")

        // 3. Products Catalog & Price basis
        sb.append("--- PRODUCTS CATALOG (FISH SPECIES) ---\n")
        sb.append("Product ID,Item Name,Category,Price Basis (₹/kg),Stock Pieces,Stock Weight (kg)\n")
        products.forEach { p ->
            sb.append("${p.id},\"${p.name.replace("\"", "\"\"")}\",\"${p.category}\",${p.pricePerKg},${p.stockPieces},${p.stockWeightKg}\n")
        }
        sb.append("\n")

        // 4. Customers Directory
        sb.append("--- CUSTOMERS DIRECTORY ---\n")
        sb.append("Customer ID,Name,Phone Number,Address,Notes\n")
        customers.forEach { c ->
            sb.append("${c.id},\"${c.name.replace("\"", "\"\"")}\",\"${c.phoneNumber}\",\"${c.address.replace("\"", "\"\"")}\",\"${c.notes.replace("\"", "\"\"")}\"\n")
        }

        return sb.toString()
    }

    fun isCloudConfigured(): Boolean = cloudDataStore.isCloudConfigured()

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsList()
        val customers = customerDao.getAllCustomersList()
        val batches = batchDao.getAllBatchesList()
        val sales = saleDao.getAllSalesList()
        val payments = paymentDao.getAllPaymentsList()
        val email = googleAccount.value.email.ifBlank { "offline_user@raifish.local" }
        cloudDataStore.serializePayloadToJson(email, products, customers, batches, sales, payments)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        val payload = cloudDataStore.parsePayloadFromJson(jsonString) ?: return@withContext false
        try {
            if (payload.products.isNotEmpty()) {
                productDao.insertAll(payload.products)
            }
            if (payload.customers.isNotEmpty()) {
                customerDao.insertAll(payload.customers)
            }
            if (payload.batches.isNotEmpty()) {
                batchDao.insertAll(payload.batches)
            }
            if (payload.sales.isNotEmpty()) {
                saleDao.insertAll(payload.sales)
            }
            _lastSyncLog.value = "Restored ${payload.sales.size} sales, ${payload.products.size} products from file"
            true
        } catch (e: Exception) {
            false
        }
    }
}
