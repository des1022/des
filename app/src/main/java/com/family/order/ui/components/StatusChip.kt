package com.family.order.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.family.order.ui.theme.statusColor
import com.family.order.ui.theme.statusContainerColor
import com.family.order.util.orderStatusText

/** 订单状态标签 */
@Composable
fun OrderStatusChip(status: Int) {
    val container = statusContainerColor(status)
    val fg = statusColor(status)
    Surface(
        color = container,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    ) {
        Text(
            text = orderStatusText(status),
            color = fg,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
