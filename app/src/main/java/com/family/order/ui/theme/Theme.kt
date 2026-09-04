package com.family.order.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 主题色板：以「家常橙红」为主色，呼应点菜/美食场景，老人也能一眼分辨主操作按钮。
 * 同时兼容系统深色模式（跟随系统）。
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFFE64A19),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF5A1500),
    secondary = Color(0xFF7A4A2B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DCC8),
    background = Color(0xFFF6F5F2),
    onBackground = Color(0xFF1F1B16),
    surface = Color.White,
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFECEAE6),
    onSurfaceVariant = Color(0xFF5B534B),
    outline = Color(0xFFE2DFDA),
    outlineVariant = Color(0xFFD6D2CC),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF7A4D),
    onPrimary = Color(0xFF3A0A00),
    primaryContainer = Color(0xFF8A2A00),
    onPrimaryContainer = Color(0xFFFFDBD0),
    secondary = Color(0xFFE0BCA0),
    onSecondary = Color(0xFF3D2718),
    secondaryContainer = Color(0xFF573B29),
    background = Color(0xFF1A1714),
    onBackground = Color(0xFFE8E1D9),
    surface = Color(0xFF241F1B),
    onSurface = Color(0xFFE8E1D9),
    surfaceVariant = Color(0xFF4A433B),
    onSurfaceVariant = Color(0xFFCCC3B8),
    outline = Color(0xFF4A433B),
    outlineVariant = Color(0xFF3A3430),
    error = Color(0xFFF2B8B0),
    onError = Color(0xFF5A160F),
)

@Composable
fun FamilyOrderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

/** 价格文字颜色（中国习惯用红色表示金额） */
val priceColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFFF8A65) else Color(0xFFE64A19)

/** 订单状态对应的展示色 */
fun statusColor(status: Int): Color = when (status) {
    com.family.order.data.local.OrderEntity.STATUS_WAIT -> Color(0xFFE64A19)   // 待制作：橙红
    com.family.order.data.local.OrderEntity.STATUS_COOKING -> Color(0xFFF9A825) // 制作中：琥珀
    com.family.order.data.local.OrderEntity.STATUS_DONE -> Color(0xFF43A047)    // 已完成：绿
    else -> Color(0xFF9E9E9E)
}

/** 订单状态对应的浅底（用于 chip 背景） */
fun statusContainerColor(status: Int): Color = when (status) {
    com.family.order.data.local.OrderEntity.STATUS_WAIT -> Color(0xFFFFE0D6)
    com.family.order.data.local.OrderEntity.STATUS_COOKING -> Color(0xFFFFF3D6)
    com.family.order.data.local.OrderEntity.STATUS_DONE -> Color(0xFFDDF2DF)
    else -> Color(0xFFEEEEEE)
}
