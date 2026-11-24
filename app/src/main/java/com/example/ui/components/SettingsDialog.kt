package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.SyncStatus
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlue2
import com.example.ui.theme.GeminiBottomSheet
import com.example.ui.theme.GeminiGradientButton
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.GeminiSheetHeader
import com.example.ui.theme.bahi
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiGlass
import com.example.ui.theme.geminiSoftBlue
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsDialog(
    currentLanguage: AppLanguage,
    strings: AppStrings,
    googleAccount: GoogleAccountInfo = GoogleAccountInfo(),

    syncStatus: SyncStatus = SyncStatus.IDLE,
    onOpenGoogleSignIn: () -> Unit = {},
    onToggleAutoSync: (Boolean) -> Unit = {},
    onSyncNow: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onSwitchAccount: () -> Unit = {},
    onLanguageSelected: (AppLanguage) -> Unit,
    onClearAllData: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    val accent = geminiAccent()
    val danger = MaterialTheme.bahi.danger

    GeminiBottomSheet(onDismissRequest = onDismiss) {
        GeminiSheetHeader(
            title = strings.settingsTitle,
            onClose = onDismiss
        )
        Text(
            text = "RAI FISH Settings & Preferences",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Language Selection Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(geminiSoftBlue(), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = strings.languageSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.languageSectionSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // English Option Card
            LanguageOptionCard(
                title = "English",
                subtitle = "Default system language (USD / ₹ pricing)",
                isSelected = currentLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
                testTag = "lang_option_english"
            )

            // Hindi Option Card
            LanguageOptionCard(
                title = "हिन्दी (Hindi)",
                subtitle = "दैनिक बैच, बिक्री, उत्पाद और ग्राहक हिन्दी में",
                isSelected = currentLanguage == AppLanguage.HINDI,
                onClick = { onLanguageSelected(AppLanguage.HINDI) },
                testTag = "lang_option_hindi"
            )

            GeminiHairline()

            // Information Card about Products & Sync
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .geminiGlass(RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = strings.appInfoTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.appInfoSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            GeminiHairline()

            // Google Account and Cloud Sync Card
            if (googleAccount.isLinked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .geminiGlass(RoundedCornerShape(20.dp), strong = true)
                        .padding(16.dp)
                ) {
                    // Top row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiBlue2)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    strings.googleDriveSyncHeader,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.bahi.success, CircleShape))
                            }
                            Text(
                                googleAccount.email,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = onSwitchAccount,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.bahi.edge),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = geminiSoftBlue()),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.switchButton, color = accent, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Middle block (Cloud folder info)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .geminiGlass(RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    strings.cloudFolderLabel,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateStr = if (googleAccount.lastSyncTimestamp > 0) {
                                    SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(
                                        Date(googleAccount.lastSyncTimestamp)
                                    )
                                } else "Never"
                                Text(
                                    if (strings.isHindi) "अंतिम सिंक: $dateStr" else "Last Synced: $dateStr",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.bahi.success,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom block
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Switch(
                            checked = googleAccount.autoSyncEnabled,
                            onCheckedChange = onToggleAutoSync,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                        Text(
                            strings.realtimeSyncLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        OutlinedButton(
                            onClick = onExportCsv,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.bahi.edge),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = geminiSoftBlue()),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.csvButton, color = accent, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onSyncNow,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            enabled = syncStatus != SyncStatus.SYNCING
                        ) {
                            if (syncStatus == SyncStatus.SYNCING) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.syncButton, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .geminiGlass(RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenGoogleSignIn() }
                        .padding(12.dp)
                        .testTag("settings_google_account_card")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiBlue2)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.googleSignInTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.googleSignInExplanation,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                        )
                    }
                }
            }

            GeminiHairline()

            // Clear All Data Action (Erase Test Data / Reset Database)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(danger.copy(alpha = 0.08f))
                    .border(1.dp, danger.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { showConfirmClearDialog = true }
                    .padding(12.dp)
                    .testTag("clear_all_data_card")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = danger,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.clearAllDataTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = danger
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.clearAllDataSub,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        GeminiGradientButton(
            label = strings.doneButton,
            onClick = onDismiss,
            modifier = Modifier.testTag("settings_done_btn")
        )
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = danger,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = strings.clearAllDataConfirmTitle,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.clearAllDataConfirmMsg,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = danger),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_clear_all_btn")
                ) {
                    Text(strings.clearEverythingButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}


@Composable
private fun LanguageOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val accent = geminiAccent()
    val borderColor = if (isSelected) accent else MaterialTheme.bahi.edge
    val bgColor = if (isSelected) geminiSoftBlue() else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = accent,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
