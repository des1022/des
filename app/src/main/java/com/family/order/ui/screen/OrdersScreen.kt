package com.family.order.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.order.FamilyOrderApp
import com.family.order.data.local.OrderEntity
import com.family.order.data.local.OrderWithGoods
import com.family.order.ui.components.EmptyState
import com.family.order.ui.components.LocalImage
import com.family.order.ui.components.OrderStatusChip
import com.family.order.ui.theme.priceColor
import com.family.order.util.formatPrice
import com.family.order.util.formatTime
import com.family.order.viewmodel.OrdersViewModel
import com.family.order.viewmodel.TodoDish

@Composable
fun OrdersScreen() {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<OrdersViewModel>(factory = app.container.viewModelFactory)
    val allOrders by vm.allOrders.collectAsStateWithLifecycle()
    val nickname by vm.nickname.collectAsStateWithLifecycle()
    val tab by vm.tab.collectAsStateWithLifecycle()

    val tabTitles = listOf("我的订单", "待做清单")

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
                            OrderCard(owg)
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
private fun OrderCard(owg: OrderWithGoods) {
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
        }
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
