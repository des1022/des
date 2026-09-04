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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.order.FamilyOrderApp
import com.family.order.data.local.CategoryEntity
import com.family.order.ui.components.AppTopBar
import com.family.order.ui.components.EmptyState
import com.family.order.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

@Composable
fun CategoryScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<CategoryViewModel>(factory = app.container.viewModelFactory)
    val categories by vm.categories.collectAsStateWithLifecycle()
    val counts by vm.dishCounts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var transferTarget by remember { mutableStateOf<String?>(null) }
    var showTransfer by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(title = "分类管理", onBack = { navController.popBackStack() })

        // 新增分类按钮
        TextButton(onClick = { newName = ""; showAdd = true }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("新增分类")
        }

        if (categories.isEmpty()) {
            EmptyState("还没有分类")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { it.id }) { cat ->
                    val count = counts[cat.name] ?: 0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(cat.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "$count 道菜",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                if (count > 0) {
                                    transferTarget = null
                                    showTransfer = true
                                }
                                deleteTarget = cat
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除分类", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // 新增分类弹窗
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("新增分类") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val ok = vm.addCategory(newName)
                        if (!ok) {
                            android.widget.Toast.makeText(context, "分类名为空或已存在", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            showAdd = false
                        }
                    }
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("取消") } }
        )
    }

    // 删除含菜品分类时的「转移」弹窗
    if (showTransfer && deleteTarget != null) {
        val others = categories.filter { it.id != deleteTarget!!.id }
        AlertDialog(
            onDismissRequest = { showTransfer = false },
            title = { Text("转移菜品") },
            text = {
                Column {
                    Text("「${deleteTarget!!.name}」下还有菜品，请选择转移到哪个分类：")
                    if (others.isEmpty()) {
                        Text("（当前没有其他分类，请先新增一个分类）", color = MaterialTheme.colorScheme.error)
                    } else {
                        others.forEach { other ->
                            Row(
                                Modifier.fillMaxWidth().clickable { transferTarget = other.name },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = transferTarget == other.name, onClick = { transferTarget = other.name })
                                Text(other.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = others.isNotEmpty() && transferTarget != null,
                    onClick = {
                        scope.launch { vm.deleteCategory(deleteTarget!!, transferTarget) }
                        showTransfer = false
                        deleteTarget = null
                    }
                ) { Text("删除并转移") }
            },
            dismissButton = { TextButton(onClick = { showTransfer = false; deleteTarget = null }) { Text("取消") } }
        )
    }

    // 空分类直接删除确认
    if (!showTransfer && deleteTarget != null) {
        val count = counts[deleteTarget!!.name] ?: 0
        if (count == 0) {
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("删除分类") },
                text = { Text("确定删除「${deleteTarget!!.name}」？") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch { vm.deleteCategory(deleteTarget!!, null) }
                        deleteTarget = null
                    }) { Text("删除") }
                },
                dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } }
            )
        }
    }
}
