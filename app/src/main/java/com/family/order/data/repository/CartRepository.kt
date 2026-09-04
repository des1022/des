package com.family.order.data.repository

import com.family.order.data.local.CartEntity
import com.family.order.data.local.dao.CartDao
import com.family.order.data.model.CartItem
import kotlinx.coroutines.flow.Flow

/**
 * 购物车仓储层。
 * 数据落在 Room 的 cart 表，因此杀进程、重启手机后购物车内容依然保留。
 */
class CartRepository(private val cartDao: CartDao) {

    fun observeItems(): Flow<List<CartItem>> = cartDao.observeItems()

    /** 加购：已存在则数量 +1 */
    suspend fun add(dishId: Long) {
        val current = cartDao.findByDishId(dishId)
        if (current == null) {
            cartDao.upsert(CartEntity(dishId = dishId, num = 1))
        } else {
            cartDao.updateNum(dishId, current.num + 1)
        }
    }

    /** 设置数量，减到 0 自动移除 */
    suspend fun setNum(dishId: Long, num: Int) {
        if (num <= 0) cartDao.deleteByDishId(dishId) else cartDao.updateNum(dishId, num)
    }

    suspend fun increase(dishId: Long) {
        val current = cartDao.findByDishId(dishId) ?: return
        cartDao.updateNum(dishId, current.num + 1)
    }

    suspend fun decrease(dishId: Long) {
        val current = cartDao.findByDishId(dishId) ?: return
        if (current.num <= 1) cartDao.deleteByDishId(dishId) else cartDao.updateNum(dishId, current.num - 1)
    }

    suspend fun remove(dishId: Long) = cartDao.deleteByDishId(dishId)

    suspend fun clear() = cartDao.clear()
}
