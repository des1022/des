package com.family.order.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.order.FamilyOrderApp
import com.family.order.data.local.OrderEntity
import com.family.order.data.local.OrderWithGoods
import com.family.order.ui.components.EmptyState
import com.family.order.ui.components.LocalImage
import com.family.order.ui.components.OrderStatusChip
import com.family.order.ui.theme.priceColor
import com.family.order.util.OrderShareImage
import com.family.order.util.formatPrice
import com.family.order.util.formatTime
import com.family.order.viewmodel.OrdersViewModel
import com.family.order.viewmodel.TodoDish
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen() {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<OrdersViewModel>(factory = app.container.viewModelFactory)
    val allOrders by vm.allOrders.collectAsStateWithLifecycle()
    val nickname by vm.nickname.collectAsStateWithLifecycle()
    val tab by vm.tab.collectAsStateWithLifecycle()

    val tabTitles = listOf("我的订单", "待做清单")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = tab == index,
                    onClick = { vm.tab.value = index },
                    text = { Text(title) }
                )
            }
        }

        when (tab) {
            0 -> {
                val myOrders = vm.myOrders
                if (nickname.isBlank()) {
                    EmptyState("设置昵称后，这里会显示你下的订单\n（在「我的」页填写，或在下单时填写）")
                } else if (myOrders.isEmpty()) {
                    EmptyState("还没有下过单")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(myOrders, key = { it.order.id }) { owg ->
                            OrderCard(
                                owg,
                                onShare = { scope.launch { shareOrderPicture(context, owg) } }
                            )
                        }
                    }
                }
            }
            1 -> {
                val todo = vm.todoItems
                if (todo.isEmpty()) {
                    EmptyState("暂时没有待做的菜")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(todo, key = { it.name }) { item ->
                            TodoCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(owg: OrderWithGoods, onShare: () -> Unit) {
    val done = owg.order.status == OrderEntity.STATUS_DONE
    Card(
        modifier = Modifier.fillMaxWidth().then(if (done) Modifier.alpha(0.6f) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    owg.order.nickname.ifBlank { "匿名" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                if (owg.order.remark.isNotBlank()) {
                    Text(
                        "（${owg.order.remark}）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    OrderStatusChip(owg.order.status)
                }
            }
            Text(
                formatTime(owg.order.createTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            owg.goods.forEach { g ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LocalImage(g.imagePath, Modifier.size(36.dp), corner = 6.dp, sampleSize = 100)
                    Text(g.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text("×${g.num}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (owg.order.totalPrice > 0) {
                Text(
                    "合计 ¥${formatPrice(owg.order.totalPrice)}",
                    color = priceColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 分享：一键生成点单长图
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onShare) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("分享点单")
                }
            }
        }
    }
}

/** 生成订单分享图 → 调起系统分享面板；任一步失败都只弹 Toast，绝不闪退 */
private suspend fun shareOrderPicture(context: android.content.Context, owg: OrderWithGoods) {
    val file = OrderShareImage.render(context, owg)
    if (file == null) {
        android.widget.Toast.makeText(context, "图片生成失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享点单"))
    }.onFailure {
        android.widget.Toast.makeText(context, "分享失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun TodoCard(item: TodoDish) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalImage(item.imagePath, Modifier.size(48.dp), corner = 8.dp, sampleSize = 140)
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp).weight(1f)
            )
            Text(
                "×${item.totalNum} 份",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
