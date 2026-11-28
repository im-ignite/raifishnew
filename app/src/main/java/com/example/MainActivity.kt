package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.model.SyncStatus
import com.example.data.repository.TradeRepository
import com.example.notification.NotificationHelper
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.GoogleAccountProfileDialog
import com.example.ui.components.GoogleLogoIcon

import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardDailyScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.DeletedRecordsScreen
import com.example.ui.theme.GeminiAvatar
import com.example.ui.theme.GeminiFab
import com.example.ui.theme.GeminiFabConfig
import com.example.ui.theme.LocalGeminiFab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavInactiveDark
import com.example.ui.theme.NavInactiveLight
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiBackground
import com.example.ui.theme.geminiGlass
import com.example.ui.theme.geminiSoftBlue
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.TradeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TradeViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val notifHelper = NotificationHelper(applicationContext)
        val repository = TradeRepository(db, notifHelper, applicationContext)
        TradeViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            var userDarkModeOverride by remember { mutableStateOf<Boolean?>(null) }
            val isDark = userDarkModeOverride ?: systemDark

            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(
                    viewModel = viewModel,
                    isDarkMode = isDark,
                    onToggleDarkMode = {
                        userDarkModeOverride = !isDark
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: TradeViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val googleAccount by viewModel.googleAccount.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncLog by viewModel.lastSyncLog.collectAsStateWithLifecycle()

    val batches by viewModel.allBatches.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val reportMetrics by viewModel.reportMetrics.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountPopover by remember { mutableStateOf(false) }
    // Single contextual FAB, owned here; screens register via RegisterGeminiFab.
    val fabState = remember { mutableStateOf<GeminiFabConfig?>(null) }
    var hasAutoPromptedLogin by rememberSaveable { mutableStateOf(false) }
    

    val strings = remember(uiState.language) { AppStrings(uiState.language) }

    fun doGoogleSignIn() {
        viewModel.signInWithCredentialManager(context) { success, errorMsg ->
            if (success) {
                val msg = if (strings.isHindi) "Google खाता सिंक हो गया" else "Signed in with Google. Syncing cloud data..."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Sign in failed: ${errorMsg ?: "Unknown Error"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Prompt user to sign in with Google on startup like major apps
    LaunchedEffect(googleAccount.isLinked) {
        if (!googleAccount.isLinked && !hasAutoPromptedLogin) {
            hasAutoPromptedLogin = true
            kotlinx.coroutines.delay(600)
            doGoogleSignIn()
        }
    }

    // Notification permission request for Android 13+
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled for sync & inventory alerts", Toast.LENGTH_SHORT).show()
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackupFromUri(context, uri) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        LocalAppLanguage provides uiState.language,
        LocalGeminiFab provides fabState
    ) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxSize(),
                    drawerContainerColor = Color.Transparent
                ) {
                    AppDrawerContent(
                        activeTab = uiState.activeTab,
                        onSelectTab = { tabIndex ->
                            viewModel.setActiveTab(tabIndex)
                        },
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode,
                        onOpenSettings = {
                            showSettingsDialog = true
                        },
                        onSyncNow = {
                            viewModel.triggerCloudSync()
                            Toast.makeText(context, if (strings.isHindi) "क्लाउड सिंक शुरू किया गया" else "Cloud sync initiated", Toast.LENGTH_SHORT).show()
                        },
                        googleAccountEmail = googleAccount.email,
                        isCloudSynced = syncStatus == SyncStatus.SUCCESS || googleAccount.isLinked,
                        onCloseDrawer = {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .geminiBackground(),
                containerColor = Color.Transparent,
                floatingActionButton = {
                    val fab = fabState.value
                    if (fab != null) {
                        GeminiFab(label = fab.label, onClick = fab.onClick)
                    }
                },
                topBar = {
                    // Minimal Gemini header: hamburger, frosted tab-title pill, theme + avatar.
                    val tabTitle = when (uiState.activeTab) {
                        0 -> strings.navDaily
                        1 -> strings.navSales
                        2 -> strings.navProducts
                        3 -> strings.navReports
                        4 -> strings.navClients
                        5 -> "Recycle Bin"
                        else -> strings.navDaily
                    }
                    val avatarInitials =
                        if (googleAccount.displayName.isNotBlank() || googleAccount.email.isNotBlank()) {
                            googleAccount.initials
                        } else {
                            "IO"
                        }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .geminiGlass(CircleShape)
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.5.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleDarkMode,
                                modifier = Modifier.testTag("dark_mode_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = strings.darkModeToggle,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            GeminiAvatar(
                                initials = avatarInitials,
                                size = 36.dp,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { showAccountPopover = true }
                            )
                        }
                    }
                },
                bottomBar = {
                    val navItems = listOf(
                        Triple(strings.navDaily, Icons.Default.Today, "nav_daily"),
                        Triple(strings.navSales, Icons.Default.ShoppingCart, "nav_sales"),
                        Triple(strings.navProducts, Icons.Default.Inventory2, "nav_products"),
                        Triple(strings.navReports, Icons.Default.BarChart, "nav_reports"),
                        Triple(strings.navClients, Icons.Default.Group, "nav_customers")
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_nav_bar")
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        if (isDarkMode) Color.Black else Color(0xFFD6EAFF)
                                    )
                                )
                            )
                            .navigationBarsPadding()
                            .padding(top = 10.dp, bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navItems.forEachIndexed { index, (label, icon, testTag) ->
                                val isSelected = uiState.activeTab == index
                                val inactiveColor = if (isDarkMode) NavInactiveDark else NavInactiveLight
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { viewModel.setActiveTab(index) }
                                        .testTag(testTag)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 48.dp, height = 30.dp)
                                            .then(
                                                if (isSelected) {
                                                    Modifier.shadow(
                                                        6.dp,
                                                        CircleShape,
                                                        spotColor = geminiAccent().copy(alpha = 0.35f)
                                                    )
                                                } else Modifier
                                            )
                                            .background(
                                                if (isSelected) geminiSoftBlue() else Color.Transparent,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) geminiAccent() else inactiveColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) geminiAccent() else inactiveColor
                                    )
                                }
                            }
                        }
                    }
                }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardDailyScreen(
                        sales = sales,
                        customers = customers,
                        products = products,
                        onRecordSale = { custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date ->
                            viewModel.recordSale(custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date)
                            Toast.makeText(context, "Fish sale recorded & queued for Drive sync", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateSale = { updated ->
                            viewModel.updateSale(updated)
                            Toast.makeText(context, "Sale record updated", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteSale = { toDelete ->
                            viewModel.deleteSale(toDelete)
                            Toast.makeText(context, "Sale record deleted", Toast.LENGTH_SHORT).show()
                        },
                        onAddCustomer = { name, phone, address, notes ->
                            viewModel.addCustomer(name, phone, address, notes)
                        }
                    )

                    1 -> SalesScreen(
                        sales = sales,
                        products = products,
                        customers = customers,
                        onRecordSale = { custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date ->
                            viewModel.recordSale(custId, custName, prodId, itemName, pcs, wt, pricePerKg, amountPaid, date)
                            Toast.makeText(context, "Sale recorded! Inventory automatically deducted.", Toast.LENGTH_SHORT).show()
                        },
                        onAddCustomer = { name, phone, address, notes ->
                            viewModel.addCustomer(name, phone, address, notes)
                        },
                        onUpdateSale = { updated ->
                            viewModel.updateSale(updated)
                            Toast.makeText(context, "Sale record updated", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteSale = { toDelete ->
                            viewModel.deleteSale(toDelete)
                            Toast.makeText(context, "Sale record deleted", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> CatalogScreen(
                        products = products,
                        googleAccount = googleAccount,
                        onAddProduct = { name, pricePerKg, pcs, wt, cat ->
                            viewModel.addProduct(name, pricePerKg, pcs, wt, cat)
                            Toast.makeText(context, "Product saved to Main Profile", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateProduct = { viewModel.updateProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )

                    3 -> ReportsScreen(
                        metrics = reportMetrics,
                        selectedPreset = uiState.selectedDateRangePreset,
                        specificSearchDate = uiState.specificSearchDate,
                        batches = batches,
                        sales = sales,
                        onSelectPreset = { viewModel.setDateRangePreset(it) },
                        onSearchSpecificDate = { viewModel.setSpecificDateSearch(it) },
                        onClearSpecificDate = { viewModel.clearSpecificDateSearch() },
                        onExportCsvForDrive = {
                            val success = viewModel.exportCsvForGoogleDrive(context)
                            if (!success) {
                                Toast.makeText(context, "Failed to prepare CSV for Drive", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenRecycleBin = { viewModel.setActiveTab(5) },
                    )

                    4 -> CustomersScreen(
                        customers = customers,
                        sales = sales,
                        allPayments = allPayments,
                        onAddCustomer = { name, phone, address, notes ->
                            viewModel.addCustomer(name, phone, address, notes)
                            Toast.makeText(context, "Customer added to CRM", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteCustomer = { toDelete ->
                            viewModel.deleteCustomer(toDelete)
                            Toast.makeText(context, "Client removed", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateSale = { updated ->
                            viewModel.updateSale(updated)
                            Toast.makeText(context, "Sale record updated", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateToSales = { viewModel.setActiveTab(1) },
                        onAddPayment = { customerId, amount, dateString ->
                            viewModel.registerCustomerPayment(customerId, amount, dateString)
                            Toast.makeText(context, "Payment of ₹$amount recorded", Toast.LENGTH_SHORT).show()
                        },
                        onMarkSalePaid = { sale ->
                            viewModel.markSalePaid(sale)
                            Toast.makeText(context, "Payment done — ₹${String.format("%.2f", sale.totalPrice - sale.amountPaid)} recorded", Toast.LENGTH_SHORT).show()
                        },
                        getPaymentsForCustomer = { customerId ->
                            viewModel.getPaymentsForCustomer(customerId)
                        }
                    )
                    
                    5 -> DeletedRecordsScreen(
                        deletedCustomersFlow = viewModel.deletedCustomers,
                        deletedProductsFlow = viewModel.deletedProducts,
                        deletedSalesFlow = viewModel.deletedSales,
                        deletedBatchesFlow = viewModel.deletedBatches,
                        onRestoreCustomer = { viewModel.restoreCustomer(it) },
                        onRestoreProduct = { viewModel.restoreProduct(it) },
                        onRestoreSale = { viewModel.restoreSale(it) },
                        onRestoreBatch = { viewModel.restoreBatch(it) },
                        onBack = { viewModel.setActiveTab(3) } // Go back to Reports
                    )
                }
            }
        }
    }
    }

    // The CredentialManager now handles the Sign-In UI natively.

    if (showAccountPopover) {
        GoogleAccountProfileDialog(
            account = googleAccount,
            syncStatus = syncStatus,
            lastSyncLog = lastSyncLog,
            strings = strings,
            onOpenSettings = {
                showAccountPopover = false
                showSettingsDialog = true
            },
            onSyncNow = {
                viewModel.triggerCloudSync()
                Toast.makeText(
                    context,
                    if (strings.isHindi) "क्लाउड सिंक शुरू हुआ..." else "Syncing with Google Cloud...",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onToggleAutoSync = { enabled ->
                viewModel.toggleAutoSync(enabled)
            },
            onExportBackup = {
                val success = viewModel.exportBackupFile(context)
                if (!success) {
                    Toast.makeText(context, "Failed to export backup", Toast.LENGTH_SHORT).show()
                }
            },
            onRestoreBackup = {
                try {
                    restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not launch file picker", Toast.LENGTH_SHORT).show()
                }
            },
            onSwitchAccount = {
                doGoogleSignIn()
            },
            onSignOut = {
                viewModel.signOutGoogleAccount()
                Toast.makeText(context, "Signed out. Data preserved locally.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAccountPopover = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentLanguage = uiState.language,
            strings = strings,
            googleAccount = googleAccount,
            
            syncStatus = syncStatus,
            onOpenGoogleSignIn = {
                showSettingsDialog = false
                doGoogleSignIn()
            },
            onToggleAutoSync = { enabled ->
                viewModel.toggleAutoSync(enabled)
            },
            onSyncNow = {
                viewModel.triggerCloudSync()
                Toast.makeText(context, "Syncing with Google Cloud...", Toast.LENGTH_SHORT).show()
            },
            onExportCsv = {
                val success = viewModel.exportCsvForGoogleDrive(context)
                if (!success) {
                    Toast.makeText(context, "Failed to prepare CSV for export", Toast.LENGTH_SHORT).show()
                }
            },
            onSwitchAccount = {
                showSettingsDialog = false
                doGoogleSignIn()
            },
            onLanguageSelected = { newLang ->
                viewModel.setLanguage(newLang)
                val toastMsg = if (newLang == AppLanguage.HINDI)
                    "भाषा बदलकर हिन्दी कर दी गई"
                else
                    "Language switched to English"
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            },
            onClearAllData = {
                viewModel.clearAllData()
                Toast.makeText(context, strings.clearAllDataSuccess, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

}
}
