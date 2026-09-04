package com.family.order.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** 通用二次确认弹窗（非阻塞，按钮回调由调用方处理关闭） */
@Composable
fun ConfirmDialog(
    title: String,
    message: String? = null,
    confirmText: String = "确定",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = if (message != null) {
            { Text(message) }
        } else null,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(cancelText) }
        }
    )
}
