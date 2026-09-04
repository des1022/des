package com.family.order.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** 订单 + 其下所有菜品，Room 会自动按 orderId 关联查询 */
data class OrderWithGoods(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val goods: List<OrderGoodsEntity>
)
