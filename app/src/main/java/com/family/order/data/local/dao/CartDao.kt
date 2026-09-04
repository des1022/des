package com.family.order.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.family.order.data.local.CartEntity
import com.family.order.data.model.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    /**
     * 购物车明细：只存 id 和数量，展示信息实时 join dishes 表，
     * 保证菜品改名/改价后购物车内容同步更新。
     */
    @Query(
        """
        SELECT c.dishId AS dishId, d.name AS name, d.imagePath AS imagePath,
               d.price AS price, c.num AS num
        FROM cart c INNER JOIN dishes d ON c.dishId = d.id
        ORDER BY d.createTime DESC
        """
    )
    fun observeItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart WHERE dishId = :dishId")
    suspend fun findByDishId(dishId: Long): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartEntity)

    @Query("UPDATE cart SET num = :num WHERE dishId = :dishId")
    suspend fun updateNum(dishId: Long, num: Int)

    @Query("DELETE FROM cart WHERE dishId = :dishId")
    suspend fun deleteByDishId(dishId: Long)

    @Query("DELETE FROM cart")
    suspend fun clear()
}
