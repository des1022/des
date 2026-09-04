package com.family.order.data.repository

import com.family.order.data.local.OrderEntity
import com.family.order.data.local.OrderGoodsEntity
import com.family.order.data.local.OrderWithGoods
import com.family.order.data.local.dao.OrderDao
import com.family.order.data.model.CartItem
import kotlinx.coroutines.flow.Flow

/** 订单仓储层 */
class OrderRepository(private val orderDao: OrderDao) {

    fun observeAll(): Flow<List<OrderWithGoods>> = orderDao.observeAll()

    /**
     * 提交订单：先写主表拿到自增 id，再批量写明细，两步在同一个事务语义下完成。
     */
    suspend fun submit(nickname: String, remark: String, items: List<CartItem>): Long {
        val totalNum = items.sumOf { it.num }
        val totalPrice = items.sumOf { it.price * it.num }
        val orderId = orderDao.insertOrder(
            OrderEntity(
                nickname = nickname,
                remark = remark,
                totalNum = totalNum,
                totalPrice = totalPrice,
                status = OrderEntity.STATUS_WAIT
            )
        )
        orderDao.insertGoods(
            items.map {
                OrderGoodsEntity(
                    orderId = orderId,
                    dishId = it.dishId,
                    name = it.name,
                    imagePath = it.imagePath,
                    price = it.price,
                    num = it.num
                )
            }
        )
        return orderId
    }

    suspend fun updateStatus(id: Long, status: Int) = orderDao.updateStatus(id, status)

    suspend fun delete(id: Long) = orderDao.deleteOrderCascade(id)
}
