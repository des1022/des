package com.family.order

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.family.order.data.local.AppDatabase
import com.family.order.data.repository.CartRepository
import com.family.order.data.repository.DishRepository
import com.family.order.data.repository.OrderRepository
import com.family.order.data.repository.SettingsRepository
import com.family.order.util.ImageStore
import com.family.order.viewmodel.AdminViewModel
import com.family.order.viewmodel.CategoryViewModel
import com.family.order.viewmodel.ConfirmViewModel
import com.family.order.viewmodel.DishEditViewModel
import com.family.order.viewmodel.HomeViewModel
import com.family.order.viewmodel.MineViewModel
import com.family.order.viewmodel.OrderAdminViewModel
import com.family.order.viewmodel.OrdersViewModel

/**
 * 极简依赖容器：手工装配，不引入 DI 框架。
 * 所有仓储都是单例，数据库也是单实例，避免多处打开 SQLite。
 */
class AppContainer(applicationContext: android.content.Context) {

    private val database: AppDatabase = AppDatabase.build(applicationContext)
    val imageStore: ImageStore = ImageStore(applicationContext)

    val dishRepository: DishRepository = DishRepository(
        dishDao = database.dishDao(),
        categoryDao = database.categoryDao(),
        imageStore = imageStore
    )

    val orderRepository: OrderRepository = OrderRepository(database.orderDao())

    val cartRepository: CartRepository = CartRepository(database.cartDao())

    val settingsRepository: SettingsRepository = SettingsRepository(applicationContext)

    /** 统一的 ViewModel 工厂，供各页面按类型获取 */
    val viewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                        HomeViewModel(dishRepository, cartRepository) as T
                    modelClass.isAssignableFrom(OrdersViewModel::class.java) ->
                        OrdersViewModel(orderRepository, settingsRepository) as T
                    modelClass.isAssignableFrom(MineViewModel::class.java) ->
                        MineViewModel(settingsRepository) as T
                    modelClass.isAssignableFrom(ConfirmViewModel::class.java) ->
                        ConfirmViewModel(cartRepository, orderRepository, settingsRepository) as T
                    modelClass.isAssignableFrom(AdminViewModel::class.java) ->
                        AdminViewModel(dishRepository) as T
                    modelClass.isAssignableFrom(DishEditViewModel::class.java) ->
                        DishEditViewModel(dishRepository, imageStore) as T
                    modelClass.isAssignableFrom(CategoryViewModel::class.java) ->
                        CategoryViewModel(dishRepository) as T
                    modelClass.isAssignableFrom(OrderAdminViewModel::class.java) ->
                        OrderAdminViewModel(orderRepository) as T
                    else -> throw IllegalArgumentException("未知 ViewModel：$modelClass")
                }
            }
        }
    }
}

class FamilyOrderApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
