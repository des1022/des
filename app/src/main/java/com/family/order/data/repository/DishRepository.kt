package com.family.order.data.repository

import com.family.order.data.local.CategoryEntity
import com.family.order.data.local.DishEntity
import com.family.order.data.local.dao.CategoryDao
import com.family.order.data.local.dao.DishDao
import com.family.order.util.ImageStore
import kotlinx.coroutines.flow.Flow

/**
 * 菜品与分类的仓储层：封装 DAO，并处理「删除菜品时同步删除本地图片文件」这类跨资源操作。
 */
class DishRepository(
    private val dishDao: DishDao,
    private val categoryDao: CategoryDao,
    private val imageStore: ImageStore
) {

    /** 上架菜品（点菜端） */
    fun observeOnSale(): Flow<List<DishEntity>> = dishDao.observeOnSale()

    /** 全部菜品（管理端） */
    fun observeAll(): Flow<List<DishEntity>> = dishDao.observeAll()

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun getById(id: Long): DishEntity? = dishDao.getById(id)

    suspend fun add(dish: DishEntity): Long = dishDao.insert(dish)

    suspend fun update(dish: DishEntity) = dishDao.update(dish)

    suspend fun setStatus(id: Long, status: Int) = dishDao.updateStatus(id, status)

    /** 删除菜品，并同步删除其本地图片 */
    suspend fun delete(id: Long) {
        dishDao.getById(id)?.let { imageStore.delete(it.imagePath) }
        dishDao.delete(id)
    }

    /**
     * 保存编辑结果。若更换了图片，删除旧图片文件，避免私有目录堆积垃圾。
     */
    suspend fun updateWithImageCleanup(dish: DishEntity, oldImagePath: String) {
        if (oldImagePath.isNotBlank() && oldImagePath != dish.imagePath) {
            imageStore.delete(oldImagePath)
        }
        dishDao.update(dish)
    }

    suspend fun countByCategory(name: String): Int = dishDao.countByCategory(name)

    /** 分类转移：把 from 分类下的菜品整体迁到 to */
    suspend fun moveCategory(from: String, to: String): Int = dishDao.moveCategory(from, to)

    /** 新增分类，重名返回 false */
    suspend fun addCategory(name: String): Boolean {
        if (categoryDao.countByName(name) > 0) return false
        val sort = 0
        categoryDao.insert(CategoryEntity(name = name, sort = sort))
        return true
    }

    /** 删除分类：菜品转移由调用方（ViewModel）先处理，这里只删分类本身 */
    suspend fun deleteCategory(id: Long) = categoryDao.delete(id)
}
