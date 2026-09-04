package com.family.order.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.family.order.ui.components.AppTopBar
import com.family.order.viewmodel.DishEditViewModel
import com.family.order.ui.components.LocalImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishEditScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<DishEditViewModel>(factory = app.container.viewModelFactory)
    val context = LocalContext.current

    val dishIdArg = navController.currentBackStackEntry?.arguments?.getString("dishId")?.toLongOrNull()

    val categories by app.container.dishRepository.observeCategories()
        .collectAsStateWithLifecycle(initialValue = emptyList<com.family.order.data.local.CategoryEntity>())

    var menuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch { vm.onPickImage(uri) }
    }

    // 存图失败时给出明确提示（失败已由 ViewModel 捕获，不再导致闪退）
    androidx.compose.runtime.LaunchedEffect(vm.pickError) {
        vm.pickError?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            vm.clearPickError()
        }
    }

    // 进入页面时加载（新增或编辑）
    androidx.compose.runtime.LaunchedEffect(dishIdArg) {
        vm.load(dishIdArg)
    }

    if (!vm.loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(
            title = if (dishIdArg == null) "新增菜品" else "编辑菜品",
            onBack = { navController.popBackStack() }
        )

        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 菜品图片
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (vm.imagePath.isNotBlank()) {
                    LocalImage(vm.imagePath, Modifier.fillMaxSize(), corner = 12.dp, sampleSize = 600)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("点击选择菜品图片", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("（建议横图，自动压缩到 800px 内）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 名称
            OutlinedTextField(
                value = vm.name,
                onValueChange = { if (it.length <= 10) vm.updateName(it) },
                label = { Text("菜品名称（最多 10 字）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 分类（下拉选择已有 + 可手动输入新增）
            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                OutlinedTextField(
                    value = vm.category,
                    onValueChange = vm::updateCategory,
                    label = { Text("菜品分类") },
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                vm.updateCategory(cat.name)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            // 价格（选填）
            OutlinedTextField(
                value = vm.priceText,
                onValueChange = vm::updatePriceText,
                label = { Text("价格（选填，留空表示不显示价格）") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // 描述（选填）
            OutlinedTextField(
                value = vm.desc,
                onValueChange = { if (it.length <= 30) vm.updateDesc(it) },
                label = { Text("菜品描述（选填，最多 30 字）") },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // 上架状态
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("默认上架", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = vm.status == com.family.order.data.local.DishEntity.STATUS_ON,
                    onCheckedChange = { vm.updateStatus(if (it) com.family.order.data.local.DishEntity.STATUS_ON else com.family.order.data.local.DishEntity.STATUS_OFF) }
                )
            }

            Button(
                onClick = {
                    val err = vm.validate()
                    if (err != null) {
                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch { vm.save { navController.popBackStack() } }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}
