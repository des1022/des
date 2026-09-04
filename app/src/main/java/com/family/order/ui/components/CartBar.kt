package com.family.order.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.order.data.model.CartItem
import com.family.order.ui.theme.priceColor
import com.family.order.util.formatPrice

/**
 * 底部常驻购物车栏。仅在点了菜的（totalNum>0）时显示；
 * 点击栏体展开明细（onExpand），明细弹层由调用方以 CartSheet 渲染在内容层。
 */
@Composable
fun CartBar(
    items: List<CartItem>,
    onCheckout: () -> Unit,
    onSetNum: (Long, Int) -> Unit,
    onClear: () -> Unit,
    onExpand: () -> Unit
) {
    val totalNum = items.sumOf { it.num }
    val totalPrice = items.sumOf { it.price * it.num }

    AnimatedVisibility(visible = totalNum > 0) {
        Surface(
            shadowElevation = 8.dp,
            tonalElevation = 0.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpand() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "购物车",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        if (totalNum > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(y = (-14).dp, x = 12.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    totalNum.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "共 $totalNum 份",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (totalPrice > 0) {
                            Text(
                                "¥${formatPrice(totalPrice)}",
                                color = priceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onCheckout) {
                        Text("去结算")
                    }
                }
            }
        }
    }
}

/**
 * 购物车明细弹层（ModalBottomSheet）。在屏幕内容层调用，保证全屏正确渲染。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(
    items: List<CartItem>,
    onSetNum: (Long, Int) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        val totalNum = items.sumOf { it.num }
        val totalPrice = items.sumOf { it.price * it.num }
        Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("购物车", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClear) { Text("清空") }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((56 * items.size.coerceAtLeast(1)).dp.coerceAtMost(380.dp)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(items, key = { it.dishId }) { item ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LocalImage(
                            path = item.imagePath,
                            modifier = Modifier.size(48.dp),
                            corner = 8.dp,
                            sampleSize = 140
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                            if (item.price > 0) {
                                Text(
                                    "¥${formatPrice(item.price * item.num)}",
                                    color = priceColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        QuantityStepper(
                            count = item.num,
                            onMinus = { onSetNum(item.dishId, item.num - 1) },
                            onPlus = { onSetNum(item.dishId, item.num + 1) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("合计 $totalNum 份", style = MaterialTheme.typography.bodyMedium)
                    if (totalPrice > 0) {
                        Text(
                            "¥${formatPrice(totalPrice)}",
                            color = priceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onCheckout) { Text("去结算") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
