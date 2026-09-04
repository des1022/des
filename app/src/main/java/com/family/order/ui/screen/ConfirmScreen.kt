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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.order.FamilyOrderApp
import com.family.order.data.model.CartItem
import com.family.order.ui.components.AppTopBar
import com.family.order.ui.components.EmptyState
import com.family.order.ui.components.LocalImage
import com.family.order.ui.theme.priceColor
import com.family.order.util.formatPrice
import com.family.order.viewmodel.ConfirmViewModel

@Composable
fun ConfirmScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyOrderApp
    val vm = viewModel<ConfirmViewModel>(factory = app.container.viewModelFactory)
    val cartItems by vm.cartItems.collectAsStateWithLifecycle()
    val remembered by vm.rememberedNickname.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nickname by remember { mutableStateOf(remembered) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(title = "确认下单", onBack = { navController.popBackStack() })

        if (cartItems.isEmpty()) {
            EmptyState("购物车是空的")
        } else {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cartItems, key = { it.dishId }) { item ->
                        CartLine(item)
                    }
                }

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("下单人昵称") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                )
                OutlinedTextField(
                    value = vm.remark,
                    onValueChange = vm::setRemark,
                    label = { Text("订单备注（选填，如：少放盐、不要香菜）") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("共 ${vm.totalNum} 份", style = MaterialTheme.typography.bodyMedium)
                        if (vm.totalPrice > 0) {
                            Text(
                                "¥${formatPrice(vm.totalPrice)}",
                                color = priceColor,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = androidx.compose.material3.MaterialTheme.typography.titleLarge.fontSize
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Button(
                            onClick = {
                            if (nickname.isBlank()) {
                                android.widget.Toast.makeText(context, "请填写下单人昵称", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                vm.submit(nickname) {
                                    android.widget.Toast.makeText(context, "下单成功", android.widget.Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                            },
                            enabled = !vm.submitting
                        ) {
                            Text("提交订单")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartLine(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalImage(item.imagePath, Modifier.size(44.dp).padding(end = 10.dp), corner = 8.dp, sampleSize = 130)
        Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("×${item.num}", style = MaterialTheme.typography.bodyLarge)
        if (item.price > 0) {
            Text(
                "  ¥${formatPrice(item.price * item.num)}",
                color = priceColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
