package com.family.order.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.family.order.data.local.dao.CartDao
import com.family.order.data.local.dao.CategoryDao
import com.family.order.data.local.dao.DishDao
import com.family.order.data.local.dao.OrderDao

/**
 * 本地数据库，保存在应用私有目录，随应用卸载一并清除。
 * 当前为第 1 版，未定义迁移规则：升级时重建表（家庭菜谱数据量很小，可接受）。
 */
@Database(
    entities = [
        DishEntity::class,
        CategoryEntity::class,
        OrderEntity::class,
        OrderGoodsEntity::class,
        CartEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dishDao(): DishDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao

    companion object {
        private const val DB_NAME = "family_order.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun build(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
