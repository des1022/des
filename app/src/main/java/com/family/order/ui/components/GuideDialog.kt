package com.family.order.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val GUIDE_STEPS = listOf(
    "① 在「点菜」页浏览菜品，点右侧 ＋ 即可加入购物车",
    "② 点底部购物车栏，可加减数量、查看明细",
    "③ 点「去结算」，填好你的名字就能下单",
    "④ 在「订单」页可看到自己的订单和厨房待做清单",
    "⑤ 管理功能藏在「我的」页底部，需要管理密码"
)

/** 首次打开时的操作引导（一步一图式文字引导） */
@Composable
fun GuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("怎么用点菜小程序") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                GUIDE_STEPS.forEach { step ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("开始点菜") }
        }
    )
}
