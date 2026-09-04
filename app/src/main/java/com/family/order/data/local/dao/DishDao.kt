package com.family.order.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.family.order.data.local.DishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {

    /** 全部菜品（管理端使用） */
    @Query("SELECT * FROM dishes ORDER BY createTime DESC")
    fun observeAll(): Flow<List<DishEntity>>

    /** 仅上架菜品（点菜端使用） */
    @Query("SELECT * FROM dishes WHERE status = 1 ORDER BY createTime DESC")
    fun observeOnSale(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getById(id: Long): DishEntity?

    @Insert
    suspend fun insert(dish: DishEntity): Long

    @Update
    suspend fun update(dish: DishEntity)

    /** 上架 / 下架切换 */
    @Query("UPDATE dishes SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int)

    @Query("DELETE FROM dishes WHERE id = :id")
    suspend fun delete(id: Long)

    /** 删除分类时，把该分类下的菜品整体迁移到目标分类，返回受影响行数 */
    @Query("UPDATE dishes SET category = :to WHERE category = :from")
    suspend fun moveCategory(from: String, to: String): Int

    @Query("SELECT COUNT(*) FROM dishes WHERE category = :name")
    suspend fun countByCategory(name: String): Int
}
