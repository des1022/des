package com.family.order.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.family.order.data.local.OrderEntity
import com.family.order.data.local.OrderGoodsEntity
import com.family.order.data.local.OrderWithGoods
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    /** 所有订单（含菜品明细），按下单时间倒序 */
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createTime DESC")
    fun observeAll(): Flow<List<OrderWithGoods>>

    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertGoods(goods: List<OrderGoodsEntity>)

    /** 修改订单状态：0 待制作 / 1 制作中 / 2 已完成 */
    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int)

    /**
     * 删除订单。order_goods 上配置了外键级联，
     * 这里仍显式先删明细，避免个别机型外键未生效留下脏数据。
     */
    @Transaction
    suspend fun deleteOrderCascade(id: Long) {
        deleteGoodsByOrderId(id)
        deleteOrder(id)
    }

    @Query("DELETE FROM order_goods WHERE orderId = :orderId")
    suspend fun deleteGoodsByOrderId(orderId: Long)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: Long)
}
