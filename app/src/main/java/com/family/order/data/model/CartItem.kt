package com.family.order.data.model

/**
 * 购物车条目：由 cart 表与 dishes 表 join 得到的结果，
 * 不是独立的数据表，仅用于 UI 展示。
 */
data class CartItem(
    val dishId: Long,
    val name: String,
    val imagePath: String,
    val price: Double,
    val num: Int
)
