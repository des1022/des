package com.family.order.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.order.FamilyOrderApp
import com.family.order.ui.components.ConfirmDialog
import com.family.order.viewmodel.MineViewModel

@Composable
fun MineScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<MineViewModel>(factory = app.container.viewModelFactory)
    val nickname by vm.nickname.collectAsStateWithLifecycle()
    val hasPassword by vm.hasAdminPassword.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // onConfirm/onDismiss 是普通 lambda（非 @Composable），不能在内部调用 LocalContext.current，
    // 因此在组合作用域内提前取出 Context 供回调使用。
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 当前用户
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("当前昵称", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    nickname.ifBlank { "未设置" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    "下单时填写的昵称会记住，方便查看「我的订单」",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 管理入口（隐蔽：放在列表底部，需密码）
        Card(
            modifier = Modifier.fillMaxWidth().clickable { navController.navigate("admin") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "进入管理",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp).weight(1f)
                )
                Text(
                    if (hasPassword) "需密码" else "未设密码",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 清空本地缓存
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showClearDialog = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("清空本地缓存", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 使用说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("使用说明", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(
                    "1. 在「点菜」页点 ＋ 加菜，底部购物车可改数量\n" +
                        "2. 点「去结算」填名字就能下单\n" +
                        "3. 「订单」页可看自己的订单和厨房待做清单\n" +
                        "4. 管理功能（改菜、分类、订单状态）需管理密码\n" +
                        "5. 所有数据只存在本机，不上传、不联网",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "清空本地缓存",
            message = "将清除本机保存的昵称和管理密码，不影响菜品与订单数据。确定继续？",
            confirmText = "清空",
            onConfirm = {
                scope.launch { vm.clearCache() }
                showClearDialog = false
                android.widget.Toast.makeText(
                    context, "已清空本地缓存", android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showClearDialog = false }
        )
    }
}
