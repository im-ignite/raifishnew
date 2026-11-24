package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.data.model.GoogleAccountInfo
import com.example.data.model.SyncStatus
import com.example.ui.theme.GeminiAvatar
import com.example.ui.theme.GeminiHairline
import com.example.ui.theme.bahi
import com.example.ui.theme.geminiAccent
import com.example.ui.theme.geminiGlass
import com.example.ui.util.AppStrings

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier, sizeDp: Int = 24) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            fontWeight = FontWeight.Bold,
            fontSize = (sizeDp * 0.65).sp,
            color = Color(0xFF4285F4)
        )
    }
}

/**
 * Account popover shown when the header avatar is tapped (MainActivity).
 * Rendered in a [Popup] anchored top-end just below the header; tapping
 * outside (or back) dismisses it via [onDismiss].
 *
 * NOTE: there is no `GoogleSignInDialog` function in this file — the
 * CredentialManager sign-in UI is native (see MainActivity) — so there
 * was no sign-in prompt here to restyle.
 */
@Composable
fun GoogleAccountProfileDialog(
    account: GoogleAccountInfo,
    syncStatus: SyncStatus,
    lastSyncLog: String,
    strings: AppStrings,
    isCloudConfigured: Boolean = false,
    onSyncNow: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onExportBackup: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    onSwitchAccount: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    val isSyncing = syncStatus == SyncStatus.SYNCING
    val isSynced = syncStatus == SyncStatus.SUCCESS || account.isLinked
    val density = LocalDensity.current

    Popup(
        alignment = Alignment.TopEnd,
        offset = with(density) { IntOffset((-16).dp.roundToPx(), 88.dp.roundToPx()) },
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .width(278.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .geminiGlass(RoundedCornerShape(24.dp), strong = true)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- Header: avatar, name, email, sync status ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeminiAvatar(
                        initials = account.initials,
                        size = 44.dp,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.displayName.ifBlank { "Google Account" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = account.email.ifBlank {
                                if (strings.isHindi) "साइन इन नहीं है" else "Not signed in"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isSynced) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = geminiAccent(),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.syncStatusDriveSynced,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = geminiAccent()
                                )
                            }
                        }
                    }
                }

                if (lastSyncLog.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lastSyncLog,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                GeminiHairline()
                Spacer(modifier = Modifier.height(8.dp))

                // --- Auto-sync toggle (kept from the old dialog) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.autoSyncTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.autoSyncSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = account.autoSyncEnabled,
                        onCheckedChange = onToggleAutoSync,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = geminiAccent()
                        ),
                        modifier = Modifier.testTag("auto_sync_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // --- Action rows ---
                PopoverRow(
                    icon = Icons.Default.Settings,
                    label = strings.settingsButton,
                    onClick = {
                        onOpenSettings?.invoke()
                        onDismiss()
                    }
                )
                PopoverRow(
                    icon = Icons.Default.CloudUpload,
                    label = if (strings.isHindi) "ड्राइव पर लेजर बैकअप करें" else "Backup Ledger to Drive",
                    onClick = {
                        onSyncNow()
                        onDismiss()
                    },
                    testTag = "backup_ledger_drive_btn"
                )
                PopoverRow(
                    icon = Icons.Default.Sync,
                    label = strings.syncNowButton,
                    onClick = {
                        onSyncNow()
                        onDismiss()
                    },
                    testTag = "sync_now_cloud_btn",
                    trailing = {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = geminiAccent(),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )
                PopoverRow(
                    icon = Icons.Default.CloudUpload,
                    label = strings.saveBackupButton,
                    onClick = {
                        onExportBackup()
                        onDismiss()
                    },
                    testTag = "export_backup_file_btn"
                )
                PopoverRow(
                    icon = Icons.Default.Restore,
                    label = strings.restoreBackupButton,
                    onClick = {
                        onRestoreBackup()
                        onDismiss()
                    },
                    testTag = "restore_backup_file_btn"
                )
                PopoverRow(
                    icon = Icons.Default.SwapHoriz,
                    label = strings.switchAccount,
                    onClick = {
                        onSwitchAccount()
                        onDismiss()
                    },
                    testTag = "switch_google_account_btn"
                )
                PopoverRow(
                    icon = Icons.Default.ExitToApp,
                    label = strings.signOutButton,
                    tint = MaterialTheme.bahi.danger,
                    onClick = {
                        onSignOut()
                        onDismiss()
                    },
                    testTag = "sign_out_google_btn"
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.closeButton,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PopoverRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color? = null,
    testTag: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val rowTint = tint ?: MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (tint != null) tint else geminiAccent(),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = rowTint,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}
