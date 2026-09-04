package com.family.order.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.order.FamilyOrderApp
import com.family.order.data.local.OrderEntity
import com.family.order.data.local.OrderWithGoods
import com.family.order.ui.components.AppTopBar
import com.family.order.ui.components.ConfirmDialog
import com.family.order.ui.components.EmptyState
import com.family.order.ui.components.LocalImage
import com.family.order.viewmodel.OrderAdminViewModel
import com.family.order.util.formatPrice
import com.family.order.util.formatTime

@Composable
fun OrderAdminScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<OrderAdminViewModel>(factory = app.container.viewModelFactory)
    val orders by vm.orders.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<OrderWithGoods?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        AppTopBar(title = "订单管理", onBack = { navController.popBackStack() })

        if (orders.isEmpty()) {
            EmptyState("还没有订单")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.order.id }) { owg ->
                AdminOrderCard(
                    owg = owg,
                    onStatus = { scope.launch { vm.updateStatus(owg.order.id, it) } },
                    onDelete = { deleteTarget = owg }
                )
                }
            }
        }
    }

    if (deleteTarget != null) {
        ConfirmDialog(
            title = "删除订单",
            message = "确定删除「${deleteTarget!!.order.nickname.ifBlank { "匿名" }}」的这单？",
            confirmText = "删除",
            onConfirm = { deleteTarget?.let { vm.deleteOrder(it.order.id) }; deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun AdminOrderCard(
    owg: OrderWithGoods,
    onStatus: (Int) -> Unit,
    onDelete: () -> Unit
) {
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
                Text(
                    "  ${formatTime(owg.order.createTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除订单", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            owg.goods.forEach { g ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LocalImage(g.imagePath, Modifier.size(32.dp), corner = 6.dp, sampleSize = 90)
                    Text(g.name, Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("×${g.num}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (owg.order.remark.isNotBlank()) {
                Text(
                    "备注：${owg.order.remark}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // 状态切换
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusButton("待制作", owg.order.status == 0) { onStatus(0) }
                StatusButton("制作中", owg.order.status == 1) { onStatus(1) }
                StatusButton("已完成", owg.order.status == 2) { onStatus(2) }
            }
        }
    }
}

@Composable
private fun StatusButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.weight(1f)) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) { Text(label) }
    }
}
