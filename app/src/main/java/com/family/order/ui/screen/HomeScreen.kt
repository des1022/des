package com.family.order.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.order.FamilyOrderApp
import com.family.order.data.local.CategoryEntity
import com.family.order.ui.components.CartBar
import com.family.order.ui.components.CartSheet
import com.family.order.ui.components.DishRow
import com.family.order.ui.components.EmptyState
import com.family.order.ui.components.LoadingIndicator
import com.family.order.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<HomeViewModel>(factory = app.container.viewModelFactory)
    val categories by vm.categories.collectAsStateWithLifecycle()
    val cartItems by vm.cartItems.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var keyword by remember { mutableStateOf("") }
    var sheetOpen by remember { mutableStateOf(false) }

    val chips = listOf<Any?>("全部") + categories
    val visible = vm.visibleDishes.filter {
        keyword.isBlank() || it.name.contains(keyword.trim(), ignoreCase = true)
    }

    Scaffold(
        bottomBar = {
            CartBar(
                items = cartItems,
                onCheckout = { navController.navigate("confirm") },
                onSetNum = { id, num -> scope.launch { vm.setCartNum(id, num) } },
                onClear = { scope.launch { vm.clearCart() } },
                onExpand = { sheetOpen = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索框
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("搜索菜名") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            // 分类标签栏（横向滑动）
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(chips, key = { it.toString() }) { item ->
                    val name = if (item is CategoryEntity) item.name else "全部"
                    FilterChip(
                        selected = vm.selectedCategory == (if (item is CategoryEntity) item.name else null),
                        onClick = { vm.selectCategory(if (item is CategoryEntity) item.name else null) },
                        label = { Text(name) }
                    )
                }
            }

            if (visible.isEmpty()) {
                EmptyState(
                    text = if (keyword.isBlank()) "还没有上架的菜品\n先到「我的 → 进入管理」添加，并保持「上架」状态" else "没找到「$keyword」",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.id }) { dish ->
                        DishRow(
                            dish = dish,
                            onAdd = {
                                scope.launch {
                                    vm.addToCart(dish.id)
                                    android.widget.Toast.makeText(
                                        context, "已加入购物车", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        if (sheetOpen) {
            CartSheet(
                items = cartItems,
                onSetNum = { id, num -> scope.launch { vm.setCartNum(id, num) } },
                onClear = { scope.launch { vm.clearCart() } },
                onCheckout = {
                    sheetOpen = false
                    navController.navigate("confirm")
                },
                onDismiss = { sheetOpen = false }
            )
        }
    }
}
