package com.family.order.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.order.FamilyOrderApp
import com.family.order.data.local.DishEntity
import com.family.order.ui.components.AppTopBar
import com.family.order.ui.components.ConfirmDialog
import com.family.order.ui.components.DishManageRow
import com.family.order.ui.components.EmptyState
import com.family.order.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<AdminViewModel>(factory = app.container.viewModelFactory)
    val dishes by vm.dishes.collectAsStateWithLifecycle()
    val adminPassword by vm.adminPassword.collectAsStateWithLifecycle()
    val hasPassword = adminPassword.isNotBlank()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pwd by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<DishEntity?>(null) }

    if (!vm.unlocked) {
        // —— 密码校验 / 首次设置 ——
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (hasPassword) "请输入管理密码" else "设置管理密码",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                if (hasPassword) "用于进入菜品管理" else "首次使用，请设置 4 位数字管理密码",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )
            OutlinedTextField(
                value = pwd,
                onValueChange = { if (it.length <= 4) pwd = it.filter { c -> c.isDigit() } },
                label = { Text("4 位数字密码") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = {
                    if (pwd.length != 4) {
                        android.widget.Toast.makeText(context, "请输入 4 位数字", android.widget.Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (hasPassword) {
                        if (vm.verify(pwd)) vm.unlock()
                        else android.widget.Toast.makeText(context, "密码错误", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch { vm.setPassword(pwd) }
                        android.widget.Toast.makeText(context, "密码已设置", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(if (hasPassword) "进入" else "确定")
            }
        }
        return
    }

    // —— 管理内容 ——
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜品管理") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("dishEdit") }) {
                Icon(Icons.Filled.Add, contentDescription = "新增菜品")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (dishes.isEmpty()) {
                EmptyState("还没有菜品，点右下角 ＋ 新增")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    items(dishes, key = { it.id }) { dish ->
                        DishManageRow(
                            dish = dish,
                            onToggleStatus = { scope.launch { vm.toggleStatus(dish) } },
                            onEdit = { navController.navigate("dishEdit?dishId=${dish.id}") },
                            onDelete = { deleteTarget = dish }
                        )
                    }
                }
            }

            // 分类管理 / 订单管理 入口
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { navController.navigate("category") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("分类管理", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp).weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { navController.navigate("orderAdmin") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("订单管理", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp).weight(1f))
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 8.dp))
        }
    }

    if (deleteTarget != null) {
        ConfirmDialog(
            title = "删除菜品",
            message = "确定删除「${deleteTarget!!.name}」？对应图片也会一并删除。",
            confirmText = "删除",
            onConfirm = { deleteTarget?.let { vm.deleteDish(it) }; deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}
