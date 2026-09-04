package com.family.order.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 菜品表。
 * imagePath 存的是应用私有目录下的本地文件绝对路径（非网络 URL），
 * 因为本应用纯本地运行、不联网。
 */
@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val imagePath: String,
    val price: Double = 0.0,
    val desc: String = "",
    /** 1 = 上架，0 = 下架 */
    val status: Int = STATUS_ON,
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_OFF = 0
        const val STATUS_ON = 1
    }
}

/** 分类表：与菜品通过 category 名称关联，删除分类前需先转移菜品 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sort: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

/**
 * 订单主表。
 * status：0 待制作 / 1 制作中 / 2 已完成
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val remark: String = "",
    val totalNum: Int = 0,
    val totalPrice: Double = 0.0,
    val status: Int = STATUS_WAIT,
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_WAIT = 0
        const val STATUS_COOKING = 1
        const val STATUS_DONE = 2
    }
}

/** 订单里的每一道菜。快照式存储：菜品后续改名/改价不影响历史订单 */
@Entity(
    tableName = "order_goods",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class OrderGoodsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val dishId: Long,
    val name: String,
    val imagePath: String,
    val price: Double,
    val num: Int
)

/**
 * 购物车只存「菜品 id + 数量」，名称/图片/价格一律从 dishes 表实时 join 读取。
 * 好处：菜品改名或改价后购物车自动同步；菜品被删除时由外键级联自动移除。
 */
@Entity(
    tableName = "cart",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId")]
)
data class CartEntity(
    @PrimaryKey val dishId: Long,
    val num: Int,
    val createTime: Long = System.currentTimeMillis()
)
