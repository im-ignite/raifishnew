package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────
//  Gemini shared UI kit — brushes, glass, avatar, FAB, sheets.
//  Every screen in the redesign builds on these so the look
//  stays consistent. No logic lives here, only styling.
// ─────────────────────────────────────────────────────────────

/** App background: frost white → sky blue gradient (light), black with a
 *  subtle centre blue glow (dark). Apply to the root container. */
fun Modifier.geminiBackground(): Modifier = composed {
    val isDark = MaterialTheme.bahi.isDark
    if (isDark) {
        background(Color.Black).drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2563EB).copy(alpha = 0.18f),
                        Color(0xFF172554).copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = size.maxDimension * 0.75f,
                )
            )
        }
    } else {
        background(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFF),
                    Color(0xFFE8F2FF),
                    Color(0xFFCFE4FF),
                )
            )
        )
    }
}

/** Glass surface colour: white 58% / 82% (light), white 4% / dark slate (dark). */
@Composable
fun geminiGlassColor(strong: Boolean = false): Color {
    val isDark = MaterialTheme.bahi.isDark
    return when {
        isDark && strong -> Color(0xC70F172A) // rgba(15,23,42,0.78)
        isDark -> Color.White.copy(alpha = 0.04f)
        strong -> Color.White.copy(alpha = 0.82f)
        else -> Color.White.copy(alpha = 0.58f)
    }
}

/** Frosted glass surface with a hairline border. */
@Composable
fun Modifier.geminiGlass(shape: Shape = RoundedCornerShape(24.dp), strong: Boolean = false): Modifier =
    this
        .background(geminiGlassColor(strong), shape)
        .border(1.dp, MaterialTheme.bahi.edge, shape)

/** Soft blue tint used for active pills, icon circles, formula bars. */
@Composable
fun geminiSoftBlue(): Color {
    val isDark = MaterialTheme.bahi.isDark
    return if (isDark) GeminiBlueDark.copy(alpha = 0.14f) else GeminiBlue.copy(alpha = 0.10f)
}

/** Accent text colour (blue in light, lighter blue in dark). */
@Composable
fun geminiAccent(): Color = if (MaterialTheme.bahi.isDark) GeminiBlueDark else GeminiBlue

/** Gradient brush for the FAB and primary buttons. */
@Composable
fun geminiFabBrush(): Brush {
    val isDark = MaterialTheme.bahi.isDark
    return Brush.linearGradient(
        listOf(
            if (isDark) FabGradientStartDark else FabGradientStart,
            FabGradientMid,
            if (isDark) FabGradientEndDark else FabGradientEnd,
        )
    )
}

/** Gradient brush for money totals (95deg blue → lavender). */
@Composable
fun geminiMoneyBrush(): Brush {
    val isDark = MaterialTheme.bahi.isDark
    return Brush.linearGradient(
        listOf(
            if (isDark) MoneyGradientStartDark else MoneyGradientStart,
            if (isDark) MoneyGradientMidDark else MoneyGradientMid,
            if (isDark) MoneyGradientEndDark else MoneyGradientEnd,
        )
    )
}

/** Text rendered with the Gemini blue gradient (money totals). */
@Composable
fun GeminiGradientText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style.copy(brush = geminiMoneyBrush()),
        modifier = modifier,
    )
}

/** Avatar with the multi-blue conic ring (Google account avatar). */
@Composable
fun GeminiAvatar(
    initials: String,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                Brush.sweepGradient(
                    listOf(GeminiBlue2, GeminiLavenderLight, GeminiSky, GeminiBlue, GeminiBlue2)
                ),
                CircleShape,
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiBlueLight)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

/** Soft blue circle with initials — list rows (clients, species, sales). */
@Composable
fun GeminiInitialAvatar(
    text: String,
    size: Dp = 40.dp,
    fontSize: TextUnit = 14.sp,
    letters: Int = 1,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(geminiSoftBlue(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.take(letters).uppercase(),
            color = geminiAccent(),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )
    }
}

/** Gradient pill FAB with a + icon and a label. */
@Composable
fun GeminiFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(10.dp, CircleShape, spotColor = Color(0xFF1A73E8).copy(alpha = 0.48f))
            .background(geminiFabBrush(), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

/** The single contextual FAB, owned by MainActivity. Screens register
 *  their label + action with [RegisterGeminiFab]; Reports registers null
 *  so the FAB hides. */
data class GeminiFabConfig(val label: String, val onClick: () -> Unit)

val LocalGeminiFab = staticCompositionLocalOf<MutableState<GeminiFabConfig?>> { mutableStateOf(null) }

@Composable
fun RegisterGeminiFab(label: String?, onClick: (() -> Unit)?) {
    val fabState = LocalGeminiFab.current
    val latestClick by rememberUpdatedState(onClick)
    DisposableEffect(label) {
        fabState.value = if (label != null && latestClick != null) {
            GeminiFabConfig(label) { latestClick?.invoke() }
        } else {
            null
        }
        onDispose {
            if (fabState.value?.label == label) fabState.value = null
        }
    }
}

/** Press feedback: shrink to ~0.95 while pressed (reference motion). */
fun Modifier.geminiPressable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        label = "geminiPress",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

/** Hairline divider (1dp, ink 7% / white 8%). */
@Composable
fun GeminiHairline(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = MaterialTheme.bahi.edge)
}

/** Shared colours for glass inputs (search bars, form fields). */
@Composable
fun geminiTextFieldColors(): TextFieldColors {
    val isDark = MaterialTheme.bahi.isDark
    val container = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.72f)
    val accent = if (isDark) GeminiBlueDark else GeminiBlue
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = container,
        unfocusedContainerColor = container,
        disabledContainerColor = container,
        focusedBorderColor = accent,
        unfocusedBorderColor = MaterialTheme.bahi.edge,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = accent,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Pill-shaped frosted search bar. */
@Composable
fun GeminiPillSearch(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
        },
        singleLine = true,
        shape = CircleShape,
        colors = geminiTextFieldColors(),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

/** Section header with an optional soft-blue count badge ("0 sales"). */
@Composable
fun GeminiSectionHeader(
    title: String,
    badge: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .background(geminiSoftBlue(), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(
                    text = badge,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = geminiAccent(),
                )
            }
        }
    }
}

/** Small uppercase stat label ("DAY'S TOTAL SALES"). */
@Composable
fun GeminiStatLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        letterSpacing = 0.5.sp,
    )
}

/** Soft pill tag ("Spent: ₹4,250" / "12.5 kg bought"). */
@Composable
fun GeminiTag(
    text: String,
    accent: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                if (accent) geminiSoftBlue() else geminiGlassColor(),
                CircleShape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (accent) geminiAccent() else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Friendly empty state: soft-blue icon circle, title, message, optional pill action. */
@Composable
fun GeminiEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(geminiSoftBlue(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = geminiAccent(),
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 260.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .background(geminiSoftBlue(), CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = geminiAccent(),
                )
            }
        }
    }
}

/** Auto-calculated formula bar: "12.5 kg × ₹180/kg" + gradient total. */
@Composable
fun GeminiFormulaBar(
    formula: String,
    total: String,
    totalLabel: String = "Auto Price",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(geminiSoftBlue(), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = formula,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = totalLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        GeminiGradientText(
            text = total,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
        )
    }
}

/** Borderless date switcher: ‹  [📅 Today · 24 Nov 2026]  ›. Tapping the
 *  date jumps to today. */
@Composable
fun GeminiDateSwitcher(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous date",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onJumpToToday)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = geminiAccent(),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next date",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Segmented pill selector (Today / Yesterday / 7 Days / 30 Days). */
@Composable
fun GeminiSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .geminiGlass(CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected) geminiAccent() else Color.Transparent,
                        CircleShape,
                    )
                    .clip(CircleShape)
                    .clickable { onSelect(index) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Bottom-sheet modal: 28dp top corners, drag handle, blurred/dimmed
 *  backdrop (handled by ModalBottomSheet's scrim). Wraps the content in
 *  the reference sheet padding. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = MaterialTheme.bahi.isDark
    val container = if (isDark) Color(0xF50C101A) else Color.White.copy(alpha = 0.94f)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = container,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(MaterialTheme.bahi.edge, CircleShape),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp),
        ) {
            content()
        }
    }
}

/** Bottom-sheet header: bold title + close button. */
@Composable
fun GeminiSheetHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Full-width gradient primary button for sheets ("Save Sale Entry"). */
@Composable
fun GeminiGradientButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CircleShape, spotColor = Color(0xFF1A73E8).copy(alpha = 0.4f))
            .background(geminiFabBrush(), CircleShape)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.6f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
    }
}
